package com.whylog.server.domain.git.entity;

import com.whylog.server.domain.git.dto.GitRequest;
import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(
        name = "Commits",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_commits_repository_hash",
                        columnNames = {"repository_id", "hash"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Commit extends BaseEntity {

    @Id
    @Column(name = "commit_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @Column(nullable = false)
    private String hash;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "author_name", length = 50, nullable = false)
    private String authorName;

    @Column(name = "author_email", length = 50, nullable = false)
    private String authorEmail;

    @Column(name = "author_profile_image", length = 255, nullable = false)
    private String authorProfileImage;

    @Column(name = "datetime", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "added_lines", nullable = false)
    private Integer addedLines;

    @Column(name = "deleted_lines", nullable = false)
    private Integer deletedLines;

    @OneToOne(mappedBy = "commit", cascade = CascadeType.ALL, orphanRemoval = true)
    private CommitAnalysis commitAnalysis;

    @OneToMany(mappedBy = "commit", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CommitConnection> commitConnections = new ArrayList<>();

    public static Commit create(GitRequest.CommitCreateDTO dto, Repository repository) {
        Commit commit = new Commit();
        commit.hash = dto.getHash();
        commit.message = dto.getMessage();
        commit.authorName = dto.getAuthorName();
        commit.authorEmail = dto.getAuthorEmail();
        commit.authorProfileImage = dto.getAuthorProfileImage();
        commit.dateTime = dto.getDateTime();
        commit.addedLines = dto.getAddedLines();
        commit.deletedLines = dto.getDeletedLines();
        commit.repository = repository;
        return commit;
    }
}
