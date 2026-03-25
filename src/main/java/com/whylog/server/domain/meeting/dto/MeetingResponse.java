package com.whylog.server.domain.meeting.dto;

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
        private String status;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 초대 응답")
    public static class MeetingInvitationResponseDTO {

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;
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
        private List<Long> members;
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

        @Schema(description = "분석 내용", example = "뭐 이건 나중에 논의 주제, 핵심맥락 등 들어갈 자리입니다.")
        private String content;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 오디오 응답")
    public static class AudioDTO {

        @Schema(description = "오디오 ID", example = "1")
        private Long audioId;

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;

        @Schema(description = "오디오 URL", example = "https://example.com/audio/meeting-1.mp3")
        private String audioUrl;
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
}
