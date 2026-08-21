package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.decision.repository.ApplicationBaseRepository;
import com.whylog.server.domain.decision.repository.ApplicationCommitsRepository;
import com.whylog.server.domain.decision.repository.ApplicationContextRepository;
import com.whylog.server.domain.decision.repository.ApplicationRepository;
import com.whylog.server.domain.decision.repository.ApplicationTimelineRepository;
import com.whylog.server.domain.decision.repository.DecisionBaseRepository;
import com.whylog.server.domain.decision.repository.DecisionCommitsRepository;
import com.whylog.server.domain.decision.repository.DecisionContextRepository;
import com.whylog.server.domain.decision.repository.DecisionRepository;
import com.whylog.server.domain.decision.repository.DecisionTimelineRepository;
import com.whylog.server.domain.meeting.repository.DialogueRepository;
import com.whylog.server.domain.meeting.repository.MeetingAnalysisRepository;
import com.whylog.server.domain.meeting.repository.MeetingMemberRepository;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingCleanupService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationBaseRepository applicationBaseRepository;
    private final ApplicationCommitsRepository applicationCommitsRepository;
    private final ApplicationContextRepository applicationContextRepository;
    private final ApplicationTimelineRepository applicationTimelineRepository;
    private final DecisionBaseRepository decisionBaseRepository;
    private final DecisionCommitsRepository decisionCommitsRepository;
    private final DecisionContextRepository decisionContextRepository;
    private final DecisionRepository decisionRepository;
    private final DecisionTimelineRepository decisionTimelineRepository;
    private final MeetingAnalysisRepository meetingAnalysisRepository;
    private final DialogueRepository dialogueRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingRepository meetingRepository;

    @Transactional
    public List<Long> deleteByTeamId(Long teamId) {
        List<Long> meetingIds = meetingRepository.findIdsByTeamId(teamId);
        deleteChildrenByTeamId(teamId, meetingIds);
        meetingRepository.deleteByTeamId(teamId);
        return meetingIds;
    }

    @Transactional
    public void deleteByMeetingId(Long meetingId) {
        deleteChildrenByMeetingId(meetingId);
        meetingRepository.deleteById(meetingId);
    }

    private void deleteChildrenByTeamId(Long teamId, List<Long> meetingIds) {
        applicationCommitsRepository.deleteByTeamId(teamId);
        applicationTimelineRepository.deleteByTeamId(teamId);
        applicationContextRepository.deleteByTeamId(teamId);
        applicationBaseRepository.deleteByTeamId(teamId);
        decisionCommitsRepository.deleteByTeamId(teamId);
        decisionTimelineRepository.deleteByTeamId(teamId);
        decisionContextRepository.deleteByTeamId(teamId);
        decisionBaseRepository.deleteByTeamId(teamId);
        applicationRepository.deleteByTeamId(teamId);
        decisionRepository.deleteByTeamId(teamId);
        meetingAnalysisRepository.deleteByTeamId(teamId);
        dialogueRepository.deleteByTeamId(teamId);
        meetingMemberRepository.deleteByTeamId(teamId);
    }

    private void deleteChildrenByMeetingId(Long meetingId) {
        applicationCommitsRepository.deleteByMeetingId(meetingId);
        applicationTimelineRepository.deleteByMeetingId(meetingId);
        applicationContextRepository.deleteByMeetingId(meetingId);
        applicationBaseRepository.deleteByMeetingId(meetingId);
        decisionCommitsRepository.deleteByMeetingId(meetingId);
        decisionTimelineRepository.deleteByMeetingId(meetingId);
        decisionContextRepository.deleteByMeetingId(meetingId);
        decisionBaseRepository.deleteByMeetingId(meetingId);
        applicationRepository.deleteByMeetingId(meetingId);
        decisionRepository.deleteByMeetingId(meetingId);
        meetingAnalysisRepository.deleteByMeetingId(meetingId);
        dialogueRepository.deleteByMeetingId(meetingId);
        meetingMemberRepository.deleteByMeetingId(meetingId);
    }
}
