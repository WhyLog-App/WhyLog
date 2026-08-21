import RelationshipMap from "./components/RelationshipMap";

function DecisionsPage() {
  return (
    <div className="flex min-h-full flex-col gap-5 py-8 lg:py-10">
      <header className="flex flex-col gap-2">
        <div className="flex flex-wrap items-center gap-3">
          <h1 className="typo-h5 text-(--color-text-primary)">결정 관계도</h1>
          <span className="rounded-full bg-(--color-bg-brand-subtle) px-2 py-1 typo-caption1 text-(--color-text-brand)">
            예시 데이터
          </span>
        </div>
        <p className="typo-body5 text-(--color-text-secondary)">
          카드를 클릭하면 연결된 관계가 강조됩니다. 빈 화면을 드래그하거나
          확대·축소하여 관계도를 탐색하세요.
        </p>
      </header>
      <div className="min-h-[620px] flex-1">
        <RelationshipMap />
      </div>
    </div>
  );
}

export default DecisionsPage;
