package ru.practicum.shareit.server.exception;

public class BookingBadRequestException extends RuntimeException {

    public BookingBadRequestException(String message) {
        super(message);
    }
}
