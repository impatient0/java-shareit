package ru.practicum.shareit.item;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;

@Data
@NoArgsConstructor
public class Item {

    private Long id;
    private String name;
    private String description;
    private User owner;
    private ItemStatus status;
}
