package ru.practicum.shareit.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
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

    @Override
    public List<UserDto> getAllUsers() {
        List<UserDto> users = userRepository.findAll().stream().map(userMapper::mapToDto).toList();
        log.debug("Fetched {} users", users.size());
        return users;
    }

    @Override
    public UserDto saveUser(NewUserDto newUserDto) {
        User user = userMapper.mapToUser(newUserDto);
        Long userId = userRepository.save(user).getId();
        user.setId(userId);
        log.debug("Saved new user: {}", user);
        return userMapper.mapToDto(user);
    }

    @Override
    public UserDto getById(Long id) {
        return userMapper.mapToDto(userRepository.findById(id).orElseThrow(() -> {
            log.warn("User with id {} not found", id);
            return new UserNotFoundException("User with id " + id + " not found");
        }));
    }

    @Override
    public UserDto update(UpdateUserDto updatedUserDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found for update", userId);
            return new UserNotFoundException("User with id " + userId + " not found");
        });
        if (updatedUserDto.getEmail() != null && !updatedUserDto.getEmail().equals(user.getEmail())
            && userRepository.existsByEmail(updatedUserDto.getEmail())) {
            throw new EmailAlreadyExistsException(
                "User with email " + updatedUserDto.getEmail() + " already exists");
        }
        User updatedUser = userMapper.updateUserFields(updatedUserDto, user);
        userRepository.save(updatedUser);
        log.debug("Updated user: {}", updatedUser);
        return userMapper.mapToDto(updatedUser);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting user with id {}", id);
        userRepository.deleteById(id);
    }
}