package com.whylog.server.domain.meeting.dto;

import com.whylog.server.domain.meeting.entity.MeetingAnalysis;
import com.whylog.server.domain.meeting.enums.MeetingStatus;
import com.whylog.server.domain.meeting.socket.MeetingParticipant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

public class MeetingResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 목록 조회 응답")
    public static class MeetingListDTO {

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;

        @Schema(description = "회의 명", example = "백엔드 비상대책회의")
        private String name;

        @Schema(description = "회의 상태", example = "ONGOING")
        private MeetingStatus status;

        @Schema(description = "경과시간 (시:분:초)", example = "00:00:00")
        private String elapse;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 입장 응답")
    public static class MeetingJoinResponseDTO {

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;

        @Schema(description = "참여자 ID", example = "1")
        private Long memberId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 생성 응답")
    public static class MeetingCreateResponseDTO {

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;

        @Schema(description = "회의 명", example = "백엔드 비상대책회의")
        private String name;

        @Schema(description = "시작 시간", example = "2026-03-24T10:00:00")
        private LocalDateTime startDateTime;

        @Schema(description = "회의 참여자 목록", example = "[1, 2, 3]")
        private List<Long> members;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 참여자 삭제 응답(회의 나가기)")
    public static class MemberDeleteResponseDTO {

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 종료 응답")
    public static class MeetingEndResponseDTO {

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;

        @Schema(description = "종료 시간", example = "2026-03-24T12:00:00")
        private LocalDateTime endDateTime;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 삭제 응답")
    public static class MeetingDeleteResponseDTO {

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;

        @Schema(description = "삭제 성공 여부", example = "true")
        private Boolean isRemoved;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 SFU 접속 토큰 응답")
    public static class MeetingRtcTokenDTO {

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;

        @Schema(description = "LiveKit room name", example = "meeting-1")
        private String roomName;

        @Schema(description = "LiveKit server URL", example = "wss://livekit.example.com")
        private String serverUrl;

        @Schema(description = "LiveKit join token")
        private String token;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 기본 정보 응답")
    public static class MeetingDetailDTO {

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;

        @Schema(description = "회의 명", example = "백엔드 비상대책회의")
        private String name;

        @Schema(description = "시작 시간", example = "2026-03-24T10:00:00")
        private LocalDateTime startDateTime;

        @Schema(description = "종료 시간", example = "2026-03-24T12:00:00")
        private LocalDateTime endDateTime;

        @Schema(description = "소요 시간(분단위)", example = "120")
        private Long duration;

        @Schema(description = "참여자 수", example = "3")
        private Integer memberCount;

        @Schema(description = "회의 참여자 목록", example = "[1, 2, 3]")
        private List<MeetingParticipantInfo> members;

        @Schema(
                description = "녹음 파일의 재생 시간(초). 녹음본이 아직 없거나 길이를 확인할 수 없으면 null 입니다.",
                example = "120",
                nullable = true
        )
        private Integer audioDuration;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 대화 기록 조회 응답")
    public static class HistoryListDTO {

        @Schema(description = "대화 기여 참여자 목록")
        private List<ParticipantDTO> participants;

        @Schema(description = "대화 기록 목록")
        private List<DialogueDTO> dialogues;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "참여자 정보")
        public static class ParticipantDTO {

            @Schema(description = "참여자 ID", example = "1")
            private Long memberId;

            @Schema(description = "참여자명", example = "김준용")
            private String name;

            @Schema(description = "참여자 프로필 사진 URL", example = "https://example.com/profile/user-1.jpg")
            private String profileImage;

        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "개별 대화 기록")
        public static class DialogueDTO {

            @Schema(description = "발화자 id", example = "1")
            private Long memberId;

            @Schema(description = "대화 내용", example = "아니 우리 이거 버그난다니까?!?@??@")
            private String content;

            @Schema(description = "말한 시간", example = "00:22")
            private String timestamp;

        }

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 분석 결과 조회 응답")
    public static class AnalysisResultDTO {

        @Schema(description = "분석 결과 ID", example = "1")
        private Long analysisId;

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;

        @Schema(description = "분석 완료 여부", example = "true")
        private Boolean isAnalyzed;

        @Schema(description = "회의 제목", example = "Whylog 프로젝트 서버 저장 및 배포 관련 논의", nullable = true)
        private String meetingTitle;

        @Schema(description = "회의 목적", example = "서버 저장 시도 및 배포 DB 상태 점검", nullable = true)
        private String meetingPurpose;

        @Schema(description = "회의 재생 시간(초)", example = "43", nullable = true)
        private Integer audioDuration;

        @Schema(description = "논의 주제 목록", example = "[\"서버 저장 상태 확인\", \"배포 DB 환경\"]", nullable = true)
        private List<String> topics;

        @Schema(description = "핵심 맥락 목록", example = "[\"서버에 데이터가 저장되지 않은 상태로 추정됨\"]", nullable = true)
        private List<String> coreContext;

        @Schema(description = "적용사항 제목 목록", example = "[\"서버 저장 절차 정리\", \"배포 DB 점검\"]", nullable = true)
        private List<String> applicationTitles;

        @Schema(description = "적용사항 사유 목록", example = "[\"저장 실패 원인을 추적하기 위해\", \"배포 환경 차이를 확인하기 위해\"]", nullable = true)
        private List<String> applicationReasons;

        public static AnalysisResultDTO createFalse(Long meetingId) {
            return AnalysisResultDTO.builder()
                    .meetingId(meetingId)
                    .isAnalyzed(false)
                    .build();
        }

        public static AnalysisResultDTO create(MeetingAnalysis ma, Integer audioDuration) {
            return AnalysisResultDTO.builder()
                    .analysisId(ma.getId())
                    .meetingId(ma.getMeeting().getId())
                    .isAnalyzed(true)
                    .meetingTitle(ma.getMeetingTitle())
                    .meetingPurpose(ma.getMeetingPurpose())
                    .audioDuration(audioDuration)
                    .topics(ma.getTopics())
                    .coreContext(ma.getCoreContext())
                    .applicationTitles(ma.getApplicationTitles())
                    .applicationReasons(ma.getApplicationReasons())
                    .build();
        }

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 오디오 응답. 프론트엔드는 audioUrl을 <audio src> 또는 Audio()로 바로 재생하면 됩니다.")
    public static class AudioDTO {
        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;

        @Schema(description = "실제 재생 가능한 오디오 저장 키(S3 object key). 예: recordings/meeting-1-audio.mp4", example = "recordings/meeting-1-audio.mp4")
        private String audioKey;

        @Schema(
                description = "10분짜리 presigned URL. 브라우저는 이 URL로 오디오를 직접 받아 재생합니다.",
                example = "https://example.com/presigned-audio-url"
        )
        private String audioUrl;

        @Schema(
                description = "녹음 파일의 재생 시간(초). 아직 파일이 없거나 길이를 알 수 없으면 null 입니다.",
                example = "120",
                nullable = true
        )
        private Integer audioDuration;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "적용사항 항목")
    public static class ApplicationDTO {

        @Schema(description = "적용사항 ID", example = "1")
        private Long applicationId;

        @Schema(description = "적용사항명", example = "Redis 기술 변경")
        private String name;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "미팅 내 참여자 정보")
    public static class MeetingParticipantInfo{

        @Schema(description = "멤버 id", example = "1")
        private Long memberId;

        @Schema(description = "유저이름", example = "아무개")
        private String name;

        @Schema(description = "프로필이미지", example = "https://example.com/profile/user-1.jpg")
        private String profileImage;

    }

}
