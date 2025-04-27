package ru.practicum.shareit.server.booking.dto;

import ru.practicum.shareit.common.dto.booking.BookingDto;
import ru.practicum.shareit.common.dto.booking.NewBookingDto;
import ru.practicum.shareit.server.booking.Booking;

public interface BookingMapper {

    BookingDto mapToDto(Booking booking);

    Booking mapToBooking(NewBookingDto newBookingDto);
}
