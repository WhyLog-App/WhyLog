import { useEffect, useRef, useState } from "react";

// Minimal Web Speech API types (not in lib.dom for all TS versions)
type SpeechRecognitionAlternativeLike = { transcript: string };
type SpeechRecognitionResultLike = {
  isFinal: boolean;
  length: number;
  [index: number]: SpeechRecognitionAlternativeLike;
};
type SpeechRecognitionResultListLike = {
  [index: number]: SpeechRecognitionResultLike;
  length: number;
};
interface SpeechRecognitionEventLike {
  resultIndex: number;
  results: SpeechRecognitionResultListLike;
}
interface SpeechRecognitionErrorEventLike {
  error: string;
}
interface SpeechRecognitionLike {
  lang: string;
  continuous: boolean;
  interimResults: boolean;
  onstart: (() => void) | null;
  onresult: ((event: SpeechRecognitionEventLike) => void) | null;
  onerror: ((event: SpeechRecognitionErrorEventLike) => void) | null;
  onend: (() => void) | null;
  start: () => void;
  stop: () => void;
}
type SpeechRecognitionCtor = new () => SpeechRecognitionLike;

const getCtor = (): SpeechRecognitionCtor | null => {
  if (typeof window === "undefined") return null;
  const w = window as unknown as {
    SpeechRecognition?: SpeechRecognitionCtor;
    webkitSpeechRecognition?: SpeechRecognitionCtor;
  };
  return w.SpeechRecognition ?? w.webkitSpeechRecognition ?? null;
};

interface UseSpeechRecognitionOptions {
  enabled: boolean;
  onInterim: (text: string) => void;
  onFinal: (text: string) => void;
  onError?: (error: string) => void;
}

export const useSpeechRecognition = ({
  enabled,
  onInterim,
  onFinal,
  onError,
}: UseSpeechRecognitionOptions) => {
  const [isSupported] = useState<boolean>(() => getCtor() != null);
  const [isListening, setIsListening] = useState(false);

  const recognitionRef = useRef<SpeechRecognitionLike | null>(null);
  const activeRef = useRef(false);
  // Latest callbacks to avoid stale closures on restart
  const onInterimRef = useRef(onInterim);
  const onFinalRef = useRef(onFinal);
  const onErrorRef = useRef(onError);

  useEffect(() => {
    onInterimRef.current = onInterim;
    onFinalRef.current = onFinal;
    onErrorRef.current = onError;
  }, [onInterim, onFinal, onError]);

  useEffect(() => {
    const Ctor = getCtor();
    if (!Ctor || !enabled) return;

    const recognition = new Ctor();
    recognition.lang = "ko-KR";
    recognition.continuous = true;
    recognition.interimResults = true;

    recognition.onstart = () => {
      recognitionRef.current = recognition;
      activeRef.current = true;
      setIsListening(true);
    };

    recognition.onresult = (event) => {
      const result = event.results[event.resultIndex];
      let transcript = "";
      for (let i = 0; i < result.length; i += 1) {
        transcript += result[i].transcript;
      }
      transcript = transcript.trim();
      if (!transcript) return;

      if (result.isFinal) {
        onFinalRef.current(transcript);
      } else {
        onInterimRef.current(transcript);
      }
    };

    recognition.onerror = (event) => {
      onErrorRef.current?.(event.error);
    };

    recognition.onend = () => {
      // Chrome cuts recognition periodically; auto-restart while still active
      if (activeRef.current) {
        try {
          recognition.start();
        } catch {
          /* start() may throw if called too fast */
        }
      } else {
        setIsListening(false);
      }
    };

    try {
      recognition.start();
    } catch {
      /* already started */
    }

    return () => {
      activeRef.current = false;
      recognitionRef.current = null;
      recognition.onend = null;
      recognition.onresult = null;
      recognition.onerror = null;
      recognition.onstart = null;
      try {
        recognition.stop();
      } catch {
        /* noop */
      }
      setIsListening(false);
    };
  }, [enabled]);

  return { isSupported, isListening };
};
