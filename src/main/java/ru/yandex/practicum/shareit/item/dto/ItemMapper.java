package ru.yandex.practicum.shareit.item.dto;

import ru.yandex.practicum.shareit.item.Item;

public interface ItemMapper {

    ItemDto mapToDto(Item item);

    Item mapToItem(NewItemDto newItemDto);

    Item updateItemFields(UpdateItemDto updateItemDto, Item item);
}
