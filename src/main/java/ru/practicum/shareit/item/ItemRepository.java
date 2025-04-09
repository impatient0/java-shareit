package ru.practicum.shareit.item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Item save(Item item);

    List<Item> findAll();

    List<Item> findByOwnerId(long ownerId);

    Optional<Item> findById(Long id);

    void deleteById(Long id);

    List<Item> search(String query);
}
