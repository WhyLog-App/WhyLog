package com.whylog.server.domain.meeting.entity;

import com.whylog.server.domain.decision.entity.Decision;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
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

//    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<MeetingMember> meetingMembers = new ArrayList<>();
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
