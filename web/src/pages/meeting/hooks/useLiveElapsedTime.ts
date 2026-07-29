import { useEffect, useState } from "react";

const pad = (n: number) => n.toString().padStart(2, "0");

const format = (totalSeconds: number) => {
  const safe = Math.max(0, totalSeconds);
  const h = Math.floor(safe / 3600);
  const m = Math.floor((safe % 3600) / 60);
  const s = safe % 60;
  return `${pad(h)}:${pad(m)}:${pad(s)}`;
};

const parseElapse = (elapse: string | null | undefined): number => {
  if (!elapse) return 0;
  const parts = elapse.split(":");
  if (parts.length !== 3) return 0;
  const [h, m, s] = parts.map((p) => Number(p));
  if (!Number.isInteger(h) || !Number.isInteger(m) || !Number.isInteger(s)) {
    return 0;
  }
  if (h < 0 || m < 0 || s < 0) return 0;
  return h * 3600 + m * 60 + s;
};

export const useLiveElapsedTime = (
  elapse: string | null | undefined,
): string => {
  const [baseSeconds, setBaseSeconds] = useState(() => parseElapse(elapse));
  const [baseAt, setBaseAt] = useState(() => Date.now());
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    setBaseSeconds(parseElapse(elapse));
    const t = Date.now();
    setBaseAt(t);
    setNow(t);
  }, [elapse]);

  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(id);
  }, []);

  return format(baseSeconds + Math.floor((now - baseAt) / 1000));
};
