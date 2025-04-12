package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

@Component
@SuppressWarnings("unused")
public class BookingMapperImpl implements BookingMapper {


    @Override
    public BookingDto mapToDto(Booking booking) {
        return new BookingDto(booking.getId(), booking.getItem().getId(), booking.getBooker().getId(),
                booking.getStartDate().toLocalDateTime(), booking.getEndDate().toLocalDateTime(), booking.getStatus().toString());
    }

    @Override
    public Booking mapToBooking(NewBookingDto newBookingDto) {
        Booking booking = new Booking();
        booking.setStartDate(java.sql.Timestamp.valueOf(newBookingDto.getStart()));
        booking.setEndDate(java.sql.Timestamp.valueOf(newBookingDto.getEnd()));
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }
}