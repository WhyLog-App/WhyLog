alter table member
    add column profile_visibility enum ('PUBLIC','PRIVATE') not null default 'PUBLIC';
