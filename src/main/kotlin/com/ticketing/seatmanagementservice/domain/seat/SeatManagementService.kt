package com.ticketing.seatmanagementservice.domain.seat

import com.fasterxml.jackson.databind.ObjectMapper
import com.ticketing.seatmanagementservice.domain.seat.dto.SeatRegistrationRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

/**
 * 좌석 정보 등록 및 상태 관리에 대한 비즈니스 로직을 처리하는 서비스
 */
@Service
class SeatManagementService (

    //Redis와 통신하기 위한 Spring 표준 도구
    private val redisTemplate: RedisTemplate<String, String>,
    //객체를 JSON 문자열로 변환
    private val objectMapper: ObjectMapper
) {
    /**
     * 특정 상품에 대한 전체 좌석 정보를 Redis에 등록합니다.
     *
     * 흐름
     * 1.요청받은 좌석 목록(List<SeatInfo>)을 순회합니다.
     * 2.각 좌석 정보를 JSON형태의 문자열로 변환합니다.
     * 3.Redis에 저장할 Key와 좌석정보를 담을 Map을 준비합니다.
     * 4.Map에 Key(좌석ID)를 기준으로 JSON문자열을 Value로 하여 모든 좌석정보를 담습니다.
     * 5.Redis의 HASH 자료구조에 putAll 명령어를 사용하여 모든 좌석 정보를 한 번에 저장합니다.
     */
    fun registerSeats(request: SeatRegistrationRequest) {
        val key = "product:${request.productId}:seats"
        val seatsMap = mutableMapOf<String, String>()

        request.seats.forEach() { seat ->
            val seatId = "${seat.section}-${seat.row}-${seat.seatNumber}"
            val seatDetails = mapOf(
                "status" to SeatStatus.AVAILABLE.name,
                "grade" to seat.grade,
                "price" to seat.price
            )

            //좌석의 상세 정보를 JSON 문자열로 변환하여 저장
            seatsMap[seatId] = objectMapper.writeValueAsString(seatDetails)
        }

        //HASH 자료구조를 사용하여, 하나의 상품에 대한 모든 좌석 정보를 그룹으로 묶어 저장합니다.
        redisTemplate.opsForHash<String, String>().putAll(key, seatsMap)
    }
}