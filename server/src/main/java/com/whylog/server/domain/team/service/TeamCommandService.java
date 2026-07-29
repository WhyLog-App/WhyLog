package com.whylog.server.domain.team.service;

import com.whylog.server.domain.team.dto.TeamRequest;
import com.whylog.server.domain.team.dto.TeamResponse;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.domain.team.entity.TeamMember;
import com.whylog.server.domain.team.enums.TeamRole;
import com.whylog.server.domain.team.exception.TeamErrorCode;
import com.whylog.server.domain.team.repository.TeamMemberRepository;
import com.whylog.server.domain.team.repository.TeamRepository;
import com.whylog.server.domain.meeting.service.MeetingCleanupService;
import com.whylog.server.domain.meeting.socket.MeetingSocketRoomService;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import com.whylog.server.global.external.s3.ImageType;
import com.whylog.server.global.external.s3.S3Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TeamCommandService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MeetingCleanupService meetingCleanupService;
    private final MeetingSocketRoomService meetingSocketRoomService;
    private final MemberUseCase memberUseCase;
    private final TeamUseCase teamUseCase;
    private final S3Client s3Client;

    public TeamResponse.TeamCreateResponseDTO createTeam(Long memberId, TeamRequest.TeamCreateDTO request, MultipartFile image){

        // 팀명 이미 존재하면 예외 발생
        if(teamRepository.existsByName(request.getName())){
            throw new ErrorHandler(TeamErrorCode.TEAM_NAME_ALREADY_EXISTS);
        }

        // 팀명 길이 체크
        if (request.getName().isEmpty() || request.getName().length() > 50) {
            throw new ErrorHandler(TeamErrorCode.TEAM_NAME_LENGTH);
        }

        String imageKey = null;
        if( image != null && !image.isEmpty()){
            imageKey = s3Client.uploadFile(image, ImageType.TEAM_IMAGE);
        }

        // 팀 생성 및 저장
        Team team = Team.create(request, imageKey);
        teamRepository.save(team);

        // 팀원으로 등록
        Member member = memberUseCase.findMemberById(memberId);
        addMember(team, member, TeamRole.OWNER);

        return TeamResponse.TeamCreateResponseDTO.builder()
                .teamId(team.getId())
                .name(team.getName())
                .imageUrl(s3Client.getFileUrl(team.getImage()))
                .build();
    }

    public TeamResponse.InvitationResponseDTO invite(Long teamId, TeamRequest.InvitationDTO request){

        // 데이터 조회
        Member member = memberUseCase.findMemberByEmail(request.getMemberEmail());
        Team team = teamUseCase.findTeamById(teamId);

        // 이미 초대된 경우는 예외처리
        if (teamMemberRepository.existsByTeamIdAndMemberIdAndActiveTrue(team.getId(), member.getId())) {
            throw new ErrorHandler(TeamErrorCode.TEAM_MEMBER_ALREADY_EXISTS);
        }

        // 팀원 추가
        TeamMember teamMember = addMember(team, member, TeamRole.MEMBER);

        return TeamResponse.InvitationResponseDTO.builder()
                .teamId(teamMember.getTeam().getId())
                .memberEmail(teamMember.getMember().getEmail())
                .build();
    }

    public TeamResponse.TeamRemoveResponseDTO removeTeam(Long memberId, Long teamId) {
        Team team = teamUseCase.findTeamById(teamId);

        teamMemberRepository.findOwnerTeamMember(memberId, teamId, TeamRole.OWNER)
                .orElseThrow(() -> new ErrorHandler(TeamErrorCode.TEAM_NOT_OWNER));

        List<Long> meetingIds = meetingCleanupService.deleteByTeamId(teamId);
        teamRepository.delete(team);

        // 팀 제거로 인한 미팅 종료
        scheduleAfterCommit(() -> meetingIds.forEach(meetingSocketRoomService::closeRoom));
        scheduleAfterCommit(() -> {
            try {
                s3Client.deleteFile(team.getImage());
            } catch (RuntimeException exception) {
                log.warn("Failed to delete team image from S3: teamId={}, imageKey={}", teamId, team.getImage(), exception);
            }
        });

        return TeamResponse.TeamRemoveResponseDTO.builder()
                .isRemoved(true)
                .build();

    }

    private void scheduleAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }

        task.run();
    }

    private TeamMember addMember(Team team, Member member, TeamRole role){
        TeamMember teamMember = TeamMember.create(team, member, role);
        return teamMemberRepository.save(teamMember);
    }



}
