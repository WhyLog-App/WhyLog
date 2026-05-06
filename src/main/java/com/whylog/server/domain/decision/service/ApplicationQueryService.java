package com.whylog.server.domain.decision.service;

import com.whylog.server.domain.decision.dto.ApplicationResponse;
import com.whylog.server.domain.decision.entity.Application;
import com.whylog.server.domain.decision.entity.ApplicationBase;
import com.whylog.server.domain.decision.entity.ApplicationTimeline;
import com.whylog.server.domain.decision.exception.ApplicationNotFoundException;
import com.whylog.server.domain.decision.repository.ApplicationBaseRepository;
import com.whylog.server.domain.decision.repository.ApplicationRepository;
import com.whylog.server.domain.decision.repository.ApplicationTimelineRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationQueryService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationBaseRepository applicationBaseRepository;
    private final ApplicationTimelineRepository applicationTimelineRepository;
    private final MemberUseCase memberUseCase;

    // 적용사항 상세 조회에 필요한 제목, 타임라인, 원문 맥락, 결정근거를 조회
    @Transactional(readOnly = true)
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
                            .profileImage(member != null ? member.getProfileImage() : null)
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
