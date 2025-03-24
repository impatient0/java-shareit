package ru.yandex.practicum.shareit.item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Long save(Item item);
    List<Item> getAll();
    List<Item> getByOwner(long ownerId);
    Optional<Item> update(Item updatedItem);
    void delete(Long id);
    List<Item> search(String query);
}
