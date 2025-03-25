package ru.yandex.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.shareit.item.ItemStatus;

@Data
@AllArgsConstructor
public class ItemDto {

    private Long id;
    private String name;
    private String description;
    private ItemStatus status;

    @JsonProperty("available")
    @SuppressWarnings("unused")
    public boolean isAvailable() {
        return status == ItemStatus.AVAILABLE;
    }
}
