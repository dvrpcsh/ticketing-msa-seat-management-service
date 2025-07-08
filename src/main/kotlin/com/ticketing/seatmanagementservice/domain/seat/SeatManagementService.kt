package com.ticketing.seatmanagementservice.domain.seat

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ticketing.seatmanagementservice.domain.seat.dto.LockSeatRequest
import com.ticketing.seatmanagementservice.domain.seat.dto.SeatRegistrationRequest
import com.ticketing.seatmanagementservice.domain.seat.dto.SeatStatusResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

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

    /**
     * 특정 상품의 전체 좌석 상태 목록을 조회합니다.
     *
     * 흐름
     * 1.요청된 productId를 기반으로 Redis에서 사용할 키를 생성합니다.
     * 2.Redis의 HGETALL 명령어에 해당하는 entries()를 호출하여, 해당 키의 모든 필드(좌석ID)와 값(좌석 정보 JSON)을 Map 형태로 가져옵니다.
     * 3.가져온 Map의 각 항목을 순회합니다.
     * 4.각 좌석의 정보(JSON 문자열)를 Map<String, Any> 형태로 다시 변환(파싱)합니다.
     * 5.파싱된 정보와 좌석ID를 사용하여 최종 응답 DTO인 SeatStatusResponse 객체를 생성합니다.
     * 6.생성된 DTO들을 리스트에 담아 클라이언트에게 반환합니다.
     */
    fun getSeatStatuses(productId: Long): List<SeatStatusResponse> {
        val key = "product:${productId}:seats"
        val hashEntries = redisTemplate.opsForHash<String, String>().entries(key)

        return hashEntries.map { (seatId, seatDetailsJson) ->
            //JSON문자열을 Map으로 변환(TypeReference를 사용하여 Generic타입 정확히 명시)
            //val seatDetails: Map<String, Any> = objectMapper.readValue(seatDetailsJson)
            val seatDetails: Map<String, Any> = objectMapper.readValue(seatDetailsJson, object : TypeReference<Map<String, Any>>() {})

            SeatStatusResponse(
                seatId = seatId,
                grade =  seatDetails["grade"] as String,
                price =  seatDetails["price"] as Int,
                status =  SeatStatus.valueOf(seatDetails["status"] as String)
            )
        }
    }

    /**
     * 특정 좌석을 선택하면 다른 사용자가 접근하지 못하게 잠금(Lock)을 합니다.
     *
     * 흐름
     * 1.좌석 잠금을 위한 별도 키를 생성합니다.
     * 2.Redis의 'setIfAbsent (SETNX)' 명령어를 사용하여 1번에서 만든 키를 설정합니다.
     * 2-1 위 작업이 성공하면 true, 실패하면 false 반환
     * 3.좌석 선점에 성공하면, 해당 좌석의 상태를 'LOCKED'로 업데이트합니다.
     * 3-1 기존 좌석 정보를 가져와 상태만 변경하고, 다시 JSON으로 변환하여 HASH에 덮어씀
     * 4.선점된 좌석은 5분 뒤에 자동으로 잠금이 해제되도록 만료 시간을 설정합니다.
     */
    fun lockSeat(request: LockSeatRequest): Boolean {
        val lockKey = "lock:product:${request.productId}:${request.seatId}"
        val seatkey = "product:${request.productId}:seats"
        val lockDuration = Duration.ofMinutes(5) //5분동안 잠금

        //SETNX를 이용한 잠금 시도
        val locked = redisTemplate.opsForValue().setIfAbsent(lockKey, request.userId.toString(), lockDuration)

        if(locked != true) {
            //다른 사용자가 이미 선점중이라 예외 발생
            throw IllegalArgumentException("매진된 좌석입니다.")
        }

        try {
            //좌석 상태를 LOCKED로 변경
            val seatDetailsJson = redisTemplate.opsForHash<String, String>().get(seatkey, request.seatId)
                ?: throw IllegalArgumentException("존재하지 않는 좌석입니다.")

            val seatDetails: MutableMap<String, Any> = objectMapper.readValue(seatDetailsJson, object : TypeReference<MutableMap<String, Any>>() {})

            //이미 예약된 좌석인지 한번 더 확인
            if(SeatStatus.valueOf(seatDetails["status"] as String) != SeatStatus.AVAILABLE) {
                redisTemplate.delete(lockKey) //락을 다시 풀어줌
                throw IllegalArgumentException("이미 예약된 좌석입니다.")
            }

            seatDetails["status"] = SeatStatus.LOCKED.name
            val updatedSeatDetailsJson = objectMapper.writeValueAsString(seatDetails)

            redisTemplate.opsForHash<String, String>().put(seatkey, request.seatId, updatedSeatDetailsJson)

            return true
        } catch(e: Exception) {
            //상태 업데이트 중 문제가 발생하면, 설정했던 락을 다시 풀어줍니다.
            redisTemplate.delete(lockKey)
            throw e
        }
    }

    /**
     * 결제과 완료된 좌석의 상태를 'RESERVED'(예매 완료)로 변경합니다.
     *
     * 흐름
     * 1.Kafka로부터 productId와 seatId를 전달받습니다.
     * 2.Redis에 저장된 해당 좌석의 정보를 가져옵니다.
     * 3.좌석의 상태를 'RESERVED'로 변경하고, 다시 JSON으로 변환하여 HASH에 덮어씁니다.
     * 4.결제가 완료되었으므로, 좌석 선점(Lock)을 위해 사용했던 임시 lockKey를 삭제하여 다른 요청이 들어오지 않도록 합니다.
     */
    fun updateSeatStatusToReserved(productId: Long, seatId: String) {
        val seatKey = "product:${productId}:seats"
        val lockKey = "lock:product:${productId}:${seatId}"

        val seatDetailsJson = redisTemplate.opsForHash<String, String>().get(seatKey, seatId)
            ?: //좌석 정보가 없으면, 이미 다른 로직에 의해 처리되거나 잘못된 요청일 수 있으므로 로그만 남기고 종료.
            return

        val seatDetails: MutableMap<String, Any> = objectMapper.readValue(seatDetailsJson, object : TypeReference<MutableMap<String, Any>>() {})

        //상태를 RESERVED로 변경
        seatDetails["status"] = SeatStatus.RESERVED.name
        val updatedSeatDetailsJson = objectMapper.writeValueAsString(seatDetails)

        //HASH에 업데이트된 정보 저장
        redisTemplate.opsForHash<String, String>().put(seatKey, seatId, updatedSeatDetailsJson)

        //사용이 끝난 lockKey삭제
        redisTemplate.delete(lockKey)
    }

    /**
     * 결제 실패 또는 주문 취소 시, 선점했던 좌석의 잠금을 해제합니다.
     *
     * 1.Kafka로부터 productId와 seatId를 전달받습니다.
     * 2.Redis에 저장된 해당 좌석의 정보를 가져옵니다.
     * 3.좌석의 상태가 'LOCKED'일 경우에만 'AVAILABLE'로 변경하고 다시 저장하여 덮어씁니다.
     * 4.좌석 선점(LOCK)을 위해 사용했던 임시 lockKey를 삭제하여 다른 요청이 들어오지 않도록 합니다.
     */
    fun releaseSeatLock(productId: Long, seatId: String) {
        val seatKey = "product:${productId}:seats"
        val lockKey = "lock:product:${productId}:${seatId}"
        val seatDetailsJson = redisTemplate.opsForHash<String,String>().get(seatKey, seatId) ?: return
        val seatDetails: MutableMap<String, Any> = objectMapper.readValue(seatDetailsJson, object : TypeReference<MutableMap<String, Any>>(){})

        //상태가 LOCKED일 경우에만 AVAILABLE로 변경
        if(SeatStatus.valueOf(seatDetails["status"] as String) == SeatStatus.LOCKED) {
            seatDetails["status"] = SeatStatus.AVAILABLE.name
            val updatedSeatDetailsJson = objectMapper.writeValueAsString(seatDetails)
            redisTemplate.opsForHash<String, String>().put(seatKey, seatId, updatedSeatDetailsJson)
        }

        //사용이 끝난 lockKey삭제
        redisTemplate.delete(lockKey)
    }
}