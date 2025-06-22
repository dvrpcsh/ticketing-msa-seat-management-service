package com.ticketing.seatmanagementservice.domain.seat

import com.ticketing.seatmanagementservice.domain.seat.dto.LockSeatRequest
import com.ticketing.seatmanagementservice.domain.seat.dto.SeatRegistrationRequest
import com.ticketing.seatmanagementservice.domain.seat.dto.SeatStatusResponse
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

    /**
     * 특정 상품의 실시간 좌석 상태 목록을 조회하는 API
     *
     * 흐름
     * 1.클라이언트로부터 조회할 상품의 ID를 경로 변수(PathVariable)로 받습니다.
     * 2.SeatManagementService의 getSeatStatuses 메서드를 호출하여 좌석 목록을 조회합니다.
     * 3.조회된 좌석 목록을 클라이언트에게 응답합니다.
     */
    @Operation(summary = "상품의 실시간 좌석 상태 조회")
    @GetMapping("/{productId}")
    fun getSeatStatuses(@PathVariable productId: Long): ResponseEntity<List<SeatStatusResponse>> {
        val seats = seatManagementService.getSeatStatuses(productId)

        return ResponseEntity.ok(seats)
    }

    /**
     * 특정 좌석을 5분간 선점(Lock)하는 API
     *
     * 흐름
     * 1.클라이언트로부터 선점할 좌석 정보를 받습니다.
     * 2.SeatManagementService의 lockSeat 메서드를 호출하여 좌석 선점을 시도합니다.
     * 3.선점에 성공하면 성공 메시지를, 실패하면 서비스에서 발생시킨 예외에 따라 적절한 에러를 응답합니다.
     */
    @Operation(summary = "좌석 선점(Lock)", description = "특정 좌석을 5분간 선점합니다.(동시성 제어)")
    @PostMapping("/lock")
    fun lockSeat(@RequestBody request: LockSeatRequest): ResponseEntity<String> {
        seatManagementService.lockSeat(request)

        return ResponseEntity.ok("좌석(ID: ${request.seatId})이 성공적으로 선점되었습니다.")
    }
}