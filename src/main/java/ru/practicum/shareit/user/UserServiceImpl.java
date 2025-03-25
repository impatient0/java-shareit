package ru.practicum.shareit.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.UserValidationException;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Validator validator;

    @Override
    public List<UserDto> getAllUsers() {
        List<UserDto> users = userRepository.getAll().stream().map(userMapper::mapToDto).toList();
        log.debug("Fetched {} users", users.size());
        return users;
    }

    @Override
    public UserDto saveUser(NewUserDto newUserDto) {
        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(newUserDto);
        if (!violations.isEmpty()) {
            String violationMessage = violations.iterator().next().getMessage();
            log.warn("Error when saving user: {}", violationMessage);
            throw new UserValidationException(violationMessage);
        }
        User user = userMapper.mapToUser(newUserDto);
        Long userId = userRepository.save(user);
        user.setId(userId);
        log.debug("Saved new user: {}", user);
        return userMapper.mapToDto(user);
    }

    @Override
    public UserDto getById(Long id) {
        return userMapper.mapToDto(userRepository.getById(id).orElseThrow(() -> {
            log.warn("User with id {} not found", id);
            return new UserNotFoundException("User with id " + id + " not found");
        }));
    }

    @Override
    public UserDto update(UpdateUserDto updatedUserDto, Long userId) {
        User user = userRepository.getById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found for update", userId);
            return new UserNotFoundException("User with id " + userId + " not found");
        });
        Set<ConstraintViolation<UpdateUserDto>> violations = validator.validate(updatedUserDto);
        if (!violations.isEmpty()) {
            String violationMessage = violations.iterator().next().getMessage();
            log.warn("Error when updating user: {}", violationMessage);
            throw new UserValidationException(violationMessage);
        }
        User updatedUser = userMapper.updateUserFields(updatedUserDto, user);
        userRepository.update(updatedUser);
        log.debug("Updated user: {}", updatedUser);
        return userMapper.mapToDto(updatedUser);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting user with id {}", id);
        userRepository.delete(id);
    }
}