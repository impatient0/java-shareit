package ru.practicum.shareit.common.dto.booking;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

class NewBookingDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testValidNewBookingDto() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        NewBookingDto newBookingDto = new NewBookingDto(1L, start, end);

        Set<ConstraintViolation<NewBookingDto>> violations = validator.validate(newBookingDto);

        assertThat(violations, is(empty()));
    }

    @Test
    void testNewBookingDtoWithNullItemId() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        NewBookingDto newBookingDto = new NewBookingDto(null, start, end);

        Set<ConstraintViolation<NewBookingDto>> violations = validator.validate(newBookingDto);

        assertThat(violations, contains(
            allOf(
                hasProperty("message", is(equalTo("Item ID cannot be null"))),
                hasProperty("propertyPath", hasToString("itemId"))
            )
        ));
    }

    @Test
    void testNewBookingDtoWithNullStart() {
        NewBookingDto newBookingDto = new NewBookingDto(1L, null, LocalDateTime.now().plusHours(2));

        Set<ConstraintViolation<NewBookingDto>> violations = validator.validate(newBookingDto);

        assertThat(violations, contains(
            allOf(
                hasProperty("message", is(equalTo("Start date cannot be null"))),
                hasProperty("propertyPath", hasToString("start"))
            )
        ));
    }

    @Test
    void testNewBookingDtoWithNullEnd() {
        NewBookingDto newBookingDto = new NewBookingDto(1L, LocalDateTime.now().plusHours(1), null);

        Set<ConstraintViolation<NewBookingDto>> violations = validator.validate(newBookingDto);

        assertThat(violations, contains(
            allOf(
                hasProperty("message", is(equalTo("End date cannot be null"))),
                hasProperty("propertyPath", hasToString("end"))
            )
        ));
    }

    @Test
    void testNewBookingDtoWithAllNulls() {
        NewBookingDto newBookingDto = new NewBookingDto(null, null, null);

        Set<ConstraintViolation<NewBookingDto>> violations = validator.validate(newBookingDto);

        assertThat(violations, containsInAnyOrder(
            allOf(hasProperty("message", is(equalTo("Item ID cannot be null"))), hasProperty("propertyPath", hasToString("itemId"))),
            allOf(hasProperty("message", is(equalTo("Start date cannot be null"))), hasProperty("propertyPath", hasToString("start"))),
            allOf(hasProperty("message", is(equalTo("End date cannot be null"))), hasProperty("propertyPath", hasToString("end")))
        ));
    }
}