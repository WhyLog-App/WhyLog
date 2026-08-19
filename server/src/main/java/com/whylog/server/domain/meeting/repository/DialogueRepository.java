package com.whylog.server.domain.meeting.repository;

import com.whylog.server.domain.meeting.entity.Dialogue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DialogueRepository extends JpaRepository<Dialogue, Long> {

    /**
     * 회의록을 발화 순서대로 조회합니다. 같은 초에 들어온 발화는 speech_datetime만으로 순서가 정해지지 않으므로 삽입 순서와 일치하는 dialogue_id로
     * 순서를 확정합니다. 화자 이름이 필요해 member를 함께 가져옵니다.
     */
    @Query(
            "SELECT d FROM Dialogue d JOIN FETCH d.member"
                    + " WHERE d.meeting.id = :meetingId"
                    + " ORDER BY d.speechDateTime, d.id")
    List<Dialogue> findAllByMeetingIdOrderBySpeechTime(@Param("meetingId") Long meetingId);

    @Modifying
    @Query("DELETE FROM Dialogue d WHERE d.meeting.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM Dialogue d WHERE d.meeting.id = :meetingId")
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}
