package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateBookingDto {
    @NotNull(message = "Booking ID cannot be null")
    private Long id;
    @NotNull(message = "Status cannot be null")
    private String status;
}
