package com.whylog.server.domain.meeting.entity;

import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Dialogue")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dialogue extends BaseEntity {

    @Id
    @Column(name = "dialogue_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "speech_datetime", nullable = false)
    private LocalDateTime speechDateTime;

    public static Dialogue create(Meeting meeting, Member member, String content, LocalDateTime speechDateTime) {
        Dialogue dialogue = new Dialogue();
        dialogue.meeting = meeting;
        dialogue.member = member;
        dialogue.content = content;
        dialogue.speechDateTime = speechDateTime;
        return dialogue;
    }
}
