package ru.yandex.practicum.shareit.user.dto;

import ru.yandex.practicum.shareit.user.User;

public interface UserMapper {

    UserDto mapToDto(User user);

    User mapToUser(NewUserDto userDto);

    User updateUserFields(UpdateUserDto userDto, User user);
}
