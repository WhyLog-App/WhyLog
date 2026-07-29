import type { RoomParticipant } from "@/pages/meeting/types";
import ParticipantTile from "./ParticipantTile";

interface ParticipantGridProps {
  participants: RoomParticipant[];
}

const ParticipantGrid = ({ participants }: ParticipantGridProps) => {
  const count = participants.length;
  const gridCols = count <= 4 ? "grid-cols-2" : "grid-cols-2 lg:grid-cols-3";

  return (
    <div className={`grid w-full gap-5 ${gridCols}`}>
      {participants.map((p) => (
        <ParticipantTile
          key={p.id}
          name={p.name}
          isSelf={p.isSelf}
          profileImage={p.profileImage}
        />
      ))}
    </div>
  );
};

export default ParticipantGrid;
