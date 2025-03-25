package ru.practicum.shareit.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        items = new HashMap<>();
        nextId = new AtomicLong(1);
    }

    @Override
    public Long save(Item item) {
        item.setId(nextId.getAndIncrement());
        items.put(item.getId(), item);
        return item.getId();
    }

    @Override
    public List<Item> getAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> getByOwner(long ownerId) {
        return items.values().stream().filter(i -> i.getOwner().getId() == ownerId).toList();
    }

    @Override
    public Optional<Item> getById(Long id) {
        return Optional.ofNullable(items.get(id)).map(Item::new);

    }

    @Override
    public Optional<Item> update(Item updatedItem) {
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
            return Optional.of(new Item(existingItem));
        }
        return Optional.empty();
    }

    @Override
    public void delete(Long id) {
        items.remove(id);
    }

    @Override
    public List<Item> search(String query) {
        String lowerCaseQuery = query.toLowerCase();
        return items.values().stream().filter(i ->
            (i.getName().toLowerCase().contains(lowerCaseQuery) || i.getDescription().toLowerCase()
                .contains(lowerCaseQuery)) && i.getStatus() == ItemStatus.AVAILABLE).toList();
    }
}
