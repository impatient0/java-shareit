package ru.practicum.shareit.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;

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
        return new ArrayList<>(users.values());
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
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public void update(User updatedUser) {
        Long userId = updatedUser.getId();
        if (users.containsKey(userId)) {
            User existingUser = users.get(userId);
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
        }
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }
}