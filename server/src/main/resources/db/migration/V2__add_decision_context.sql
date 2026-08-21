create table decision_context (
    decision_context_pk bigint not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    decision_id bigint not null,
    timestamp varchar(30),
    content TEXT,
    member_id bigint,
    utterance TEXT,
    primary key (decision_context_pk)
) engine=InnoDB;

create table application_context (
    application_id bigint not null,
    decision_context_pk bigint not null,
    created_at datetime(6),
    updated_at datetime(6),
    primary key (application_id, decision_context_pk)
) engine=InnoDB;

alter table decision_context
    add constraint fk_decision_context_decision foreign key (decision_id) references decision (decision_id);

alter table application_context
    add constraint fk_application_context_application foreign key (application_id) references application (application_id);
alter table application_context
    add constraint fk_application_context_decision_context foreign key (decision_context_pk) references decision_context (decision_context_pk);
