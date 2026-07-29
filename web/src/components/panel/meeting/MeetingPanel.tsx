import { useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import IconArrowsReload from "@/assets/icons/arrow/ic_arrows_reload.svg?react";
import IconAddPlusSquare from "@/assets/icons/edit/ic_add_plus_square.svg?react";
import IconSearch from "@/assets/icons/interface/ic_search.svg?react";
import { Icon } from "@/components/common/Icon";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import { useCreateMeeting } from "@/pages/meeting/hooks/useCreateMeeting";
import { useMeetingList } from "@/pages/meeting/hooks/useMeetingList";
import { parseRouteId } from "@/utils/parseRouteId";
import MeetingPanelItem from "./MeetingPanelItem";
import StartMeetingModal from "./StartMeetingModal";

const MeetingPanel = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [keyword, setKeyword] = useState("");
  const navigate = useNavigate();
  const { teamId } = useCurrentTeam();
  const { meetingId: activeMeetingIdParam } = useParams<{
    meetingId?: string;
  }>();
  const activeMeetingId = parseRouteId(activeMeetingIdParam);
  const { createMeeting, isPending } = useCreateMeeting(teamId, () =>
    setIsModalOpen(false),
  );
  const {
    ongoing: ongoingMeetings,
    completed: completedMeetings,
    isLoading,
    isError,
    isFetching,
    refetch,
  } = useMeetingList(teamId);
  const normalizedKeyword = keyword.trim();

  const filteredCompleted = useMemo(() => {
    if (!normalizedKeyword) return completedMeetings;
    const lower = normalizedKeyword.toLowerCase();
    return completedMeetings.filter((m) =>
      m.name.toLowerCase().includes(lower),
    );
  }, [completedMeetings, normalizedKeyword]);

  const hasAnyMeetings =
    ongoingMeetings.length > 0 || completedMeetings.length > 0;

  return (
    <>
      {/* Header */}
      <div className="flex w-full items-center justify-between px-5">
        <h2 className="typo-h6 text-(--color-text-primary)">회의 목록</h2>
        <div className="flex items-center gap-2">
          {hasAnyMeetings && (
            <button
              type="button"
              className="cursor-pointer disabled:opacity-50"
              onClick={() => refetch()}
              disabled={isFetching}
              aria-label="회의 목록 새로고침"
            >
              <Icon
                icon={IconArrowsReload}
                size={20}
                className={`text-(--color-text-secondary) ${
                  isFetching ? "animate-spin" : ""
                }`}
              />
            </button>
          )}
          <button
            type="button"
            className="cursor-pointer"
            onClick={() => setIsModalOpen(true)}
            aria-label="회의 시작"
          >
            <Icon
              icon={IconAddPlusSquare}
              size={24}
              className="text-(--color-text-secondary)"
            />
          </button>
        </div>
      </div>

      {/* Divider */}
      <div className="h-px w-full bg-(--color-border-divider)" />

      {/* Ongoing meetings section */}
      {ongoingMeetings.length > 0 && teamId && (
        <div className="flex w-full shrink-0 flex-col gap-2 px-4">
          {ongoingMeetings.map((meeting) => (
            <MeetingPanelItem
              key={meeting.meeting_id}
              title={meeting.name}
              isLive
              isActive={meeting.meeting_id === activeMeetingId}
              elapsedTime={meeting.elapse ?? "00:00:00"}
              onClick={() =>
                navigate(`/team/${teamId}/meeting/${meeting.meeting_id}`)
              }
            />
          ))}
        </div>
      )}

      {isLoading ? (
        <div className="flex w-full flex-1 items-center justify-center px-4 typo-subtitle5 text-(--color-text-tertiary)">
          불러오는 중...
        </div>
      ) : isError ? (
        <div className="flex w-full flex-1 items-center justify-center px-4 typo-subtitle5 text-(--color-text-tertiary)">
          회의 목록을 불러오지 못했습니다.
        </div>
      ) : !hasAnyMeetings && !normalizedKeyword ? (
        <div className="flex w-full flex-1 flex-col items-center justify-center px-4 text-center">
          <div className="typo-subtitle4 text-(--color-text-secondary)">
            진행된 회의가
            <br />
            없습니다
          </div>
          <div className="mt-2 typo-body6 text-(--color-text-tertiary)">
            음성 회의를 시작하거나
            <br />
            기존 녹음 파일을 업로드해보세요
          </div>
        </div>
      ) : (
        <div className="flex w-full flex-1 flex-col gap-2 overflow-y-auto px-4">
          <div className="flex w-full items-center gap-2 rounded-lg bg-(--color-bg-subtle) p-3">
            <Icon
              icon={IconSearch}
              size={24}
              className="shrink-0 text-(--color-text-brand)"
            />
            <input
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              aria-label="회의 이름 검색"
              placeholder="회의 이름으로 검색하세요..."
              className="w-full bg-transparent typo-subtitle5 text-(--color-text-primary) placeholder:text-(--color-text-disabled) focus:outline-none"
            />
          </div>

          {filteredCompleted.length === 0 ? (
            <div className="flex w-full flex-1 items-center justify-center py-8 typo-subtitle5 text-(--color-text-tertiary)">
              {normalizedKeyword
                ? "검색 결과가 없습니다."
                : "완료된 회의가 없습니다."}
            </div>
          ) : (
            filteredCompleted.map((meeting) => (
              <MeetingPanelItem
                key={meeting.meeting_id}
                title={meeting.name}
                isActive={meeting.meeting_id === activeMeetingId}
                onClick={() =>
                  teamId &&
                  navigate(`/team/${teamId}/meeting/${meeting.meeting_id}`)
                }
              />
            ))
          )}
        </div>
      )}

      {isModalOpen && (
        <StartMeetingModal
          onClose={() => setIsModalOpen(false)}
          onStart={(meetingName) => createMeeting(meetingName)}
          isPending={isPending}
        />
      )}
    </>
  );
};

export default MeetingPanel;
