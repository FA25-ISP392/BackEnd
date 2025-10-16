package com.isp392.service;

import com.isp392.dto.request.BookingApprovalRequest;
import com.isp392.dto.request.BookingCreationRequest;
import com.isp392.dto.request.BookingUpdateRequest;
import com.isp392.dto.response.BookingResponse;
import com.isp392.entity.Booking;
import com.isp392.entity.Customer;
import com.isp392.entity.TableEntity;
import com.isp392.enums.BookingStatus;
import com.isp392.mapper.BookingMapper;
import com.isp392.repository.BookingRepository;
import com.isp392.repository.CustomerRepository;
import com.isp392.repository.TableRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {
    BookingRepository bookingRepository;
    CustomerRepository customerRepository;
    TableRepository tableRepository;
    BookingMapper bookingMapper;

    //    @Transactional
//    public BookingResponse createBooking(BookingCreationRequest request, String username) {
//        Customer customer = customerRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("Customer not found"));
//
//        TableEntity table = tableRepository.findById(request.getTableId())
//                .orElseThrow(() -> new RuntimeException("Table not found"));
//        LocalDateTime startTime = request.getBookingDate().minusHours(2);
//        LocalDateTime endTime = request.getBookingDate().plusHours(2);
//        List<Booking> existing = bookingRepository.findByTableAndBookingDateBetween(table, startTime, endTime);
//        if (!existing.isEmpty()) {
//            throw new RuntimeException("Table already booked at this time!");
//        }
//        table.setIsAvailable(false);
//        tableRepository.save(table);
//        Booking booking = bookingMapper.toEntity(request);
//        booking.setCustomer(customer);
//
//        return bookingMapper.toResponse(bookingRepository.save(booking));
//    }
    @Transactional
    public BookingResponse createBooking(BookingCreationRequest request, String username) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Booking booking = bookingMapper.toBooking(request);
        booking.setCustomer(customer);
        booking.setWantTable(request.getWantTable());
        booking.setCreatedAt(LocalDateTime.now());

        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    public List<BookingResponse> findAllBookings() {
        return bookingRepository.findAll().stream().map(bookingMapper::toResponse).toList();
    }

    public BookingResponse getBookingById(int id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse approvedBooking(BookingApprovalRequest request, int bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getStatus().equals(BookingStatus.PENDING)) {
            throw new RuntimeException("Booking has already been processed!");
        }

        TableEntity table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new RuntimeException("Table not found"));
        if (booking.getSeat() > table.getSeatTable() + 1) {
            throw new RuntimeException("Số người vượt quá sức chứa của bàn!");
        }
        LocalDateTime startTime = booking.getBookingDate().minusHours(2);
        LocalDateTime endTime = booking.getBookingDate().plusHours(2);
        List<Booking> existing = bookingRepository.findByTableAndBookingDateBetween(table, startTime, endTime);
        if (!existing.isEmpty()) {
            throw new RuntimeException("Table already booked at this time!");
        }
        booking.setTable(table);
        booking.setStatus(BookingStatus.APPROVED);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public List<BookingResponse> findBookingStatus(BookingStatus status) {
        List<Booking> booking = bookingRepository.findByStatus(status);
        return booking.stream().map(bookingMapper::toResponse).toList();
    }

    @Transactional
    public BookingResponse updateBooking(int id, BookingUpdateRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (request.getBookingDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Booking date must be in the future!");
        }

        if (booking.getStatus().equals(BookingStatus.APPROVED) || booking.getStatus().equals(BookingStatus.REJECTED) || booking.getStatus().equals(BookingStatus.CANCELLED)) {
            throw new RuntimeException("Cannot update a booking that has been approved, rejected, or cancelled!");
        }

        if (booking.getStatus().equals(BookingStatus.PENDING)) {
            booking.setSeat(request.getSeat());
            booking.setBookingDate(request.getBookingDate());
        }
        if (request.getWantTable() != null) {
            booking.setWantTable(request.getWantTable());
        }
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse rejectBooking(int id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.REJECTED);

        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse cancelBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (booking.getStatus().equals(BookingStatus.CANCELLED)) {
            throw new RuntimeException("Booking has already been cancelled!");
        }
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }
    @Transactional
    public List<BookingResponse> getBookingsByDateAndTable(int tableId, LocalDate date) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Booking> ls = bookingRepository.findByTableAndBookingDateBetween(table, startOfDay, endOfDay);
        return ls.stream().map(bookingMapper::toResponse).toList();
    }
}
