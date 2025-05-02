package ru.practicum.shareit.server.user.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
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

@DisplayName("User Mapper Implementation Tests")
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

        assertThat("Mapped UserDto should not be null", userDto, is(notNullValue()));
        assertThat("Mapped UserDto should have correct properties", userDto,
            allOf(
                hasProperty("id", equalTo(1L)),
                hasProperty("name", equalTo("Test User")),
                hasProperty("email", equalTo("test@example.com"))
            )
        );
    }

    @Test
    @DisplayName("mapToUser should map NewUserDto to User correctly")
    void mapToUser_whenNewUserDtoIsValid_shouldReturnCorrectUser() {
        NewUserDto newUserDto = new NewUserDto("New User", "new@example.com");

        User user = userMapper.mapToUser(newUserDto);

        assertThat("Mapped User entity should not be null", user, is(notNullValue()));
        assertThat("Mapped User entity should have name and email from DTO and null ID", user,
            allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", equalTo("New User")),
                hasProperty("email", equalTo("new@example.com"))
            )
        );
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

        assertThat("updateUserFields should return the same user instance that was passed",
            updatedUser, is(sameInstance(existingUser)));
        assertThat("Updated user should have name and email updated, ID unchanged", updatedUser,
            allOf(
                hasProperty("id", equalTo(1L)),
                hasProperty("name", equalTo("New Name")),
                hasProperty("email", equalTo("new@example.com"))
            )
        );
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

        assertThat("updateUserFields should return the same user instance that was passed",
            updatedUser, is(sameInstance(existingUser)));
        assertThat("Updated user should have name updated, email unchanged, ID unchanged",
            updatedUser,
            allOf(
                hasProperty("id", equalTo(1L)),
                hasProperty("name", equalTo("New Name")),
                hasProperty("email", equalTo("old@example.com"))
            )
        );
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

        assertThat("updateUserFields should return the same user instance that was passed",
            updatedUser, is(sameInstance(existingUser)));
        assertThat("Updated user should have email updated, name unchanged, ID unchanged",
            updatedUser, allOf(
                hasProperty("id", equalTo(1L)),
                hasProperty("name", equalTo("Old Name")),
                hasProperty("email", equalTo("new@example.com"))
            )
        );
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

        assertThat("updateUserFields should return the same user instance that was passed",
            updatedUser, is(sameInstance(existingUser)));
        assertThat("Updated user fields should remain unchanged when DTO fields are null",
            updatedUser,
            allOf(
                hasProperty("id", equalTo(1L)),
                hasProperty("name", equalTo("Old Name")),
                hasProperty("email", equalTo("old@example.com"))
            )
        );
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

        assertThat("updateUserFields should return the same user instance that was passed",
            updatedUser, is(sameInstance(existingUser)));
        assertThat("Updated user should have name and email updated to empty strings, ID unchanged",
            updatedUser,
            allOf(
                hasProperty("id", equalTo(1L)),
                hasProperty("name", equalTo("")),
                hasProperty("email", equalTo(""))
            )
        );
    }
}