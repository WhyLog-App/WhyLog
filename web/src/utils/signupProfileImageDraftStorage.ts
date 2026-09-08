const DB_NAME = "whylog-signup-profile-image-drafts";
const DB_VERSION = 1;
const STORE_NAME = "drafts";
const POINTER_KEY = "whylog:signup-profile-image-draft-id";
const DRAFT_TTL_MS = 60 * 60 * 1000;

const STORAGE_WARNING =
  "브라우저 저장소를 사용할 수 없어 새로고침하면 프로필 이미지가 사라질 수 있습니다.";

interface SignupProfileImageDraftRecord {
  id: string;
  email: string;
  file: File;
  expiresAt: number;
}

interface SaveSignupProfileImageDraftResult {
  warning: string | null;
  expiresAt: number;
}

const isBrowser = () => typeof window !== "undefined";

const normalizeEmail = (email: string) => email.trim().toLowerCase();

const createDraftId = () => {
  if (isBrowser() && window.crypto?.randomUUID) {
    return window.crypto.randomUUID();
  }
  return `signup-profile-${Date.now()}-${Math.random().toString(36).slice(2)}`;
};

const openDraftDb = () =>
  new Promise<IDBDatabase>((resolve, reject) => {
    if (!isBrowser() || !("indexedDB" in window)) {
      reject(new Error("IndexedDB is not available."));
      return;
    }

    const request = window.indexedDB.open(DB_NAME, DB_VERSION);
    let settled = false;

    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: "id" });
      }
    };

    request.onsuccess = () => {
      if (settled) {
        request.result.close();
        return;
      }
      settled = true;
      resolve(request.result);
    };
    request.onerror = () => {
      if (settled) return;
      settled = true;
      reject(request.error ?? new Error("IndexedDB open failed."));
    };
    request.onblocked = () => {
      if (settled) return;
      settled = true;
      reject(new Error("IndexedDB open was blocked."));
    };
  });

const runDraftTransaction = <T>(
  mode: IDBTransactionMode,
  executor: (store: IDBObjectStore) => IDBRequest<T>,
) =>
  new Promise<T>((resolve, reject) => {
    openDraftDb()
      .then((db) => {
        try {
          let result: T;
          let settled = false;
          const transaction = db.transaction(STORE_NAME, mode);
          const store = transaction.objectStore(STORE_NAME);
          const request = executor(store);

          const rejectOnce = (error: unknown) => {
            if (settled) return;
            settled = true;
            db.close();
            reject(error);
          };

          request.onsuccess = () => {
            result = request.result;
          };
          request.onerror = () => {
            rejectOnce(request.error ?? new Error("IndexedDB request failed."));
          };
          transaction.oncomplete = () => {
            if (settled) return;
            settled = true;
            db.close();
            resolve(result);
          };
          transaction.onerror = () => {
            rejectOnce(
              transaction.error ?? new Error("IndexedDB transaction failed."),
            );
          };
          transaction.onabort = () => {
            rejectOnce(
              transaction.error ?? new Error("IndexedDB transaction aborted."),
            );
          };
        } catch (error) {
          db.close();
          reject(error);
        }
      })
      .catch(reject);
  });

const getDraftPointer = () => {
  try {
    if (!isBrowser()) return null;
    return window.sessionStorage.getItem(POINTER_KEY);
  } catch (error) {
    console.error("회원가입 프로필 이미지 draft 포인터 조회 실패:", error);
    return null;
  }
};

const saveDraftPointer = (id: string) => {
  try {
    if (!isBrowser()) return false;
    window.sessionStorage.setItem(POINTER_KEY, id);
    return true;
  } catch (error) {
    console.error("회원가입 프로필 이미지 draft 포인터 저장 실패:", error);
    return false;
  }
};

const clearDraftPointer = () => {
  try {
    if (!isBrowser()) return;
    window.sessionStorage.removeItem(POINTER_KEY);
  } catch (error) {
    console.error("회원가입 프로필 이미지 draft 포인터 삭제 실패:", error);
  }
};

const clearDraftPointerIfCurrent = (id: string) => {
  if (getDraftPointer() !== id) return;
  clearDraftPointer();
};

const removeDraftById = async (id: string) => {
  await runDraftTransaction("readwrite", (store) => store.delete(id));
};

export const clearExpiredSignupProfileImageDrafts = async () => {
  try {
    const records = await runDraftTransaction<SignupProfileImageDraftRecord[]>(
      "readonly",
      (store) => store.getAll(),
    );
    const now = Date.now();
    await Promise.all(
      records
        .filter((record) => record.expiresAt <= now)
        .map((record) => removeDraftById(record.id)),
    );
  } catch (error) {
    console.error("만료된 회원가입 프로필 이미지 draft 정리 실패:", error);
  }
};

export const saveSignupProfileImageDraft = async (
  email: string,
  file: File,
): Promise<SaveSignupProfileImageDraftResult> => {
  const normalizedEmail = normalizeEmail(email);
  const previousDraftId = getDraftPointer();
  const nextDraftId = createDraftId();
  const now = Date.now();
  const expiresAt = now + DRAFT_TTL_MS;
  const record: SignupProfileImageDraftRecord = {
    id: nextDraftId,
    email: normalizedEmail,
    file,
    expiresAt,
  };

  try {
    await clearExpiredSignupProfileImageDrafts();
    await runDraftTransaction("readwrite", (store) => store.put(record));
    if (!saveDraftPointer(nextDraftId)) {
      await removeDraftById(nextDraftId);
      return { warning: STORAGE_WARNING, expiresAt };
    }
    if (previousDraftId && previousDraftId !== nextDraftId) {
      await removeDraftById(previousDraftId).catch((error) => {
        console.error("이전 회원가입 프로필 이미지 draft 삭제 실패:", error);
      });
    }
    return { warning: null, expiresAt };
  } catch (error) {
    if (previousDraftId) {
      clearDraftPointerIfCurrent(previousDraftId);
      await removeDraftById(previousDraftId).catch((deleteError) => {
        console.error(
          "이전 회원가입 프로필 이미지 draft 삭제 실패:",
          deleteError,
        );
      });
    }
    console.error("회원가입 프로필 이미지 draft 저장 실패:", error);
    return { warning: STORAGE_WARNING, expiresAt };
  }
};

export const loadSignupProfileImageDraft = async (expectedEmail: string) => {
  const draftId = getDraftPointer();
  if (!draftId) return null;

  try {
    await clearExpiredSignupProfileImageDrafts();
    const record = await runDraftTransaction<
      SignupProfileImageDraftRecord | undefined
    >("readonly", (store) => store.get(draftId));

    const shouldClear =
      !record ||
      record.expiresAt <= Date.now() ||
      record.email !== normalizeEmail(expectedEmail) ||
      !(record.file instanceof File);

    if (shouldClear) {
      clearDraftPointerIfCurrent(draftId);
      if (record) await removeDraftById(record.id);
      return null;
    }

    return record.file;
  } catch (error) {
    console.error("회원가입 프로필 이미지 draft 복원 실패:", error);
    return null;
  }
};

export const clearSignupProfileImageDraft = async (
  draftId = getDraftPointer(),
) => {
  if (!draftId) {
    clearDraftPointer();
    return;
  }

  clearDraftPointerIfCurrent(draftId);

  try {
    await removeDraftById(draftId);
  } catch (error) {
    console.error("회원가입 프로필 이미지 draft 삭제 실패:", error);
  }
};
