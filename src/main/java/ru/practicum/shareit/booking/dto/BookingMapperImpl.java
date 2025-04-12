package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.dto.UserMapper;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class BookingMapperImpl implements BookingMapper {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    public BookingDto mapToDto(Booking booking) {
        return new BookingDto(booking.getId(), itemMapper.mapToDto(booking.getItem()), userMapper.mapToDto(booking.getBooker()),
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