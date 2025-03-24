package ru.yandex.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.shareit.item.ItemStatus;

@Data
@AllArgsConstructor
public class UpdateItemDto {
    private String name;
    private String description;
    private ItemStatus status;

    @JsonCreator
    public UpdateItemDto(@JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("available") Boolean status) {
        this.name = name;
        this.description = description;
        this.status =
            status == null ? null : (status ? ItemStatus.AVAILABLE : ItemStatus.UNAVAILABLE);
    }
}

