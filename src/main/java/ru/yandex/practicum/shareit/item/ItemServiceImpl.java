package ru.yandex.practicum.shareit.item;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.shareit.item.dto.ItemDto;
import ru.yandex.practicum.shareit.item.dto.ItemMapper;
import ru.yandex.practicum.shareit.item.dto.NewItemDto;
import ru.yandex.practicum.shareit.item.dto.UpdateItemDto;
import ru.yandex.practicum.shareit.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final Validator validator;

    @Override
    public List<ItemDto> getAllItems() {
        List<ItemDto> items = itemRepository.getAll().stream().map(itemMapper::mapToDto).toList();
        log.debug("Fetched {} items", items.size());
        return items;
    }

    @Override
    public ItemDto saveItem(NewItemDto newItemDto, Long userId) {
        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);
        if (!violations.isEmpty()) {
            String violationMessage = violations.iterator().next().getMessage();
            log.warn("Error when saving item: {}", violationMessage);
            throw new ru.yandex.practicum.shareit.exception.ItemValidationException(
                violationMessage);
        }
        if (userRepository.getById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new ru.yandex.practicum.shareit.exception.UserNotFoundException(
                "User with id " + userId + " not found");
        }
        Item item = itemMapper.mapToItem(newItemDto);
        item.setOwner(userRepository.getById(userId).get());
        Long itemId = itemRepository.save(item);
        item.setId(itemId);
        log.debug("Saved new item: {}", item);
        return itemMapper.mapToDto(item);
    }

    @Override
    public ItemDto getById(Long id) {
        return itemMapper.mapToDto(itemRepository.getById(id).orElseThrow(() -> {
            log.warn("Item with id {} not found", id);
            return new ru.yandex.practicum.shareit.exception.ItemNotFoundException(
                "Item with id " + id + " not found");
        }));
    }

    @Override
    public ItemDto update(UpdateItemDto updateItemDto, Long userId, Long itemId) {
        Item item = itemRepository.getById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found for update", itemId);
            return new ru.yandex.practicum.shareit.exception.ItemNotFoundException(
                "Item with id " + itemId + " not found");
        });
        if (!item.getOwner().getId().equals(userId)) {
            log.warn("User with id {} does not own item with id {}", userId, itemId);
            throw new ru.yandex.practicum.shareit.exception.AccessDeniedException(
                "User with id " + userId + " does not own item with id " + itemId);
        }
        Set<ConstraintViolation<UpdateItemDto>> violations = validator.validate(updateItemDto);
        if (!violations.isEmpty()) {
            String violationMessage = violations.iterator().next().getMessage();
            log.warn("Error when updating item: {}", violationMessage);
            throw new ru.yandex.practicum.shareit.exception.ItemValidationException(
                violationMessage);
        }
        Item updatedItem = itemMapper.updateItemFields(updateItemDto, item);
        itemRepository.update(updatedItem);
        log.debug("Updated item: {}", updatedItem);
        return itemMapper.mapToDto(updatedItem);
    }

    @Override
    public List<ItemDto> getItemsByUserId(Long userId) {
        if (userRepository.getById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new ru.yandex.practicum.shareit.exception.UserNotFoundException(
                "User with id " + userId + " not found");
        }
        List<ItemDto> items = itemRepository.getByOwner(userId).stream().map(itemMapper::mapToDto)
            .toList();
        log.debug("Fetched {} items for user with id {}", items.size(), userId);
        return items;
    }

    @Override
    public void delete(Long id, Long userId) {
        if (userRepository.getById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new ru.yandex.practicum.shareit.exception.UserNotFoundException(
                "User with id " + userId + " not found");
        }
        Item item = itemRepository.getById(id).orElseThrow(() -> {
            log.warn("Item with id {} not found for delete", id);
            return new ru.yandex.practicum.shareit.exception.ItemNotFoundException(
                "Item with id " + id + " not found");
        });
        if (!item.getOwner().getId().equals(userId)) {
            log.warn("User with id {} does not own item with id {}", userId, id);
            throw new ru.yandex.practicum.shareit.exception.AccessDeniedException(
                "User with id " + userId + " does not own item with id " + id);
        }
        log.debug("Deleting item with id {} by user with id {}", id, userId);
        itemRepository.delete(id);
    }

    @Override
    public List<ItemDto> searchItems(String query, Long userId) {
        if (userRepository.getById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new ru.yandex.practicum.shareit.exception.UserNotFoundException(
                "User with id " + userId + " not found");
        }
        if (query.isBlank()) {
            log.debug("Search query is blank, returning empty list");
            return List.of();
        }
        // Shouldn't we filter out user's own items when searching?..
        List<ItemDto> items = itemRepository.search(query).stream()
            .map(itemMapper::mapToDto)
            .toList();
        log.debug("Found {} items by query: {}", items.size(), query);
        return items;

    }
}
