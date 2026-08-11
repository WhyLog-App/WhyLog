package com.whylog.server.domain.meeting.entity;

import com.whylog.server.domain.meeting.dto.MeetingRequest;
import com.whylog.server.domain.meeting.enums.MeetingStatus;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "meeting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting extends BaseEntity {

    @Id
    @Column(name = "meeting_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Setter
    @Column(name = "audio_key", length = 255)
    private String audioKey;

    @Setter
    @Column(name = "audio_egress_id", length = 100)
    private String audioEgressId;

    @Column(name = "is_normally_ended", nullable = false)
    private Boolean isNormallyEnded;

    @Builder
    private Meeting(String name, Team team) {
        this.name = name;
        this.team = team;
        this.isNormallyEnded = false;
        this.startDateTime = LocalDateTime.now();
        this.endDateTime = null;
    }

    public static Meeting create(MeetingRequest.MeetingCreateDTO dto, Team team) {
        return Meeting.builder().name(dto.getName()).team(team).build();
    }

    public MeetingStatus getStatus() {
        return this.endDateTime == null ? MeetingStatus.ONGOING : MeetingStatus.COMPLETED;
    }

    public boolean isOngoing() {
        return this.endDateTime == null;
    }

    public LocalDateTime endMeeting() {
        this.endDateTime = LocalDateTime.now();
        this.isNormallyEnded = true;
        return this.endDateTime;
    }

    public Long getDuration() {
        if (this.endDateTime == null) return null;
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    public String getElapse() {

        Duration duration = Duration.between(startDateTime, LocalDateTime.now());

        long seconds = duration.getSeconds();

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<MeetingMember> meetingMembers = new ArrayList<>();

    //
    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private MeetingAnalysis meetingAnalysis;

    //
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Dialogue> dialogues = new ArrayList<>();

    //
    //    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private Decision decision;

    public void attachMeetingAnalysis(MeetingAnalysis meetingAnalysis) {
        this.meetingAnalysis = meetingAnalysis;
    }

    public void addDialogue(Dialogue dialogue) {
        this.dialogues.add(dialogue);
    }
}
