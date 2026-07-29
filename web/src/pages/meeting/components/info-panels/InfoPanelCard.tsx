interface InfoPanelCardProps {
  title: string;
  items: string[];
}

const EditPenIcon = () => (
  <svg
    width="14"
    height="14"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <path d="M12 20h9" />
    <path d="M16.5 3.5a2.121 2.121 0 1 1 3 3L7 19l-4 1 1-4 12.5-12.5z" />
  </svg>
);

const InfoPanelCard = ({ title, items }: InfoPanelCardProps) => {
  return (
    <section className="flex flex-col gap-3 rounded-2xl border border-white bg-white/50 px-5 py-4 backdrop-blur-md">
      <div className="flex items-center justify-between">
        <h3 className="typo-subtitle4 text-(--color-text-primary)">{title}</h3>
        <button
          type="button"
          className="cursor-pointer text-(--color-text-tertiary) hover:text-(--color-text-secondary)"
          aria-label={`${title} 수정`}
        >
          <EditPenIcon />
        </button>
      </div>
      <ul className="flex flex-col gap-2">
        {items.map((item, idx) => (
          <li
            // biome-ignore lint/suspicious/noArrayIndexKey: static mock bullets
            key={idx}
            className="typo-body6 flex gap-2 text-(--color-text-secondary)"
          >
            <span
              aria-hidden="true"
              className="mt-2 inline-block h-1 w-1 shrink-0 rounded-full bg-(--color-text-secondary)"
            />
            <span className="flex-1 break-words">{item}</span>
          </li>
        ))}
      </ul>
    </section>
  );
};

export default InfoPanelCard;
