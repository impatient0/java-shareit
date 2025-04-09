package ru.practicum.shareit.item;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public List<ItemDto> getAllItems() {
        List<ItemDto> items = itemRepository.findAll().stream().map(itemMapper::mapToDto).toList();
        log.debug("Fetched {} items", items.size());
        return items;
    }

    @Override
    public ItemDto saveItem(NewItemDto newItemDto, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        Item item = itemMapper.mapToItem(newItemDto);
        item.setOwner(userRepository.findById(userId).get());
        Item savedItem = itemRepository.save(item);
        log.debug("Saved new item: {}", savedItem);
        return itemMapper.mapToDto(savedItem);
    }

    @Override
    public ItemDto getById(Long id) {
        return itemMapper.mapToDto(itemRepository.findById(id).orElseThrow(() -> {
            log.warn("Item with id {} not found", id);
            return new ItemNotFoundException(
                "Item with id " + id + " not found");
        }));
    }

    @Override
    public ItemDto update(UpdateItemDto updateItemDto, Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found for update", itemId);
            return new ItemNotFoundException(
                "Item with id " + itemId + " not found");
        });
        if (!item.getOwner().getId().equals(userId)) {
            log.warn("User with id {} does not own item with id {}", userId, itemId);
            throw new AccessDeniedException(
                "User with id " + userId + " does not own item with id " + itemId);
        }
        Item updatedItem = itemMapper.updateItemFields(updateItemDto, item);
        itemRepository.save(updatedItem);
        log.debug("Updated item: {}", updatedItem);
        return itemMapper.mapToDto(updatedItem);
    }

    @Override
    public List<ItemDto> getItemsByUserId(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        List<ItemDto> items = itemRepository.findByOwnerId(userId).stream().map(itemMapper::mapToDto)
            .toList();
        log.debug("Fetched {} items for user with id {}", items.size(), userId);
        return items;
    }

    @Override
    public void delete(Long id, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        Item item = itemRepository.findById(id).orElseThrow(() -> {
            log.warn("Item with id {} not found for delete", id);
            return new ItemNotFoundException(
                "Item with id " + id + " not found");
        });
        if (!item.getOwner().getId().equals(userId)) {
            log.warn("User with id {} does not own item with id {}", userId, id);
            throw new AccessDeniedException(
                "User with id " + userId + " does not own item with id " + id);
        }
        log.debug("Deleting item with id {} by user with id {}", id, userId);
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> searchItems(String query, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        if (query.isBlank()) {
            log.debug("Search query is blank, returning empty list");
            return List.of();
        }
        // Shouldn't we filter out user's own items when searching?..
        List<ItemDto> items = itemRepository.search(query).stream().map(itemMapper::mapToDto)
            .toList();
        log.debug("Found {} items by query: {}", items.size(), query);
        return items;

    }
}
