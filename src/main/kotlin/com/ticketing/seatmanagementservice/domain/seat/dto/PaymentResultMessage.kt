package com.ticketing.seatmanagementservice.domain.seat.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Kafka로부터 수신한 '결제 완료' 메시지를 담는 데이터 모델
 */
@Schema(description = "결제 완료 메시지 데이터 모델")
data class PaymentResultMessage(
    @Schema(description = "주문 ID")
    val orderId: Long,

    @Schema(description = "결제 성공 여부")
    val success: Boolean,

    @Schema(description = "결제 ID (성공 시)")
    val paymentId: String?,

    @Schema(description = "실패 사유 (실패 시)")
    val reason: String?,

    @Schema(description = "상품 ID")
    val productId: Long,

    @Schema(description = "좌석 ID")
    val seatId: String
)
