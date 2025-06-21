package com.ticketing.seatmanagementservice.domain.seat

import com.ticketing.seatmanagementservice.domain.seat.dto.SeatRegistrationRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 좌석관리와 관련된 HTTP 요청을 받고 처리하는 API의 진입점
 */
@Tag(name =  "좌석 관리 API")
@RestController
@RequestMapping("/api/seats")
class SeatManagementController (
    private val seatManagementService: SeatManagementService
) {
    /**
     * 특정 상품의 전체 좌석 정보를 등록하는 API
     *
     * 흐름
     * 1.관리자로부터 등록할 좌석 정보(SeatRegistrationRequest)를 HTTP Body로 받습니다.
     * 2.SeatManagementService의 registerSeats 메서드를 호출하여 비즈니스 로직을 수행합니다.
     * 3.처리가 완료되면 성공 메시지를 응답합니다.
     */
    @Operation(summary = "상품 좌석 정보 등록", description = "관리자가 특정 상품(공연/경기)의 전체 좌석 정보를 등록합니다.")
    @PostMapping("/register")
    fun registerSeats(@RequestBody request: SeatRegistrationRequest): ResponseEntity<String> {
        seatManagementService.registerSeats(request)

        return ResponseEntity.ok("상품 ID(${request.productId})에 대한 좌석 정보가 성공적으로 등록되었습니다.")
    }
}