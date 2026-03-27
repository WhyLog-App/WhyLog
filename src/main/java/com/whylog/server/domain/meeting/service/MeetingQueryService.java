package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.enums.MeetingStatus;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.domain.team.service.TeamUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingQueryService {

    private final MeetingRepository meetingRepository;
    private final TeamUseCase teamUseCase;

    // 미팅 목록 조회
    @Transactional(readOnly = true)
    public List<MeetingResponse.MeetingListDTO> getMeetings(Long teamId, MeetingStatus status){

        // 기본값: 진행완료
        if (status == null) {
            status = MeetingStatus.COMPLETED;
        }

        List<Meeting> meetings = meetingRepository.findByTeamId(teamId);

        return meetings.stream()
                .map(m -> MeetingResponse.MeetingListDTO.builder()
                        .meetingId(m.getId())
                        .name(m.getName())
                        .status(m.getStatus())
                        .elapse( m.isOngoing() ? m.getElapse() : null ) // 진행완료일 경우 null로 반환
                        .build()
                ).toList();

    }

}
