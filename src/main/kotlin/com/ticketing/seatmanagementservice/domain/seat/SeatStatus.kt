package com.ticketing.seatmanagementservice.domain.seat

/**
 * 좌석의 실시간 상태를 나타내는 Enum 클래스
 */
enum class SeatStatus {
    AVAILABLE,  //예매 가능
    LOCKED,     //임시 선점(좌석 선택 시)
    RESERVED    //예매 완료
}