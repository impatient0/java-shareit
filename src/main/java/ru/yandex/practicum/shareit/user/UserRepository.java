package ru.yandex.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> getAll();
    Long save(User user);
    Optional<User> getById(Long id);
    boolean existsByEmail(String email);
    Optional<User> update(User updatedUser);
    void delete(Long id);
}