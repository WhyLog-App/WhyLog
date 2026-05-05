package com.whylog.server.domain.decision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DecisionResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결정사항 상세 조회 응답")
    public static class DecisionDetailDTO {

        @Schema(description = "결정사항 ID", example = "1")
        private Long decisionId;

        @Schema(description = "결정사항 이름", example = "백엔드 비상대책회의")
        private String name;

        @Schema(description = "회의 날짜", example = "2026-05-05")
        private LocalDate meetingDate;

        @Schema(description = "회의 소요 시간", example = "1시간 30분")
        private String meetingTime;

        @Schema(description = "참여 인원 수", example = "4")
        private Integer memberCount;

        @Schema(description = "참여자 목록")
        private List<DecisionParticipantDTO> members;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결정사항 참여자 정보")
    public static class DecisionParticipantDTO {

        @Schema(description = "멤버 id", example = "1")
        private Long memberId;

        @Schema(description = "유저이름", example = "아무개")
        private String name;

        @Schema(description = "프로필이미지", example = "https://example.com/profile/user-1.jpg")
        private String profileImage;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결정사항 목록 조회 응답")
    public static class DecisionListDTO {

        @Schema(description = "결정사항 ID", example = "1")
        private Long decisionId;

        @Schema(description = "회의 명", example = "백엔드 비상대책회의")
        private String name;

        @Schema(description = "적용사항 목록")
        private List<ApplicationResponse.ApplicationDTO> applications;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "신뢰도 조회 응답")
    public static class ReliabilityDTO {

        @Schema(description = "신뢰도 점수", example = "85")
        private Integer score;
    }

}
