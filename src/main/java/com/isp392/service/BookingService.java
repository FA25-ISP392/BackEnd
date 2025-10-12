package com.isp392.service;

import com.isp392.dto.request.BookingCreationRequest;
import com.isp392.dto.request.BookingUpdateRequest;
import com.isp392.dto.response.BookingResponse;
import com.isp392.entity.Booking;
import com.isp392.entity.Customer;
import com.isp392.entity.TableEntity;
import com.isp392.mapper.BookingMapper;
import com.isp392.repository.BookingRepository;
import com.isp392.repository.CustomerRepository;
import com.isp392.repository.TableRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
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

    @Transactional
    public BookingResponse createBooking(BookingCreationRequest request, String username) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        TableEntity table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new RuntimeException("Table not found"));
        LocalDateTime startTime = request.getBookingDate().minusHours(2);
        LocalDateTime endTime = request.getBookingDate().plusHours(2);
        List<Booking> existing = bookingRepository.findByTableAndBookingDateBetween(table, startTime, endTime);
        if (!existing.isEmpty()) {
            throw new RuntimeException("Table already booked at this time!");
        }
        table.setIsAvailable(false);
        tableRepository.save(table);
        Booking booking = bookingMapper.toEntity(request);
        booking.setCustomer(customer);

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
    public BookingResponse updateBooking(int id, BookingUpdateRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Lấy bàn mới
        TableEntity newTable = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new RuntimeException("Table not found"));
        if (booking.getTable().getTableId() != newTable.getTableId()) {
            LocalDateTime startTime = request.getBookingDate().minusHours(2);
            LocalDateTime endTime = request.getBookingDate().plusHours(2);
            List<Booking> existing = bookingRepository.findByTableAndBookingDateBetween(newTable, startTime, endTime);
            if (!existing.isEmpty()) {
                throw new RuntimeException("New table already booked at this time!");
            }
            newTable.setIsAvailable(false);
            tableRepository.save(newTable);
            booking.getTable().setIsAvailable(true);
            bookingRepository.save(booking);
            booking.setTable(newTable);
        }
        booking.setSeat(request.getSeat());
        booking.setBookingDate(request.getBookingDate());
        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public void cancelBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        TableEntity table = booking.getTable();
        table.setIsAvailable(true);
        tableRepository.save(table);
        bookingRepository.delete(booking);
    }
}
