package com.ticketing.seatmanagementservice.kafka

import com.ticketing.seatmanagementservice.domain.seat.SeatManagementService
import com.ticketing.seatmanagementservice.domain.seat.dto.PaymentResultMessage
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * 결제 시스템으로부터 '결제 완료' 메시지를 수신하여 좌석 상태를 업데이트 하는 클래스
 */
@Component
class PaymentKafkaConsumer (
    private val seatManagementService: SeatManagementService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 'payment-completed' 토픽을 구독하고 있다가, 결제 완료 메시지가 오면 좌석 상태를 변경합니다.
     *
     * 1.'결제 완료' 메시지를 수신합니다.
     * 2.메시지가 성공이면 메시지에 포함된 productId와 seatId를 사용하여 SeatManagementService의 상태 변경 메서드를 호출합니다.
     * 3.결제 실패 메시지일 경우, 선점했던 좌석을 다시 'AVAILABLE' 상태로 되돌리는 로직을 호출합니다. (TODO)
     */
    @KafkaListener(topics =  ["payment-completed"], groupId =  "seat-group")
    fun handlePaymentCompletion(message: PaymentResultMessage) {
        logger.info("'결제완료'메시지를 수신했습니다. 좌석 상태 업데이트를 시작합니다. >> $message")

        if(message.success) {
            seatManagementService.updateSeatStatusToReserved(message.productId, message.seatId)
            logger.info("좌석(ID: ${message.seatId}) 상태가 '예매 완료'로 성공적으로 변경되었습니다.")
        } else {
            //TODO: 결제 실패 시 좌석 잠금 해제 등 예외 처리 로직
            logger.warn("결제 실패 메시지 수신: 좌석(ID: ${message.seatId})의 잠금을 해제해야 합니다.")
        }
    }
}