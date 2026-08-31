package com.whylog.server.domain.user.service;

import com.whylog.server.domain.team.entity.TeamMember;
import com.whylog.server.domain.team.repository.TeamMemberRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberDisplayResolver {

    private static final String LEFT_MEMBER_NAME = "나간 사용자";
    private static final String WITHDRAWN_MEMBER_NAME = "탈퇴한 사용자";

    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;

    public Map<Long, DisplayMember> resolveByTeamMemberIds(Long teamId, List<Long> memberIds) {
        List<Long> distinctMemberIds =
                memberIds.stream().filter(id -> id != null).distinct().toList();
        if (distinctMemberIds.isEmpty()) {
            return Map.of();
        }
        return resolveByTeam(teamId, memberRepository.findAllById(distinctMemberIds));
    }

    public Map<Long, DisplayMember> resolveByTeam(Long teamId, List<Member> members) {
        List<Long> memberIds =
                members.stream()
                        .filter(member -> member != null && member.getId() != null)
                        .map(Member::getId)
                        .distinct()
                        .toList();
        if (memberIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, TeamMember> membershipsByMemberId =
                teamMemberRepository.findByTeamIdAndMemberIdIn(teamId, memberIds).stream()
                        .collect(
                                Collectors.toMap(
                                        teamMember -> teamMember.getMember().getId(),
                                        Function.identity()));

        return members.stream()
                .filter(member -> member != null && member.getId() != null)
                .collect(
                        Collectors.toMap(
                                Member::getId,
                                member ->
                                        resolve(
                                                member,
                                                membershipsByMemberId.get(member.getId()))));
    }

    public DisplayMember resolve(Member member, TeamMember teamMember) {
        if (member == null) {
            return DisplayMember.textOnly(null);
        }
        if (member.isWithdrawnForRead(LocalDateTime.now())) {
            return DisplayMember.textOnly(WITHDRAWN_MEMBER_NAME);
        }
        if (teamMember != null && Boolean.FALSE.equals(teamMember.getActive())) {
            return DisplayMember.textOnly(LEFT_MEMBER_NAME);
        }
        return new DisplayMember(member.getId(), member.getName(), member.getProfileImage());
    }

    public record DisplayMember(Long memberId, String name, String profileImageKey) {

        private static DisplayMember textOnly(String name) {
            return new DisplayMember(null, name, null);
        }
    }
}
