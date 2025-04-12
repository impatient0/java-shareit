package ru.practicum.shareit.booking;

import java.util.List;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.UpdateBookingDto;

public interface BookingService {

    List<BookingDto> getAllBookings();

    BookingDto saveBooking(NewBookingDto booking, Long userId);

    BookingDto getById(Long id);

    BookingDto update(UpdateBookingDto booking, Long userId, Long bookingId);

    void delete(Long id, Long userId);

    List<BookingDto> getBookingsByBooker(Long bookerId);

    List<BookingDto> getBookingsByOwner(Long ownerId);
}
