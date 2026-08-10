create table member (
    member_id bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    name varchar(50) not null,
    email varchar(50) not null,
    password varchar(255) not null,
    profile_image varchar(255),
    role enum ('USER') not null,
    github_access_token varchar(500),
    primary key (member_id)
) engine=InnoDB;

create table team (
    team_id bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    name varchar(50) not null,
    image varchar(255),
    primary key (team_id)
) engine=InnoDB;

create table team_member (
    team_id bigint not null,
    member_id bigint not null,
    created_at datetime(6),
    updated_at datetime(6),
    is_active bit,
    role enum ('MEMBER','OWNER'),
    primary key (team_id, member_id)
) engine=InnoDB;

create table repository (
    repository_id bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    name varchar(255) not null,
    url varchar(500) not null,
    last_synced_at datetime(6),
    team_id bigint not null,
    primary key (repository_id)
) engine=InnoDB;

create table commits (
    commit_id bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    repository_id bigint not null,
    hash varchar(255) not null,
    message TEXT not null,
    author_name varchar(50) not null,
    author_email varchar(50) not null,
    author_profile_image varchar(255) not null,
    datetime datetime(6) not null,
    added_lines integer not null,
    deleted_lines integer not null,
    primary key (commit_id)
) engine=InnoDB;

create table changed_file (
    changed_file_id bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    commit_id bigint not null,
    file_name varchar(255) not null,
    primary key (changed_file_id)
) engine=InnoDB;

create table commit_analysis (
    commit_Analysis_id bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    commit_id bigint not null,
    summary TEXT,
    embedding_ready bit not null,
    primary key (commit_Analysis_id)
) engine=InnoDB;

create table meeting (
    meeting_id bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    team_id bigint not null,
    name varchar(50),
    start_date_time datetime(6) not null,
    end_date_time datetime(6),
    audio_key varchar(255),
    audio_egress_id varchar(100),
    is_normally_ended bit not null,
    primary key (meeting_id)
) engine=InnoDB;

create table meeting_member (
    meeting_id bigint not null,
    member_id bigint not null,
    created_at datetime(6),
    updated_at datetime(6),
    role enum ('GENERAL','OWNER'),
    primary key (meeting_id, member_id)
) engine=InnoDB;

create table meeting_analysis (
    meeting_analysis_id bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    meeting_id bigint not null,
    meeting_title varchar(255),
    meeting_purpose varchar(255),
    meeting_duration varchar(255),
    analysis_content LONGTEXT,
    topics LONGTEXT,
    core_context LONGTEXT,
    application_titles LONGTEXT,
    application_reasons LONGTEXT,
    primary key (meeting_analysis_id)
) engine=InnoDB;

create table dialogue (
    dialogue_id bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    meeting_id bigint not null,
    member_id bigint not null,
    content TEXT not null,
    speech_datetime datetime(6) not null,
    primary key (dialogue_id)
) engine=InnoDB;

create table decision (
    decision_id bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    meeting_id bigint not null,
    is_created bit,
    reliability_score integer,
    primary key (decision_id)
) engine=InnoDB;

create table decision_base (
    decision_base_pk bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    decision_id bigint not null,
    content TEXT,
    primary key (decision_base_pk)
) engine=InnoDB;

create table decision_commits (
    decision_commits_pk bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    decision_id bigint not null,
    commit_id bigint not null,
    primary key (decision_commits_pk)
) engine=InnoDB;

create table decision_timeline (
    decision_timeline_pk bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    decision_id bigint not null,
    timestamp varchar(30),
    step varchar(10),
    content TEXT,
    member_id bigint,
    utterance TEXT,
    primary key (decision_timeline_pk)
) engine=InnoDB;

create table application (
    application_id bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    decision_id bigint not null,
    name varchar(255),
    primary key (application_id)
) engine=InnoDB;

create table application_base (
    application_id bigint not null,
    decision_base_pk bigint not null,
    created_at datetime(6),
    updated_at datetime(6),
    primary key (application_id, decision_base_pk)
) engine=InnoDB;

create table application_commits (
    application_id bigint not null,
    decision_commits_pk bigint not null,
    created_at datetime(6),
    updated_at datetime(6),
    reason TEXT,
    confidence integer,
    primary key (application_id, decision_commits_pk)
) engine=InnoDB;

create table application_timeline (
    application_id bigint not null,
    decision_timeline_pk bigint not null,
    created_at datetime(6),
    updated_at datetime(6),
    primary key (application_id, decision_timeline_pk)
) engine=InnoDB;

create table commit_connection (
    application_id bigint not null,
    commit_id bigint not null,
    created_at datetime(6),
    updated_at datetime(6),
    primary key (application_id, commit_id)
) engine=InnoDB;

alter table member
    add constraint uk_member_email unique (email);

alter table team
    add constraint uk_team_name unique (name);

alter table commits
    add constraint uk_commits_repository_hash unique (repository_id, hash);

alter table commit_analysis
    add constraint uk_commit_analysis_commit unique (commit_id);

alter table decision
    add constraint uk_decision_meeting unique (meeting_id);

alter table decision_commits
    add constraint uk_decision_commits_decision_commit unique (decision_id, commit_id);

alter table meeting_analysis
    add constraint uk_meeting_analysis_meeting unique (meeting_id);

alter table team_member
    add constraint fk_team_member_team foreign key (team_id) references team (team_id);
alter table team_member
    add constraint fk_team_member_member foreign key (member_id) references member (member_id);

alter table repository
    add constraint fk_repository_team foreign key (team_id) references team (team_id);

alter table commits
    add constraint fk_commits_repository foreign key (repository_id) references repository (repository_id);

alter table changed_file
    add constraint fk_changed_file_commit foreign key (commit_id) references commits (commit_id);

alter table commit_analysis
    add constraint fk_commit_analysis_commit foreign key (commit_id) references commits (commit_id);

alter table meeting
    add constraint fk_meeting_team foreign key (team_id) references team (team_id);

alter table meeting_member
    add constraint fk_meeting_member_meeting foreign key (meeting_id) references meeting (meeting_id);
alter table meeting_member
    add constraint fk_meeting_member_member foreign key (member_id) references member (member_id);

alter table meeting_analysis
    add constraint fk_meeting_analysis_meeting foreign key (meeting_id) references meeting (meeting_id);

alter table dialogue
    add constraint fk_dialogue_meeting foreign key (meeting_id) references meeting (meeting_id);
alter table dialogue
    add constraint fk_dialogue_member foreign key (member_id) references member (member_id);

alter table decision
    add constraint fk_decision_meeting foreign key (meeting_id) references meeting (meeting_id);

alter table decision_base
    add constraint fk_decision_base_decision foreign key (decision_id) references decision (decision_id);

alter table decision_commits
    add constraint fk_decision_commits_decision foreign key (decision_id) references decision (decision_id);

alter table decision_timeline
    add constraint fk_decision_timeline_decision foreign key (decision_id) references decision (decision_id);

alter table application
    add constraint fk_application_decision foreign key (decision_id) references decision (decision_id);

alter table application_base
    add constraint fk_application_base_application foreign key (application_id) references application (application_id);
alter table application_base
    add constraint fk_application_base_decision_base foreign key (decision_base_pk) references decision_base (decision_base_pk);

alter table application_commits
    add constraint fk_application_commits_application foreign key (application_id) references application (application_id);
alter table application_commits
    add constraint fk_application_commits_decision_commits foreign key (decision_commits_pk) references decision_commits (decision_commits_pk);

alter table application_timeline
    add constraint fk_application_timeline_application foreign key (application_id) references application (application_id);
alter table application_timeline
    add constraint fk_application_timeline_decision_timeline foreign key (decision_timeline_pk) references decision_timeline (decision_timeline_pk);

alter table commit_connection
    add constraint fk_commit_connection_application foreign key (application_id) references application (application_id);
alter table commit_connection
    add constraint fk_commit_connection_commit foreign key (commit_id) references commits (commit_id);
