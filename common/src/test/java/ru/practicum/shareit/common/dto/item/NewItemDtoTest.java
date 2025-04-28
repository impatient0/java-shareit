package ru.practicum.shareit.common.dto.item;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NewItemDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testValidNewItemDto() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("Item Name");
        newItemDto.setDescription("Item Description");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat(violations, is(empty()));
    }

    @Test
    void testNewItemDtoWithBlankName() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("");
        newItemDto.setDescription("Item Description");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat(violations, contains(
            allOf(hasProperty("message", is(equalTo("Name cannot be blank"))),
                hasProperty("propertyPath", hasToString("name")))));
    }

    @Test
    void testNewItemDtoWithNullName() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName(null);
        newItemDto.setDescription("Item Description");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat(violations, contains(
            allOf(hasProperty("message", is(equalTo("Name cannot be blank"))),
                hasProperty("propertyPath", hasToString("name")))));
    }

    @Test
    void testNewItemDtoWithWhitespaceName() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("   ");
        newItemDto.setDescription("Item Description");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat(violations, contains(
            allOf(hasProperty("message", is(equalTo("Name cannot be blank"))),
                hasProperty("propertyPath", hasToString("name")))));
    }

    @Test
    void testNewItemDtoWithBlankDescription() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("Item Name");
        newItemDto.setDescription("");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat(violations, contains(
            allOf(hasProperty("message", is(equalTo("Description cannot be blank"))),
                hasProperty("propertyPath", hasToString("description")))));
    }

    @Test
    void testNewItemDtoWithNullDescription() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("Item Name");
        newItemDto.setDescription(null);
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat(violations, contains(
            allOf(hasProperty("message", is(equalTo("Description cannot be blank"))),
                hasProperty("propertyPath", hasToString("description")))));
    }

    @Test
    void testNewItemDtoWithWhitespaceDescription() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("Item Name");
        newItemDto.setDescription("   ");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat(violations, contains(
            allOf(hasProperty("message", is(equalTo("Description cannot be blank"))),
                hasProperty("propertyPath", hasToString("description")))));
    }

    @Test
    void testNewItemDtoWithNullAvailable() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("Item Name");
        newItemDto.setDescription("Item Description");
        newItemDto.setAvailable(null);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat(violations, contains(
            allOf(hasProperty("message", is(equalTo("Item status must be set"))),
                hasProperty("propertyPath", hasToString("available")))));
    }

    @Test
    void testNewItemDtoWithAllInvalidFields() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName(null);
        newItemDto.setDescription(null);
        newItemDto.setAvailable(null);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat(violations, containsInAnyOrder(
            allOf(hasProperty("message", is(equalTo("Name cannot be blank"))),
                hasProperty("propertyPath", hasToString("name"))),
            allOf(hasProperty("message", is(equalTo("Description cannot be blank"))),
                hasProperty("propertyPath", hasToString("description"))),
            allOf(hasProperty("message", is(equalTo("Item status must be set"))),
                hasProperty("propertyPath", hasToString("available")))));
    }
}