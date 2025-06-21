package com.ticketing.seatmanagementservice.domain.seat.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 특정 상품의 전체 좌석 정보 등록을 요청하는 데이터 모델
 */
@Schema(description = "좌석 정보 등록 요청 데이터 모델")
class SeatRegistrationRequest (
    @Schema(description = "좌석을 등록할 상품(공연/연기)의 ID", example = "1")
    val productId: Long,

    @Schema(description = "등록할 좌석 정보 목록")
    val seats: List<SeatInfo>
)

/**
 * 개별 좌석의 상세 정보를 나타내는 데이터 모델
 */
@Schema(description = "개별 좌석 정보")
data class SeatInfo(
    @Schema(description = "좌석 등급 (예: VIP, R, S)", example =  "VIP")
    val grade: String,

    @Schema(description = "구역 (예: A, B, C)", example = "A")
    val section: String,

    @Schema(description = "열 번호", example = "10")
    val row:String,

    @Schema(description = "좌석 번호", example = "15")
    val seatNumber: Int,

    @Schema(description = "좌석 가격", example = "150000")
    val price: Int
)