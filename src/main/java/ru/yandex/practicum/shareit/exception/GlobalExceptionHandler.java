package ru.yandex.practicum.shareit.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@SuppressWarnings("unused")
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<ErrorMessage> handleNotFound(final RuntimeException e) {
        return ResponseEntity.status(404).body(new ErrorMessage(e.getMessage(), 404));
    }

    @ExceptionHandler({EmailAlreadyExistsException.class})
    public ResponseEntity<ErrorMessage> handleEmailAlreadyExists(final RuntimeException e) {
        return ResponseEntity.status(409).body(new ErrorMessage(e.getMessage(), 409));
    }

    @ExceptionHandler({UserValidationException.class})
    public ResponseEntity<ErrorMessage> handleValidationError(final RuntimeException e) {
        return ResponseEntity.status(400).body(new ErrorMessage(e.getMessage(), 400));
    }

}
