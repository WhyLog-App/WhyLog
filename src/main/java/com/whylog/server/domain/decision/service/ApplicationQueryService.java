package com.whylog.server.domain.decision.service;

import com.whylog.server.domain.decision.dto.ApplicationResponse;
import com.whylog.server.domain.decision.entity.Application;
import com.whylog.server.domain.decision.entity.ApplicationBase;
import com.whylog.server.domain.decision.entity.ApplicationCommits;
import com.whylog.server.domain.decision.entity.ApplicationTimeline;
import com.whylog.server.domain.decision.exception.ApplicationNotFoundException;
import com.whylog.server.domain.decision.repository.ApplicationBaseRepository;
import com.whylog.server.domain.decision.repository.ApplicationCommitsRepository;
import com.whylog.server.domain.decision.repository.ApplicationRepository;
import com.whylog.server.domain.decision.repository.ApplicationTimelineRepository;
import com.whylog.server.domain.git.entity.Commit;
import com.whylog.server.domain.git.entity.CommitConnection;
import com.whylog.server.domain.git.exception.GitErrorCode;
import com.whylog.server.domain.git.repository.CommitConnectionRepository;
import com.whylog.server.domain.git.repository.CommitRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationQueryService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationBaseRepository applicationBaseRepository;
    private final ApplicationTimelineRepository applicationTimelineRepository;
    private final ApplicationCommitsRepository applicationCommitsRepository;
    private final CommitRepository commitRepository;
    private final CommitConnectionRepository commitConnectionRepository;
    private final MemberUseCase memberUseCase;

    // 적용사항 상세 조회에 필요한 제목, 타임라인, 원문 맥락, 결정근거를 조회
    public ApplicationResponse.ApplicationDetailDTO getApplicationDetail(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(ApplicationNotFoundException::new);

        // 적용사항에 연결된 근거/타임라인 원본 엔티티를 각각 조회
        List<ApplicationBase> applicationBases = applicationBaseRepository.findByApplicationId(applicationId);
        List<ApplicationTimeline> applicationTimelines = applicationTimelineRepository.findByApplicationId(applicationId);
        Map<Long, Member> membersById = findMembersById(applicationTimelines);

        return ApplicationResponse.ApplicationDetailDTO.builder()
                .applicationId(application.getId())
                .name(application.getName())
                .decisionReasons(toDecisionReasonItems(applicationBases))
                .decisionTimelines(toDecisionTimelineItems(applicationTimelines))
                .decisionContexts(toDecisionContextItems(applicationTimelines, membersById))
                .decisionReasonCount(applicationBases.size())
                .build();
    }

    // 적용사항에 연결된 커밋 목록을 조회
    public ApplicationResponse.ConnectedCommitListDTO getConnectedCommits(Long applicationId) {
        // 적용사항 존재 여부검증
        applicationRepository.findById(applicationId)
                .orElseThrow(ApplicationNotFoundException::new);

        // 적용사항에 사용자가 연결한 커밋 목록을 조회
        List<CommitConnection> commitConnections = commitConnectionRepository.findByApplicationId(applicationId);

        // 연결된 커밋 엔티티를 응답 DTO로 변환
        List<ApplicationResponse.ConnectedCommitDTO> commits = commitConnections.stream()
                .map(commitConnection -> ApplicationResponse.ConnectedCommitDTO.builder()
                        .commitId(commitConnection.getCommit().getId())
                        .repositoryName(commitConnection.getCommit().getRepository().getName())
                        .commitHash(commitConnection.getCommit().getHash())
                        .message(commitConnection.getCommit().getMessage())
                        .committedDate(commitConnection.getCommit().getDateTime())
                .build())
                .toList();

        return ApplicationResponse.ConnectedCommitListDTO.builder()
                .commitCount(commits.size())
                .commits(commits)
                .build();
    }

    // 적용사항의 적용현황 요약 정보를 조회
    public ApplicationResponse.ApplicationStatusDTO getApplicationStatus(Long applicationId) {
        // 적용사항 존재 여부 검증
        applicationRepository.findById(applicationId)
                .orElseThrow(ApplicationNotFoundException::new);

        // 적용사항에 사용자가 연결한 커밋 목록을 조회
        List<CommitConnection> commitConnections = commitConnectionRepository.findByApplicationId(applicationId);

        // 연결된 커밋 목록을 적용현황 응답 형식으로 변환
        List<ApplicationResponse.ApplicationBaseItemDTO> commits = commitConnections.stream()
                .map(commitConnection -> ApplicationResponse.ApplicationBaseItemDTO.builder()
                        .commitHash(commitConnection.getCommit().getHash())
                        .commitMessage(commitConnection.getCommit().getMessage())
                        .build())
                .toList();

        return ApplicationResponse.ApplicationStatusDTO.builder()
                .commitCount(commits.size())
                .commits(commits)
                .build();
    }

    // 적용사항에 추천된 커밋 목록을 조회
    public List<ApplicationResponse.RecommendedCommitDTO> getRecommendedCommits(Long applicationId) {
        // 적용사항 존재 여부 검증
        applicationRepository.findById(applicationId)
                .orElseThrow(ApplicationNotFoundException::new);

        // 적용사항과 연결된 추천 커밋 원본 정보를 조회
        List<ApplicationCommits> applicationCommits = applicationCommitsRepository.findByApplicationId(applicationId);

        // 추천 원본이 들고 있는 commitId 목록으로 실제 커밋 정보를 조회
        Map<Long, Commit> commitsById = findCommitsById(applicationCommits);

        // 추천 원본과 커밋 정보를 합쳐 응답 DTO로 변환
        return applicationCommits.stream()
                .filter(applicationCommit -> commitsById.containsKey(applicationCommit.getDecisionCommits().getCommitId()))
                .map(applicationCommit -> toRecommendedCommitDTO(applicationCommit, commitsById))
                .toList();
    }

    // 추천 커밋 ID 목록에 해당하는 커밋 정보를 조회
    private Map<Long, Commit> findCommitsById(List<ApplicationCommits> applicationCommits) {
        // 추천 커밋 원본에서 커밋 ID 목록을 추출
        List<Long> commitIds = applicationCommits.stream()
                .map(applicationCommit -> applicationCommit.getDecisionCommits().getCommitId())
                .toList();

        if (commitIds.isEmpty()) {
            return Map.of();
        }

        //응답에 필요한 커밋 정보와 레포 이름을 함께 조회
        Map<Long, Commit> commitsById = commitRepository.findAllWithRepositoryByIdIn(commitIds).stream()
                .collect(Collectors.toMap(Commit::getId, Function.identity()));

        // 추천 원본이 존재하지 않는 커밋을 참조하는 경우
        if (commitsById.size() != commitIds.size()) {
            throw new ErrorHandler(GitErrorCode.COMMIT_NOT_FOUND);
        }

        return commitsById;
    }

    // 추천 커밋 연결 정보를 응답 DTO로 변환
    private ApplicationResponse.RecommendedCommitDTO toRecommendedCommitDTO(ApplicationCommits applicationCommit,
                                                                            Map<Long, Commit> commitsById) {
        Commit commit = commitsById.get(applicationCommit.getDecisionCommits().getCommitId());

        return ApplicationResponse.RecommendedCommitDTO.builder()
                .repositoryName(commit.getRepository().getName())
                .commitId(String.valueOf(commit.getId()))
                .commitHash(commit.getHash())
                .message(commit.getMessage())
                .reason(applicationCommit.getDecisionCommits().getReason())
                .build();
    }

    // 연결 테이블을 따라 적용사항에 속한 결정근거 목록을 응답 DTO로 변환
    private List<ApplicationResponse.DecisionReasonItemDTO> toDecisionReasonItems(List<ApplicationBase> applicationBases) {
        return applicationBases.stream()
                .map(applicationBase -> ApplicationResponse.DecisionReasonItemDTO.builder()
                        .reasonId(String.valueOf(applicationBase.getDecisionBase().getId()))
                        .title(applicationBase.getDecisionBase().getContent())
                        .build())
                .toList();
    }

    // 타임라인 요약 정보
    private List<ApplicationResponse.DecisionTimelineItemDTO> toDecisionTimelineItems(List<ApplicationTimeline> applicationTimelines) {
        return applicationTimelines.stream()
                .map(applicationTimeline -> ApplicationResponse.DecisionTimelineItemDTO.builder()
                        .time(applicationTimeline.getDecisionTimeline().getTimestamp())
                        .step(applicationTimeline.getDecisionTimeline().getStep())
                        .content(applicationTimeline.getDecisionTimeline().getContent())
                        .build())
                .toList();
    }

    // 원문 맥락은 발화자 정보와 원문 발화를 함께 내려줌
    private List<ApplicationResponse.DecisionContextItemDTO> toDecisionContextItems(List<ApplicationTimeline> applicationTimelines,
                                                                                    Map<Long, Member> membersById) {
        return applicationTimelines.stream()
                .map(applicationTimeline -> {
                    Long memberId = applicationTimeline.getDecisionTimeline().getMemberId();
                    Member member = memberId != null ? membersById.get(memberId) : null;

                    return ApplicationResponse.DecisionContextItemDTO.builder()
                            .time(applicationTimeline.getDecisionTimeline().getTimestamp())
                            .memberId(memberId)
                            .memberName(member != null ? member.getName() : null)
                            .profileImage(memberUseCase.getProfileImageUrl(member) )
                            .dialogueContent(applicationTimeline.getDecisionTimeline().getUtterance())
                            .build();
                })
                .toList();
    }

    // 타임라인에 포함된 발화자들을 한 번에 조회해 memberId 기준 맵으로 구성한다.
    private Map<Long, Member> findMembersById(List<ApplicationTimeline> applicationTimelines) {
        List<Long> memberIds = applicationTimelines.stream()
                .map(applicationTimeline -> applicationTimeline.getDecisionTimeline().getMemberId())
                .filter(memberId -> memberId != null)
                .distinct()
                .toList();

        return memberUseCase.findMembersByIds(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
    }
}
