package com.isp392.controller;

import com.isp392.dto.request.BookingApprovalRequest;
import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.BookingCreationRequest;
import com.isp392.dto.request.BookingUpdateRequest;
import com.isp392.dto.response.BookingResponse;
import com.isp392.enums.BookingStatus;
import com.isp392.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {
    BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','CUSTOMER')")
    public ApiResponse<BookingResponse> createBooking(@RequestBody BookingCreationRequest request, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("sub"); // lấy username từ token JWT
        BookingResponse booking = bookingService.createBooking(request, username);
        ApiResponse<BookingResponse> response = new ApiResponse<>();
        response.setResult(booking);
        return response;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','CUSTOMER')")
    public ApiResponse<List<BookingResponse>> findAllBookings() {
        List<BookingResponse> list = bookingService.findAllBookings();
        ApiResponse<List<BookingResponse>> response = new ApiResponse<>();
        response.setResult(list);
        return response;
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','CUSTOMER')")
    public ApiResponse<List<BookingResponse>> findBookingStatus(@PathVariable BookingStatus status) {
        List<BookingResponse> list = bookingService.findBookingStatus(status);
        ApiResponse<List<BookingResponse>> response = new ApiResponse<>();
        response.setResult(list);
        return response;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','CUSTOMER')")
    public ApiResponse<BookingResponse> updateBooking(
            @PathVariable int id,
            @RequestBody BookingUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        BookingResponse updated = bookingService.updateBooking(id, request);
        ApiResponse<BookingResponse> response = new ApiResponse<>();
        response.setMessage("Booking updated successfully!");
        response.setResult(updated);
        return response;
    }

    @PutMapping("{id}/approved")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','CUSTOMER')")
    public ApiResponse<BookingResponse> approveBooking(@PathVariable int id, @RequestBody BookingApprovalRequest request, @AuthenticationPrincipal Jwt jwt) {
        BookingResponse booking = bookingService.approvedBooking(request, id);
        ApiResponse<BookingResponse> response = new ApiResponse<>();
        response.setMessage("Booking approved successfully!");
        response.setResult(booking);
        return response;
    }

    @PutMapping("{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','CUSTOMER')")
    public ApiResponse<BookingResponse> rejectBooking(@PathVariable int id, @AuthenticationPrincipal Jwt jwt) {
        BookingResponse booking = bookingService.rejectBooking(id);
        ApiResponse<BookingResponse> response = new ApiResponse<>();
        response.setMessage("Booking rejected successfully!");
        response.setResult(booking);
        return response;
    }

    @GetMapping("/by_tableDate")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','CUSTOMER')")
    public ApiResponse<List<BookingResponse>> getBookingsByDate(@RequestParam("tableId") int tableId, @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ApiResponse<List<BookingResponse>> response = new ApiResponse<>();
        if(date == null){
            response.setMessage("Date parameter is missing or invalid.");
            response.setResult(List.of());
            return response;
        }
        response.setResult(bookingService.getBookingsByDateAndTable(tableId, date));
        if(response.getResult().isEmpty()){
            response.setMessage("No bookings found for the specified table and date.");
        }
        return response;
    }
    @GetMapping("customer/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','CUSTOMER')")
    public ApiResponse<List<BookingResponse>> getBookingsByCustomer(@PathVariable int id) {
        ApiResponse<List<BookingResponse>> response = new ApiResponse<>();
        response.setResult(bookingService.findBookingsByCusId(id));
        return  response;
    }

    @PutMapping("{id}/cancel")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','CUSTOMER')")
    public ApiResponse<BookingResponse> cancelBooking(
            @PathVariable int id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        BookingResponse booking= bookingService.cancelBooking(id);
        ApiResponse<BookingResponse> response = new ApiResponse<>();
        response.setResult(booking);
        return response;
    }
}
