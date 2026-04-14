package com.whylog.server.domain.team.entity;

import com.whylog.server.domain.team.dto.TeamRequest;
import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Team")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity {

    @Id
    @Column(name = "team_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @Column(name = "image")
    private String image; // s3 key

    @Builder
    private Team(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public static Team create(TeamRequest.TeamCreateDTO dto, String image) {
        return Team.builder()
                .name(dto.getName())
                .image(image)
                .build();
    }

//    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<TeamMember> teamMembers = new ArrayList<>();
//
//    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<com.whylog.server.domain.meeting.entity.Meeting> meetings = new ArrayList<>();
//
//    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<com.whylog.server.domain.git.entity.Repository> repositories = new ArrayList<>();
}
