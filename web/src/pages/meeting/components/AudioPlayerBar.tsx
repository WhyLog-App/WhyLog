import { useEffect, useRef, useState } from "react";

interface AudioPlayerBarProps {
  durationSec?: number | null;
  audioUrl?: string | null;
}

const RewindIcon = () => (
  <svg
    width="20"
    height="20"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <path d="M3 12a9 9 0 1 0 3-6.7" />
    <path d="M3 4v5h5" />
  </svg>
);

const ForwardIcon = () => (
  <svg
    width="20"
    height="20"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <path d="M21 12a9 9 0 1 1-3-6.7" />
    <path d="M21 4v5h-5" />
  </svg>
);

const PlayIcon = () => (
  <svg
    width="22"
    height="22"
    viewBox="0 0 24 24"
    fill="currentColor"
    aria-hidden="true"
  >
    <path d="M8 5v14l11-7L8 5z" />
  </svg>
);

const PauseIcon = () => (
  <svg
    width="22"
    height="22"
    viewBox="0 0 24 24"
    fill="currentColor"
    aria-hidden="true"
  >
    <rect x="6" y="5" width="4" height="14" rx="1" />
    <rect x="14" y="5" width="4" height="14" rx="1" />
  </svg>
);

const AudioPlayerBar = ({ durationSec, audioUrl }: AudioPlayerBarProps) => {
  const audioRef = useRef<HTMLAudioElement>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentSec, setCurrentSec] = useState(0);
  const [loadedDuration, setLoadedDuration] = useState(0);

  const disabled = !audioUrl;
  const propDuration =
    typeof durationSec === "number" &&
    Number.isFinite(durationSec) &&
    durationSec > 0
      ? durationSec
      : 0;
  const effectiveDuration = loadedDuration > 0 ? loadedDuration : propDuration;

  // biome-ignore lint/correctness/useExhaustiveDependencies: reset state on audio source change
  useEffect(() => {
    setIsPlaying(false);
    setCurrentSec(0);
    setLoadedDuration(0);
  }, [audioUrl]);

  const handleTogglePlay = () => {
    const audio = audioRef.current;
    if (!audio) return;
    if (audio.paused) {
      audio.play().catch(() => setIsPlaying(false));
    } else {
      audio.pause();
    }
  };

  const handleSeek = (delta: number) => {
    const audio = audioRef.current;
    if (!audio) return;
    const max = Number.isFinite(audio.duration)
      ? audio.duration
      : effectiveDuration;
    audio.currentTime = Math.max(
      0,
      Math.min(max || 0, audio.currentTime + delta),
    );
  };

  const progressPct =
    effectiveDuration > 0
      ? Math.max(0, Math.min(100, (currentSec / effectiveDuration) * 100))
      : 0;

  return (
    <div className="flex items-center gap-4">
      {audioUrl ? (
        <audio
          ref={audioRef}
          src={audioUrl}
          preload="metadata"
          onPlay={() => setIsPlaying(true)}
          onPause={() => setIsPlaying(false)}
          onEnded={() => setIsPlaying(false)}
          onTimeUpdate={(e) => setCurrentSec(e.currentTarget.currentTime)}
          onSeeked={(e) => setCurrentSec(e.currentTarget.currentTime)}
          onLoadedMetadata={(e) => {
            setCurrentSec(e.currentTarget.currentTime);
            if (Number.isFinite(e.currentTarget.duration)) {
              setLoadedDuration(e.currentTarget.duration);
            }
          }}
          onDurationChange={(e) => {
            if (Number.isFinite(e.currentTarget.duration)) {
              setLoadedDuration(e.currentTarget.duration);
            }
          }}
          className="hidden"
        >
          <track kind="captions" />
        </audio>
      ) : null}

      <button
        type="button"
        onClick={() => handleSeek(-10)}
        disabled={disabled}
        className="flex h-9 w-9 cursor-pointer items-center justify-center rounded-full text-(--color-text-secondary) hover:bg-(--color-bg-subtle) disabled:cursor-not-allowed disabled:opacity-40"
        aria-label="뒤로 감기"
      >
        <RewindIcon />
      </button>

      <button
        type="button"
        onClick={handleTogglePlay}
        disabled={disabled}
        className="flex h-11 w-11 cursor-pointer items-center justify-center rounded-full bg-(--color-primary-500) text-white hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-40"
        aria-label={isPlaying ? "일시정지" : "재생"}
      >
        {isPlaying ? <PauseIcon /> : <PlayIcon />}
      </button>

      <button
        type="button"
        onClick={() => handleSeek(10)}
        disabled={disabled}
        className="flex h-9 w-9 cursor-pointer items-center justify-center rounded-full text-(--color-text-secondary) hover:bg-(--color-bg-subtle) disabled:cursor-not-allowed disabled:opacity-40"
        aria-label="앞으로 감기"
      >
        <ForwardIcon />
      </button>

      <div className="ml-2 flex-1">
        <div className="h-1.5 w-full rounded-full bg-(--color-bg-subtle)">
          <div
            className="h-full rounded-full bg-(--color-primary-500)"
            style={{ width: `${progressPct}%` }}
          />
        </div>
      </div>
    </div>
  );
};

export default AudioPlayerBar;
