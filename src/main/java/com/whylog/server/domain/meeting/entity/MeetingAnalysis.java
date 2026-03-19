package com.whylog.server.domain.meeting.entity;

import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
