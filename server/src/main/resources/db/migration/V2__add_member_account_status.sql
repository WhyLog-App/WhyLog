alter table member
    add column account_status enum ('UNVERIFIED','ACTIVE','INACTIVE','WITHDRAW') not null default 'ACTIVE',
    add column email_verified_at datetime(6) null;

update member
set email_verified_at = coalesce(updated_at, created_at, current_timestamp(6))
where account_status = 'ACTIVE'
  and email_verified_at is null;
