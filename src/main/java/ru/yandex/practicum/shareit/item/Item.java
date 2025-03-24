package ru.yandex.practicum.shareit.item;

import lombok.Data;
import ru.yandex.practicum.shareit.user.User;

@Data
public class Item {
    private Long id;
    private String name;
    private String description;
    private User owner;
    private ItemStatus status;
}
