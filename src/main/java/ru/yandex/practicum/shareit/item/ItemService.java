package ru.yandex.practicum.shareit.item;

import java.util.List;
import ru.yandex.practicum.shareit.item.dto.ItemDto;
import ru.yandex.practicum.shareit.item.dto.NewItemDto;
import ru.yandex.practicum.shareit.item.dto.UpdateItemDto;

public interface ItemService {
    List<ItemDto> getAllItems();
    ItemDto saveItem(NewItemDto item, Long userId);
    ItemDto getById(Long id);
    ItemDto update(UpdateItemDto item, Long userId, Long itemId);
    List<ItemDto> getItemsByUserId(Long userId);

    void delete(Long id, Long userId);

    List<ItemDto> searchItems(String query, Long userId);
}
