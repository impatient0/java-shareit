package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookingShortDto {
    private Long id;
    private Long bookerId;
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;

    public BookingShortDto(Long id, Long bookerId, Long itemId, LocalDateTime start,
        LocalDateTime end) {
        this.id = id;
        this.bookerId = bookerId;
        this.itemId = itemId;
        this.start = start;
        this.end = end;
    }
}