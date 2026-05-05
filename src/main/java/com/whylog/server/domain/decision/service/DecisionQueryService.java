package com.whylog.server.domain.decision.service;

import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.domain.decision.entity.Decision;
import com.whylog.server.domain.decision.exception.DecisionNotFoundException;
import com.whylog.server.domain.decision.repository.DecisionRepository;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.user.entity.Member;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DecisionQueryService {

    private final DecisionRepository decisionRepository;

    // 결정사항 상세 조회에 필요한 회의 정보와 참여자 정보를 조회
    @Transactional(readOnly = true)
    public DecisionResponse.DecisionDetailDTO getDecisionDetail(Long decisionId) {
        Decision decision = decisionRepository.findDetailById(decisionId)
                .orElseThrow(DecisionNotFoundException::new);

        Meeting meeting = decision.getMeeting();

        return DecisionResponse.DecisionDetailDTO.builder()
                .decisionId(decision.getId())
                .name(meeting.getName())
                .meetingDate(meeting.getStartDateTime() != null ? meeting.getStartDateTime().toLocalDate() : null)
                .meetingTime(buildMeetingDuration(meeting))
                .memberCount(meeting.getMeetingMembers().size())
                .members(
                        meeting.getMeetingMembers().stream()
                                .map(MeetingMember::getMember)
                                .map(this::toParticipant)
                                .toList()
                )
                .build();
    }

    // 회의 소요 시간을 시간, 분 단위로 변환
    private String buildMeetingDuration(Meeting meeting) {
        Long duration = meeting.getDuration();
        if (duration == null) {
            return null;
        }

        long hours = duration / 60;
        long minutes = duration % 60;

        if (hours == 0) {
            return minutes + "분";
        }

        if (minutes == 0) {
            return hours + "시간";
        }

        return hours + "시간 " + minutes + "분";
    }

    // 멤버 엔티티를 결정사항 상세 참여자 응답으로 변환
    private DecisionResponse.DecisionParticipantDTO toParticipant(Member member) {
        return DecisionResponse.DecisionParticipantDTO.builder()
                .memberId(member.getId())
                .name(member.getName())
                .profileImage(member.getProfileImage())
                .build();
    }
}
