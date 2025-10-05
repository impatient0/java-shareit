package ru.practicum.shareit.gateway.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ServerWebInputException;

@ExtendWith(MockitoExtension.class)
class DtoValidatorTest {

    @Mock
    private Validator mockValidator;

    @InjectMocks
    private DtoValidator dtoValidator;

    private Object testDto;

    @BeforeEach
    void setUp() {
        testDto = new Object();
    }

    @Test
    @DisplayName("validate should throw ServerWebInputException when DTO is null")
    void validate_whenDtoIsNull_shouldThrowServerWebInputException() {
        ServerWebInputException exception = assertThrows(ServerWebInputException.class,
            () -> dtoValidator.validate(null),
            "Should throw ServerWebInputException when DTO is null");

        assertThat(exception,
            hasProperty("reason", is(equalTo("Request body is missing or invalid"))));
        verify(mockValidator, never()).validate(any());
    }

    @Test
    @DisplayName("validate should not throw exception when there are no violations")
    void validate_whenNoViolations_shouldNotThrowException() {
        when(mockValidator.validate(testDto)).thenReturn(Collections.emptySet());

        assertDoesNotThrow(() -> dtoValidator.validate(testDto),
            "Should not throw exception when no violations");

        verify(mockValidator, times(1)).validate(testDto);
    }

    @Test
    @DisplayName("validate should throw ServerWebInputException with violation message when there"
        + " is one violation")
    void validate_whenOneViolation_shouldThrowServerWebInputExceptionWithViolationMessage() {
        @SuppressWarnings("unchecked") ConstraintViolation<Object> mockViolation =
            (ConstraintViolation<Object>) mock(
            ConstraintViolation.class);
        when(mockViolation.getMessage()).thenReturn("Error message for field X");

        Set<ConstraintViolation<Object>> violations = Set.of(mockViolation);
        when(mockValidator.validate(testDto)).thenReturn(violations);

        ServerWebInputException exception = assertThrows(ServerWebInputException.class,
            () -> dtoValidator.validate(testDto),
            "Should throw ServerWebInputException when one violation exists");

        assertThat(exception,
            hasProperty("reason", is(equalTo("Validation failed: Error message for field X"))));
        verify(mockValidator, times(1)).validate(testDto);
    }

    @Test
    @DisplayName("validate should throw ServerWebInputException with the first violation message "
        + "when there are multiple violations")
    void validate_whenMultipleViolations_shouldThrowServerWebInputExceptionWithFirstViolationMessage() {
        @SuppressWarnings("unchecked") ConstraintViolation<Object> mockViolation1 =
            (ConstraintViolation<Object>) mock(
            ConstraintViolation.class);
        when(mockViolation1.getMessage()).thenReturn("First error message");

        @SuppressWarnings("unchecked") ConstraintViolation<Object> mockViolation2 =
            (ConstraintViolation<Object>) mock(
            ConstraintViolation.class);
        // only need stubbing for one mock violation

        Set<ConstraintViolation<Object>> violations = Set.of(mockViolation1, mockViolation2);
        when(mockValidator.validate(testDto)).thenReturn(violations);

        ServerWebInputException exception = assertThrows(ServerWebInputException.class,
            () -> dtoValidator.validate(testDto),
            "Should throw ServerWebInputException when multiple violations exist");

        assertThat("Exception reason should contain 'Validation failed:' prefix", exception,
            hasProperty("reason", containsString("Validation failed: ")));
        assertThat("Exception reason should contain the message of one of the violations",
            exception, hasProperty("reason",
                either(containsString("Validation failed: First error message")).or(
                    containsString("Validation failed: Second error message"))));

        verify(mockValidator, times(1)).validate(testDto);
    }

    @Test
    @DisplayName("validate should throw ServerWebInputException with default message when "
        + "violation message is null")
    void validate_whenViolationMessageIsNull_shouldThrowServerWebInputExceptionWithDefaultMessage() {
        @SuppressWarnings("unchecked") ConstraintViolation<Object> mockViolation =
            (ConstraintViolation<Object>) mock(
            ConstraintViolation.class);
        when(mockViolation.getMessage()).thenReturn(null);

        Set<ConstraintViolation<Object>> violations = Set.of(mockViolation);
        when(mockValidator.validate(testDto)).thenReturn(violations);

        ServerWebInputException exception = assertThrows(ServerWebInputException.class,
            () -> dtoValidator.validate(testDto),
            "Should throw ServerWebInputException when violation message is null");

        assertThat(exception,
            hasProperty("reason", is(equalTo("Validation failed: Unknown validation error"))));
        verify(mockValidator, times(1)).validate(testDto);
    }
}