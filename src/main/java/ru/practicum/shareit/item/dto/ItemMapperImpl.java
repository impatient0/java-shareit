package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.Item;

@Component
@SuppressWarnings("unused")
public class ItemMapperImpl implements ItemMapper {

    @Override
    public ItemDto mapToDto(Item item) {
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getStatus());
    }

    @Override
    public Item mapToItem(NewItemDto newItemDto) {
        Item item = new Item();
        item.setName(newItemDto.getName());
        item.setDescription(newItemDto.getDescription());
        item.setStatus(newItemDto.getStatus());
        return item;
    }

    @Override
    public Item updateItemFields(UpdateItemDto updateItemDto, Item item) {
        if (updateItemDto.getName() != null) {
            item.setName(updateItemDto.getName());
        }
        if (updateItemDto.getDescription() != null) {
            item.setDescription(updateItemDto.getDescription());
        }
        if (updateItemDto.getStatus() != null) {
            item.setStatus(updateItemDto.getStatus());
        }
        return item;
    }
}
