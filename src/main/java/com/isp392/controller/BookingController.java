package com.isp392.controller;

import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.BookingCreationRequest;
import com.isp392.dto.request.BookingUpdateRequest;
import com.isp392.dto.response.BookingResponse;
import com.isp392.service.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@CrossOrigin("*")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {
    BookingService bookingService;

    @PostMapping
    public ApiResponse<BookingResponse> createBooking(@RequestBody BookingCreationRequest request, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("sub"); // lấy username từ token JWT
        BookingResponse booking = bookingService.createBooking(request, username);
        ApiResponse<BookingResponse> response = new ApiResponse<>();
        response.setResult(booking);
        return response;
    }

    @GetMapping
    public ApiResponse<List<BookingResponse>> findAllBookings() {
        List<BookingResponse> list = bookingService.findAllBookings();
        ApiResponse<List<BookingResponse>> response = new ApiResponse<>();
        response.setResult(list);
        return response;
    }

    @PutMapping("/{id}")
    public ApiResponse<BookingResponse> updateBooking(
            @PathVariable int id,
            @RequestBody BookingUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getClaimAsString("sub");
        BookingResponse updated = bookingService.updateBooking(id, request);

        ApiResponse<BookingResponse> response = new ApiResponse<>();
        response.setResult(updated);
        return response;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> cancelBooking(
            @PathVariable int id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getClaimAsString("sub");
        bookingService.cancelBooking(id);
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult("Booking cancelled successfully by " + username);
        return response;
    }
}
