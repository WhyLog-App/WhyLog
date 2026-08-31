package com.whylog.server.domain.user.service;

import com.whylog.server.domain.decision.repository.DecisionRepository;
import com.whylog.server.domain.git.repository.RepositoryRepository;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.domain.team.repository.TeamMemberRepository;
import com.whylog.server.domain.user.dto.MemberResponse;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.enums.AccountStatus;
import com.whylog.server.domain.user.exception.MemberErrorCode;
import com.whylog.server.domain.user.repository.MemberRepository;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import com.whylog.server.global.external.s3.S3Client;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private static final int PARTICIPATING_PROJECT_PAGE_SIZE = 4;

    private final MemberRepository memberRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MeetingRepository meetingRepository;
    private final DecisionRepository decisionRepository;
    private final RepositoryRepository repositoryRepository;
    private final MemberProfileAccessPolicy memberProfileAccessPolicy;
    private final S3Client s3Client;

    public List<MemberResponse.TeamListResponseDTO> getTeams(Long memberId) {
        return teamMemberRepository.findActiveTeamsByMemberId(memberId).stream()
                .map(
                        teamMember -> {
                            Team team = teamMember.getTeam();
                            return MemberResponse.TeamListResponseDTO.builder()
                                    .teamId(team.getId())
                                    .name(team.getName())
                                    .teamImage(s3Client.getFileUrl(team.getImage()))
                                    .build();
                        })
                .toList();
    }

    public MemberResponse.MyInfoDTO getMyInfo(Long memberId) {
        Member member = findActiveMember(memberId);

        return MemberResponse.MyInfoDTO.builder()
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .profileImage(s3Client.getFileUrl(member.getProfileImage()))
                .participatingProjectCount(participatingProjectCount(memberId))
                .accountStatus(member.getAccountStatus())
                .profileVisibility(member.getProfileVisibility())
                .recentMeetings(recentMeetings(memberId))
                .recentDecisions(recentDecisions(memberId))
                .build();
    }

    public MemberResponse.ProfileDTO getProfile(Long viewerId, Long memberId) {
        Member member = findProfileMember(viewerId, memberId, false);
        LocalDateTime now = LocalDateTime.now();
        if (member.isWithdrawnForRead(now)) {
            return withdrawnProfile(member);
        }

        if (!memberProfileAccessPolicy.canViewActivity(viewerId, member)) {
            return privateProfile(member);
        }

        return MemberResponse.FullProfileDTO.builder()
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .profileImage(s3Client.getFileUrl(member.getProfileImage()))
                .participatingProjectCount(participatingProjectCount(memberId))
                .build();
    }

    public MemberResponse.ParticipatingProjectListResponseDTO getParticipatingProjects(
            Long viewerId, Long memberId, Long cursorId) {
        Member member = findProfileMember(viewerId, memberId, true);
        return MemberResponse.ParticipatingProjectListResponseDTO.from(
                participatingProjects(member.getId(), cursorId), cursorId);
    }

    private MemberResponse.PrivateProfileDTO privateProfile(Member member) {
        return MemberResponse.PrivateProfileDTO.builder()
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .profileImage(s3Client.getFileUrl(member.getProfileImage()))
                .build();
    }

    private MemberResponse.WithdrawnProfileDTO withdrawnProfile(Member member) {
        return MemberResponse.WithdrawnProfileDTO.builder()
                .memberId(member.getId())
                .name("탈퇴한 사용자")
                .email("")
                .profileImage(null)
                .build();
    }

    private Member findActiveMember(Long memberId) {
        return memberRepository
                .findByIdAndAccountStatus(memberId, AccountStatus.ACTIVE)
                .orElseThrow(() -> new ErrorHandler(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private Member findVisibleProfileMember(Long memberId) {
        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() -> new ErrorHandler(MemberErrorCode.MEMBER_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        if (member.getAccountStatus() == AccountStatus.ACTIVE
                || member.isWithdrawalGraceActive(now)
                || member.isWithdrawnForRead(now)) {
            return member;
        }
        throw new ErrorHandler(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    private Member findProfileMember(Long viewerId, Long memberId, boolean fullProfileRequired) {
        findActiveMember(viewerId);
        Member member = findVisibleProfileMember(memberId);
        if (fullProfileRequired
                && (member.isWithdrawnForRead(LocalDateTime.now())
                        || !memberProfileAccessPolicy.canViewActivity(viewerId, member))) {
            throw new ErrorHandler(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        return member;
    }

    private Long participatingProjectCount(Long memberId) {
        return teamMemberRepository.countActiveProjectsByMemberId(memberId);
    }

    private Slice<MemberResponse.ParticipatingProjectSummaryDTO> participatingProjects(
            Long memberId, Long cursorId) {
        Slice<TeamMemberRepository.ActiveProjectRow> projectRows =
                teamMemberRepository.findActiveProjectRowsByMemberId(
                        memberId, cursorId, participatingProjectPage());
        List<Long> projectIds =
                projectRows.stream().map(TeamMemberRepository.ActiveProjectRow::projectId).toList();
        if (projectIds.isEmpty()) {
            return projectRows.map(project -> participatingProject(project, Map.of(), Map.of()));
        }

        Map<Long, MeetingRepository.ProfileMeetingStatsRow> memberCompletedMeetingStats =
                meetingRepository.findMemberCompletedMeetingStats(memberId, projectIds).stream()
                        .collect(
                                Collectors.toMap(
                                        MeetingRepository.ProfileMeetingStatsRow::projectId,
                                        Function.identity()));
        Map<Long, RepositoryRepository.ProfileRepositoryStatsRow> repositoryStats =
                repositoryRepository.findProfileRepositoryStatsRows(projectIds).stream()
                        .collect(
                                Collectors.toMap(
                                        RepositoryRepository.ProfileRepositoryStatsRow::projectId,
                                        Function.identity()));

        return projectRows.map(
                project ->
                        participatingProject(
                                project, memberCompletedMeetingStats, repositoryStats));
    }

    private MemberResponse.ParticipatingProjectSummaryDTO participatingProject(
            TeamMemberRepository.ActiveProjectRow project,
            Map<Long, MeetingRepository.ProfileMeetingStatsRow> memberCompletedMeetingStats,
            Map<Long, RepositoryRepository.ProfileRepositoryStatsRow> repositoryStats) {
        MeetingRepository.ProfileMeetingStatsRow memberMeetingStats =
                memberCompletedMeetingStats.get(project.projectId());
        RepositoryRepository.ProfileRepositoryStatsRow projectRepositoryStats =
                repositoryStats.get(project.projectId());

        return MemberResponse.ParticipatingProjectSummaryDTO.builder()
                .projectId(project.projectId())
                .name(project.name())
                .image(s3Client.getFileUrl(project.image()))
                .memberCompletedMeetingCount(meetingCount(memberMeetingStats))
                .memberCompletedMeetingDurationSeconds(durationSeconds(memberMeetingStats))
                .projectStoredCommitCount(commitCount(projectRepositoryStats))
                .lastSyncedAt(lastSyncedAt(projectRepositoryStats))
                .build();
    }

    private List<MemberResponse.RecentMeetingDTO> recentMeetings(Long memberId) {
        return meetingRepository
                .findRecentCompletedMeetingRowsByMemberId(memberId, recentMeetingPage())
                .stream()
                .map(
                        row ->
                                MemberResponse.RecentMeetingDTO.builder()
                                        .meetingId(row.meetingId())
                                        .projectId(row.projectId())
                                        .projectName(row.projectName())
                                        .name(row.name())
                                        .endedAt(row.endedAt())
                                        .durationSeconds(row.durationSeconds())
                                        .build())
                .toList();
    }

    private List<MemberResponse.RecentDecisionDTO> recentDecisions(Long memberId) {
        return decisionRepository
                .findRecentDecisionRowsByMemberId(
                        memberId,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt", "id")))
                .stream()
                .map(
                        row ->
                                MemberResponse.RecentDecisionDTO.builder()
                                        .decisionId(row.decisionId())
                                        .projectId(row.projectId())
                                        .projectName(row.projectName())
                                        .name(row.name())
                                        .createdAt(row.createdAt())
                                        .build())
                .toList();
    }

    private Long meetingCount(MeetingRepository.ProfileMeetingStatsRow row) {
        return row == null ? 0L : row.meetingCount();
    }

    private PageRequest recentMeetingPage() {
        return PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "endDateTime", "id"));
    }

    private PageRequest participatingProjectPage() {
        return PageRequest.of(0, PARTICIPATING_PROJECT_PAGE_SIZE);
    }

    private Long durationSeconds(MeetingRepository.ProfileMeetingStatsRow row) {
        return row == null ? 0L : row.durationSeconds();
    }

    private Long commitCount(RepositoryRepository.ProfileRepositoryStatsRow row) {
        return row == null ? 0L : row.commitCount();
    }

    private LocalDateTime lastSyncedAt(RepositoryRepository.ProfileRepositoryStatsRow row) {
        if (row == null || row.repositoryCount() == 0) {
            return null;
        }
        return row.latestLastSyncedAt();
    }
}
