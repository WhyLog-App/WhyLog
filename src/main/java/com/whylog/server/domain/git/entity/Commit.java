package com.whylog.server.domain.git.entity;

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
@Table(name = "Commits")
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

    @Column(nullable = false)
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

//    @OneToMany(mappedBy = "commit", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<ChangedFile> changedFiles = new ArrayList<>();
//
//    @OneToMany(mappedBy = "commit", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<CommitAnalysis> commitAnalyses = new ArrayList<>();
//
//    @OneToMany(mappedBy = "commit", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<CommitConnection> commitConnections = new ArrayList<>();

    public static Commit create(String hash, String message, String authorName, String authorEmail,
                                String authorProfileImage, LocalDateTime dateTime, Integer addedLines,
                                Integer deletedLines, Repository repository) {
        Commit commit = new Commit();
        commit.hash = hash;
        commit.message = message;
        commit.authorName = authorName;
        commit.authorEmail = authorEmail;
        commit.authorProfileImage = authorProfileImage;
        commit.dateTime = dateTime;
        commit.addedLines = addedLines;
        commit.deletedLines = deletedLines;
        commit.repository = repository;
        return commit;
    }
}
