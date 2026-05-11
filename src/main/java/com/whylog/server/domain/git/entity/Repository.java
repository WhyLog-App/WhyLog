package com.whylog.server.domain.git.entity;

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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Repository")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Repository extends BaseEntity {

    @Id
    @Column(name = "repository_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(length = 500, nullable = false)
    private String url;

    @Column(nullable = true)
    private LocalDateTime lastSyncedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Commit> commits = new ArrayList<>();

    public static Repository create(String name, String url, Team team) {
        Repository repository = new Repository();
        repository.name = name;
        repository.url = url;
        repository.team = team;
        return repository;
    }

    /**
     * 마지막 동기화 시간을 업데이트합니다.
     */
    public void updateLastSyncedAt(LocalDateTime syncedAt) {
        this.lastSyncedAt = syncedAt;
    }
}
