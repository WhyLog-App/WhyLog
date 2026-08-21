package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.entity.Dialogue;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.repository.DialogueRepository;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.meeting.socket.message.LiveMessageEntry;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 실시간 발화를 회의록으로 즉시 저장합니다. 오디오 녹음이 없어져 이 자막이 회의록의 유일한 원본입니다.
@Service
@Transactional
@RequiredArgsConstructor
public class MeetingDialogueCommandService {

    private final DialogueRepository dialogueRepository;
    private final MeetingRepository meetingRepository;
    private final MemberRepository memberRepository;

    /**
     * 발화 한 건을 저장합니다. 발화마다 호출되므로 연관 엔티티는 프록시로만 잡아 INSERT 한 번으로 끝냅니다. 회의·회원 식별자는 웹소켓 핸드셰이크에서 검증된 값이라
     * 존재 여부를 다시 조회하지 않습니다.
     *
     * <p>같은 타입 식별자를 나열해 받으면 호출부에서 뒤바꿔도 아무도 잡지 못하므로, 발화 한 건을 통째로 받습니다.
     */
    public void appendSpeech(LiveMessageEntry entry) {
        Meeting meeting = meetingRepository.getReferenceById(entry.meetingId());
        Member member = memberRepository.getReferenceById(entry.fromMemberId());
        dialogueRepository.save(Dialogue.create(meeting, member, entry.text(), entry.receivedAt()));
    }
}
