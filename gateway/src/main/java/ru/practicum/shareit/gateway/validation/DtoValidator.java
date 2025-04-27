package ru.practicum.shareit.gateway.validation; // Example package

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebInputException;

import java.util.Set;
import java.util.stream.Collectors;

@Component // Make it a Spring bean
@RequiredArgsConstructor
@Slf4j
public class DtoValidator {

    private final Validator validator; // Inject the validator here

    public <T> void validate(T dto) {
        if (dto == null) { // Basic null check
            log.warn("DTO validation failed: Input object is null");
            throw new ServerWebInputException("Request body is missing or invalid");
        }
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errorMessages = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
            log.warn("Validation failed for DTO [{}]: {}", dto.getClass().getSimpleName(), errorMessages);
            throw new ServerWebInputException("Validation failed: " + errorMessages);
        }
        log.debug("Validation successful for DTO: {}", dto);
    }
}