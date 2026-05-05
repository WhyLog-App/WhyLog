package com.whylog.server.domain.decision.entity;

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
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Application")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application extends BaseEntity {

    @Id
    @Column(name = "application_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Column(name = "name")
    private String name;

//    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<ApplicationTimeline> applicationTimelines = new ArrayList<>();
//
//    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<ApplicationCommits> applicationCommits = new ArrayList<>();
//
//    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<ApplicationBase> applicationBases = new ArrayList<>();

    public static Application create(Decision decision, String name) {
        Application application = new Application();
        application.decision = decision;
        application.name = name;
        return application;
    }
}
