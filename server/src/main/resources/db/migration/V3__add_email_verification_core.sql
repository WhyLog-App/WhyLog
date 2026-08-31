create table email_verification_code (
    member_id bigint not null,
    created_at datetime(6),
    updated_at datetime(6),
    code_hmac varchar(64) not null,
    expires_at datetime(6) not null,
    last_issued_at datetime(6) not null,
    failed_attempts integer not null,
    primary key (member_id)
) engine=InnoDB;

alter table email_verification_code
    add constraint fk_email_verification_code_member
        foreign key (member_id) references member (member_id)
        on delete cascade;
