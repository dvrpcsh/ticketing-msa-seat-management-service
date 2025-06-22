package com.ticketing.seatmanagementservice.domain.seat.dto

import com.ticketing.seatmanagementservice.domain.seat.SeatStatus
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 실시간 좌석 상태 조회의 응답 데이터 모델
 */
@Schema(description = "개별 좌석의 실시간 상태 정보 응답 모델")
data class SeatStatusResponse (
    @Schema(description = "좌석의 고유 ID (예: A-1-15)", example = "A-1-15")
    val seatId: String,

    @Schema(description = "좌석 등급 (예: VIP, R, S)", example = "VIP")
    val grade: String,

    @Schema(description = "좌석 가격", example = "150000")
    val price: Int,

    @Schema(description = "현재 좌석 상태", example = "AVAILABLE")
    val status: SeatStatus
)