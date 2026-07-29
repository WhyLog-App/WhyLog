import { useState } from "react";
import EmptyStateCard from "@/components/common/EmptyStateCard";
import StartMeetingModal from "@/components/panel/meeting/StartMeetingModal";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import { useCreateMeeting } from "@/pages/meeting/hooks/useCreateMeeting";

function MeetingPage() {
  const [isStartMeetingModalOpen, setIsStartMeetingModalOpen] = useState(false);
  const { teamId } = useCurrentTeam();
  const { createMeeting, isPending } = useCreateMeeting(teamId, () =>
    setIsStartMeetingModalOpen(false),
  );

  return (
    <div className="flex min-h-full w-full items-center justify-center p-8">
      <EmptyStateCard
        page="Meetings"
        onAction={() => setIsStartMeetingModalOpen(true)}
      />
      {isStartMeetingModalOpen && (
        <StartMeetingModal
          onClose={() => setIsStartMeetingModalOpen(false)}
          onStart={(meetingName) => createMeeting(meetingName)}
          isPending={isPending}
        />
      )}
    </div>
  );
}

export default MeetingPage;
