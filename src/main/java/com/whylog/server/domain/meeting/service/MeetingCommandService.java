package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.dto.MeetingRequest;
import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.enums.MeetingRole;
import com.whylog.server.domain.meeting.repository.MeetingMemberRepository;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.meeting.socket.MeetingSocketRoomService;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.domain.team.service.TeamUseCase;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import com.whylog.server.global.apiPayload.exception.ParameterRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingCommandService {

    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingRepository meetingRepository;

    private final MemberUseCase memberUseCase;
    private final TeamUseCase teamUseCase;

    private final MeetingSocketRoomService meetingSocketRoomService;

    /*
        회의를 생성합니다.

        회의를 생성하여 endDateTime이 null인 회의를 저장합니다.
        - endDateTime == null : 진행 중인 회의를 의미합니다.

        회의와 회의참여자 정보가 함께 저장됩니다.
     */
    @Transactional
    public MeetingResponse.MeetingCreateResponseDTO makeMeetingRoom(Long memberId, Long teamId, MeetingRequest.MeetingCreateDTO requestDTO){

        // null 체크
        if(teamId == null) throw new ParameterRequiredException();

        // API 호출한 유저 정보 조회
        Member member = memberUseCase.findMemberById(memberId);

        // 팀 조회
        Team team = teamUseCase.findTeamById(teamId);

        // Meeting, MeetingMember 같이 저장
        Meeting meeting = Meeting.create(requestDTO, team);
        Meeting savedMeeting = meetingRepository.save(meeting);
        MeetingMember meetingMember = MeetingMember.create(savedMeeting, member, MeetingRole.OWNER);
        meetingMemberRepository.save(meetingMember);

        // 실시간 회의 추가
        meetingSocketRoomService.createRoomIfAbsent(savedMeeting.getId()); // 현재 참여자 추가

        // TODO: 회의 분석 시작

        // dto 생성 후 반환
        return MeetingResponse.MeetingCreateResponseDTO.builder()
                .meetingId(savedMeeting.getId())
                .name(savedMeeting.getName())
                .startDateTime(savedMeeting.getStartDateTime())
                .build();
    }

}
