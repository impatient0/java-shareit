package ru.yandex.practicum.shareit.item;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unused")
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Long, Item> items;
    private final AtomicLong nextId;

    public ItemRepositoryImpl() {
        items = new java.util.HashMap<>();
        nextId = new AtomicLong(1);
    }

    @Override
    public Long save(Item item) {
        item.setId(nextId.getAndIncrement());
        items.put(item.getId(), item);
        return item.getId();
    }

    @Override
    public java.util.List<Item> getAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public java.util.List<Item> getByOwner(long ownerId) {
        return items.values().stream().filter(i -> i.getOwner().getId() == ownerId).toList();
    }

    @Override
    public Optional<Item> getById(Long id) {
        return Optional.ofNullable(items.get(id)).map(Item::new);

    }

    @Override
    public java.util.Optional<Item> update(Item updatedItem) {
        Long itemId = updatedItem.getId();
        if (items.containsKey(itemId)) {
            Item existingItem = items.get(itemId);
            if (updatedItem.getName() != null) {
                existingItem.setName(updatedItem.getName());
            }
            if (updatedItem.getDescription() != null) {
                existingItem.setDescription(updatedItem.getDescription());
            }
            if (updatedItem.getStatus() != null) {
                existingItem.setStatus(updatedItem.getStatus());
            }
            return java.util.Optional.of(new Item(existingItem));
        }
        return java.util.Optional.empty();
    }

    @Override
    public void delete(Long id) {
        items.remove(id);
    }

    @Override
    public java.util.List<Item> search(String query) {
        String lowerCaseQuery = query.toLowerCase();
        return items.values().stream().filter(i ->
            (i.getName().toLowerCase().contains(lowerCaseQuery) || i.getDescription().toLowerCase()
                .contains(lowerCaseQuery)) && i.getStatus() == ItemStatus.AVAILABLE).toList();
    }
}
