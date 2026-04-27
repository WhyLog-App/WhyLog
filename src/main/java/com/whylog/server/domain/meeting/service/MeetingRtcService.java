package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.enums.MeetingRole;
import com.whylog.server.domain.meeting.exception.MeetingNotFoundException;
import com.whylog.server.domain.meeting.repository.MeetingMemberRepository;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class MeetingRtcService {

    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MemberUseCase memberUseCase;
    private final String liveKitUrl;
    private final String liveKitApiKey;
    private final String liveKitApiSecret;
    private final long liveKitTokenExpireTime;

    public MeetingRtcService(
            MeetingRepository meetingRepository,
            MeetingMemberRepository meetingMemberRepository,
            MemberUseCase memberUseCase,
            @Value("${livekit.url}") @NotBlank String liveKitUrl,
            @Value("${livekit.api-key}") @NotBlank String liveKitApiKey,
            @Value("${livekit.api-secret}") @NotBlank String liveKitApiSecret,
            @Value("${livekit.token-expire-time}") long liveKitTokenExpireTime
    ) {
        this.meetingRepository = meetingRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.memberUseCase = memberUseCase;
        this.liveKitUrl = liveKitUrl;
        this.liveKitApiKey = liveKitApiKey;
        this.liveKitApiSecret = liveKitApiSecret;
        this.liveKitTokenExpireTime = liveKitTokenExpireTime;
    }

    @Transactional
    public MeetingResponse.MeetingRtcTokenDTO issueRtcToken(Long memberId, Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(MeetingNotFoundException::new);

        Member member = memberUseCase.findMemberById(memberId);
        ensureMeetingParticipant(meeting, member);
        String roomName = buildRoomName(meeting);
        String identity = String.valueOf(member.getId());
        String token = createJoinToken(identity, member.getName(), roomName);

        return MeetingResponse.MeetingRtcTokenDTO.builder()
                .meetingId(meetingId)
                .roomName(roomName)
                .serverUrl(liveKitUrl)
                .token(token)
                .build();
    }

    private void ensureMeetingParticipant(Meeting meeting, Member member) {
        if (meetingMemberRepository.existsByMemberIdAndMeetingId(member.getId(), meeting.getId())) {
            return;
        }
        meetingMemberRepository.save(MeetingMember.create(meeting, member, MeetingRole.GENERAL));
    }

    private String buildRoomName(Meeting meeting) {
        return "meeting-" + meeting.getId();
    }

    private String createJoinToken(String identity, String name, String roomName) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + liveKitTokenExpireTime);

        Map<String, Object> videoGrant = Map.of(
                "roomJoin", true,
                "room", roomName,
                "canPublish", true,
                "canSubscribe", true,
                "canPublishData", true
        );

        return Jwts.builder()
                .setIssuer(liveKitApiKey)
                .setSubject(identity)
                .setIssuedAt(now)
                .setNotBefore(now)
                .setExpiration(expiration)
                .claim("name", name)
                .claim("video", videoGrant)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(liveKitApiSecret.getBytes(StandardCharsets.UTF_8));
    }
}
