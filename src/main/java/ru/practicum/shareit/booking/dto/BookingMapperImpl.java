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
                booking.getStart_date().toLocalDateTime(), booking.getEnd_date().toLocalDateTime(), booking.getStatus().toString());
    }

    @Override
    public Booking mapToBooking(NewBookingDto newBookingDto) {
        Booking booking = new Booking();
        booking.setStart_date(java.sql.Timestamp.valueOf(newBookingDto.getStart()));
        booking.setEnd_date(java.sql.Timestamp.valueOf(newBookingDto.getEnd()));
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    @Override
    public Booking updateBookingFields(UpdateBookingDto updateBookingDto, Booking booking) {
        if (updateBookingDto.getStatus() != null) {
            booking.setStatus(BookingStatus.valueOf(updateBookingDto.getStatus()));
        }
        return booking;
    }
}