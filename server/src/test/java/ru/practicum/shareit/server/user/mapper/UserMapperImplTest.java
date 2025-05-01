package ru.practicum.shareit.server.user.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.common.dto.user.NewUserDto;
import ru.practicum.shareit.common.dto.user.UpdateUserDto;
import ru.practicum.shareit.common.dto.user.UserDto;
import ru.practicum.shareit.server.user.User;

class UserMapperImplTest {

    private UserMapperImpl userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
    }

    @Test
    @DisplayName("mapToDto should map User to UserDto correctly")
    void mapToDto_whenUserIsValid_shouldReturnCorrectUserDto() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        UserDto userDto = userMapper.mapToDto(user);

        assertThat("Mapped DTO should not be null", userDto, is(notNullValue()));
        assertThat("Mapped DTO should have correct ID", userDto, hasProperty("id", equalTo(1L)));
        assertThat("Mapped DTO should have correct name", userDto,
            hasProperty("name", equalTo("Test User")));
        assertThat("Mapped DTO should have correct email", userDto,
            hasProperty("email", equalTo("test@example.com")));
    }

    @Test
    @DisplayName("mapToUser should map NewUserDto to User correctly")
    void mapToUser_whenNewUserDtoIsValid_shouldReturnCorrectUser() {
        NewUserDto newUserDto = new NewUserDto("New User", "new@example.com");

        User user = userMapper.mapToUser(newUserDto);

        assertThat("Mapped User should not be null", user, is(notNullValue()));
        assertThat("Mapped User ID should be null (not set by mapper)", user,
            hasProperty("id", is(nullValue())));
        assertThat("Mapped User should have correct name", user,
            hasProperty("name", equalTo("New User")));
        assertThat("Mapped User should have correct email", user,
            hasProperty("email", equalTo("new@example.com")));
    }

    @Test
    @DisplayName("updateUserFields should update both name and email when DTO provides both")
    void updateUserFields_whenDtoHasAllFields_shouldUpdateAllFields() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        UpdateUserDto updateUserDto = new UpdateUserDto("New Name", "new@example.com");

        User updatedUser = userMapper.updateUserFields(updateUserDto, existingUser);

        assertThat("Should return the same user instance", updatedUser,
            is(sameInstance(existingUser)));
        assertThat("User ID should remain unchanged", updatedUser, hasProperty("id", equalTo(1L)));
        assertThat("User name should be updated", updatedUser,
            hasProperty("name", equalTo("New Name")));
        assertThat("User email should be updated", updatedUser,
            hasProperty("email", equalTo("new@example.com")));
    }

    @Test
    @DisplayName("updateUserFields should update only name when DTO provides only name")
    void updateUserFields_whenDtoHasOnlyName_shouldUpdateOnlyName() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        UpdateUserDto updateUserDto = new UpdateUserDto("New Name", null);

        User updatedUser = userMapper.updateUserFields(updateUserDto, existingUser);

        assertThat("Should return the same user instance", updatedUser,
            is(sameInstance(existingUser)));
        assertThat("User ID should remain unchanged", updatedUser, hasProperty("id", equalTo(1L)));
        assertThat("User name should be updated", updatedUser,
            hasProperty("name", equalTo("New Name")));
        assertThat("User email should remain unchanged", updatedUser,
            hasProperty("email", equalTo("old@example.com")));
    }

    @Test
    @DisplayName("updateUserFields should update only email when DTO provides only email")
    void updateUserFields_whenDtoHasOnlyEmail_shouldUpdateOnlyEmail() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        UpdateUserDto updateUserDto = new UpdateUserDto(null, "new@example.com");

        User updatedUser = userMapper.updateUserFields(updateUserDto, existingUser);

        assertThat("Should return the same user instance", updatedUser,
            is(sameInstance(existingUser)));
        assertThat("User ID should remain unchanged", updatedUser, hasProperty("id", equalTo(1L)));
        assertThat("User name should remain unchanged", updatedUser,
            hasProperty("name", equalTo("Old Name")));
        assertThat("User email should be updated", updatedUser,
            hasProperty("email", equalTo("new@example.com")));
    }

    @Test
    @DisplayName("updateUserFields should not update fields when DTO provides null for them")
    void updateUserFields_whenDtoHasNullFields_shouldNotUpdateFields() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        UpdateUserDto updateUserDto = new UpdateUserDto(null, null);

        User updatedUser = userMapper.updateUserFields(updateUserDto, existingUser);

        assertThat("Should return the same user instance", updatedUser,
            is(sameInstance(existingUser)));
        assertThat("User ID should remain unchanged", updatedUser, hasProperty("id", equalTo(1L)));
        assertThat("User name should remain unchanged", updatedUser,
            hasProperty("name", equalTo("Old Name")));
        assertThat("User email should remain unchanged", updatedUser,
            hasProperty("email", equalTo("old@example.com")));
    }

    @Test
    @DisplayName("updateUserFields should handle empty string updates if needed (current logic "
        + "updates)")
    void updateUserFields_whenDtoHasEmptyStrings_shouldUpdateFieldsWithEmptyStrings() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        UpdateUserDto updateUserDto = new UpdateUserDto("", "");

        User updatedUser = userMapper.updateUserFields(updateUserDto, existingUser);

        assertThat("Should return the same user instance", updatedUser,
            is(sameInstance(existingUser)));
        assertThat("User ID should remain unchanged", updatedUser, hasProperty("id", equalTo(1L)));
        assertThat("User name should be updated to empty string", updatedUser,
            hasProperty("name", equalTo("")));
        assertThat("User email should be updated to empty string", updatedUser,
            hasProperty("email", equalTo("")));
    }
}