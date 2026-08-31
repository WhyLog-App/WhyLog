create index idx_team_member_member_active_team
    on team_member (member_id, is_active, team_id);

create index idx_meeting_member_member_meeting
    on meeting_member (member_id, meeting_id);
