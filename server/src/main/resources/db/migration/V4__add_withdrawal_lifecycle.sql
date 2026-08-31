alter table member
    add column purge_at datetime(6) null;


create index idx_member_account_status_purge_at
    on member (account_status, purge_at, member_id);
