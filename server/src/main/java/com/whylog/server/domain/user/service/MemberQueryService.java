package com.whylog.server.domain.user.service;

import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.domain.team.repository.TeamMemberRepository;
import com.whylog.server.domain.user.dto.MemberResponse;
import com.whylog.server.global.external.s3.S3Client;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberQueryService {

    private final TeamMemberRepository teamMemberRepository;
    private final S3Client s3Client;

    @Transactional(readOnly = true)
    public List<MemberResponse.TeamListResponseDTO> getTeams(Long memberId) {
        return teamMemberRepository.findActiveTeamsByMemberId(memberId).stream()
                .map(teamMember -> {
                    Team team = teamMember.getTeam();
                    return MemberResponse.TeamListResponseDTO.builder()
                            .teamId(team.getId())
                            .name(team.getName())
                            .teamImage(s3Client.getFileUrl(team.getImage()))
                            .build();
                })
                .toList();
    }
}
