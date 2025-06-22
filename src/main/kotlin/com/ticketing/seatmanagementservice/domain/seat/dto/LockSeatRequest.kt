package com.ticketing.seatmanagementservice.domain.seat.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 특정 좌석에 대한 선점(Lock)을 요청하는 데이터 모델
 */
@Schema(description = "좌석 선점 요청 데이터 모델")
class LockSeatRequest (
    @Schema(description = "선점할 좌석의 상품 ID", example = "1")
    val productId: Long,

    @Schema(description = "선점할 좌석의 고유 ID (예: A-1-15", example = "A-1-15")
    val seatId: String,

    @Schema(description = "좌석을 선점하는 사용자 ID", example = "1")
    val userId: Long
)