package ru.yandex.practicum.shareit.user.dto;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.shareit.user.User;

@Component
@SuppressWarnings("unused")
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto mapToDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    @Override
    public User mapToUser(NewUserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        return user;
    }

    @Override
    public User updateUserFields(UpdateUserDto userDto, User user) {
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return user;
    }
}
