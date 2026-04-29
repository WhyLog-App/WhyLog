package com.whylog.server.domain.meeting.entity;

import com.whylog.server.global.entity.BaseEntity;
import com.whylog.server.global.util.json.StringListJsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Getter
@Table(name = "Meeting_Analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingAnalysis extends BaseEntity {

    @Id
    @Column(name = "meeting_analysis_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false, unique = true)
    private Meeting meeting;

    @Column(name = "meeting_title")
    private String meetingTitle;

    @Column(name = "meeting_purpose")
    private String meetingPurpose;

    @Column(name = "meeting_duration")
    private String meetingDuration;

    @Column(name = "analysis_content", columnDefinition = "LONGTEXT")
    private String analysisContent;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "topics", columnDefinition = "LONGTEXT")
    private List<String> topics;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "core_context", columnDefinition = "LONGTEXT")
    private List<String> coreContext;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "application_titles", columnDefinition = "LONGTEXT")
    private List<String> applicationTitles;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "application_reasons", columnDefinition = "LONGTEXT")
    private List<String> applicationReasons;

    public static MeetingAnalysis create(Meeting meeting, MeetingAnalysisPayload payload) {
        MeetingAnalysis meetingAnalysis = new MeetingAnalysis();
        meetingAnalysis.meeting = meeting;
        meetingAnalysis.apply(payload);
        return meetingAnalysis;
    }

    public void updateAnalysis(MeetingAnalysisPayload payload) {
        apply(payload);
    }

    private void apply(MeetingAnalysisPayload payload) {
        if (payload == null) {
            return;
        }

        this.meetingTitle = payload.meetingTitle();
        this.meetingPurpose = payload.meetingPurpose();
        this.meetingDuration = payload.meetingDuration();
        this.analysisContent = payload.analysisContent();
        this.topics = payload.topics();
        this.coreContext = payload.coreContext();
        this.applicationTitles = payload.applicationTitles();
        this.applicationReasons = payload.applicationReasons();
    }

    public record MeetingAnalysisPayload(
            String meetingTitle,
            String meetingPurpose,
            String meetingDuration,
            String analysisContent,
            List<String> topics,
            List<String> coreContext,
            List<String> applicationTitles,
            List<String> applicationReasons
    ) {
    }
}
