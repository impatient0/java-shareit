package ru.yandex.practicum.shareit.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.shareit.exception.EmailAlreadyExistsException;

@Repository
@SuppressWarnings("unused")
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users;
    private final AtomicLong nextId;

    public UserRepositoryImpl() {
        users = new HashMap<>();
        nextId = new AtomicLong(1);
    }

    @Override
    public List<User> getAll() {
        return users.values().stream().map(User::new).collect(Collectors.toList());
    }

    @Override
    public Long save(User user) {
        if (existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(
                "User with email " + user.getEmail() + " already exists");
        }
        user.setId(nextId.getAndIncrement());
        users.put(user.getId(), user);
        return user.getId();
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id)).map(User::new);
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public Optional<User> update(User updatedUser) {
        Long userId = updatedUser.getId();
        if (users.containsKey(userId)) {
            User existingUser = users.get(userId);
            if (updatedUser.getName() != null) {
                existingUser.setName(updatedUser.getName());
            }
            if (updatedUser.getEmail() != null && !updatedUser.getEmail()
                .equals(existingUser.getEmail())) {
                if (existsByEmail(updatedUser.getEmail())) {
                    throw new EmailAlreadyExistsException(
                        "User with email " + updatedUser.getEmail() + " already exists");
                }
                existingUser.setEmail(updatedUser.getEmail());
            }
            return Optional.of(new User(existingUser));
        }
        return Optional.empty();
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }
}