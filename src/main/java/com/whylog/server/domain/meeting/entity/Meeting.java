package com.whylog.server.domain.meeting.entity;

import com.whylog.server.domain.meeting.dto.MeetingRequest;
import com.whylog.server.domain.meeting.enums.MeetingStatus;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Meeting")
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

    @Builder
    private Meeting(String name, Team team) {
        this.name = name;
        this.team = team;
        this.startDateTime = LocalDateTime.now();
        this.endDateTime = null;
    }

    public static Meeting create(MeetingRequest.MeetingCreateDTO dto, Team team) {
        return Meeting.builder()
                .name(dto.getName())
                .team(team)
                .build();
    }

    public MeetingStatus getStatus(){
        return this.endDateTime == null ? MeetingStatus.ONGOING : MeetingStatus.COMPLETED;
    }

    public boolean isOngoing(){
        return this.endDateTime == null;
    }

    public LocalDateTime endMeeting() {
        this.endDateTime = LocalDateTime.now();
        return this.endDateTime;
    }

    public Long getDuration() {
        if(this.endDateTime == null) return null;
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
//    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final MeetingAnalysis meetingAnalyses;
//
//    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<Dialogue> dialogues = new ArrayList<>();
//
//    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Decision decision;
}
