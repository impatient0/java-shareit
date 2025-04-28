package ru.practicum.shareit.common.dto.item;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

class NewCommentDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testValidNewCommentDto() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText("This is a valid comment.");

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(newCommentDto);

        assertThat(violations, is(empty()));
    }

    @Test
    void testNewCommentDtoWithBlankText() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText("");

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(newCommentDto);

        assertThat(violations, contains(
            allOf(
                hasProperty("message", is(equalTo("Comment text cannot be blank"))),
                hasProperty("propertyPath", hasToString("text"))
            )
        ));
    }

    @Test
    void testNewCommentDtoWithNullText() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText(null);

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(newCommentDto);

        assertThat(violations, contains(
            allOf(
                hasProperty("message", is(equalTo("Comment text cannot be blank"))),
                hasProperty("propertyPath", hasToString("text"))
            )
        ));
    }

    @Test
    void testNewCommentDtoWithWhitespaceText() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText("   ");

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(newCommentDto);

        assertThat(violations, contains(
            allOf(
                hasProperty("message", is(equalTo("Comment text cannot be blank"))),
                hasProperty("propertyPath", hasToString("text"))
            )
        ));
    }
}