package com.whylog.server.domain.team.service;

import com.whylog.server.domain.team.dto.TeamRequest;
import com.whylog.server.domain.team.dto.TeamResponse;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.domain.team.entity.TeamMember;
import com.whylog.server.domain.team.enums.TeamRole;
import com.whylog.server.domain.team.exception.TeamErrorCode;
import com.whylog.server.domain.team.repository.TeamMemberRepository;
import com.whylog.server.domain.team.repository.TeamRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamCommandService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberUseCase memberUseCase;

    @Transactional
    public TeamResponse.TeamCreateResponseDTO createTeam(Long memberId, TeamRequest.TeamCreateDTO request){

        // 팀명 이미 존재하면 예외 발생
        if(teamRepository.existsByName(request.getName())){
            throw new ErrorHandler(TeamErrorCode.TEAM_NAME_ALREADY_EXISTS);
        }

        // 팀명 길이 체크
        if (request.getName().isEmpty() || request.getName().length() > 50) {
            throw new ErrorHandler(TeamErrorCode.TEAM_NAME_LENGTH);
        }

        // 팀 생성 및 저장
        Team team = Team.create(request);
        teamRepository.save(team);

        // 팀원으로 등록
        Member member = memberUseCase.findMemberById(memberId);
        addMember(team, member, TeamRole.OWNER);

        return TeamResponse.TeamCreateResponseDTO.builder()
                .teamId(team.getId())
                .name(team.getName())
                .build();
    }

    private void addMember(Team team, Member member, TeamRole role){
        TeamMember teamMember = TeamMember.create(team, member, role);
        teamMemberRepository.save(teamMember);
    }

}
