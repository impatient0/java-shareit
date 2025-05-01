package ru.practicum.shareit.server.item.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.dto.item.CommentDto;
import ru.practicum.shareit.common.dto.item.ItemDto;
import ru.practicum.shareit.common.dto.item.ItemWithBookingInfoDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;
import ru.practicum.shareit.server.item.Comment;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.user.User;


@DisplayName("Item Mapper Implementation Tests")
@ExtendWith(MockitoExtension.class)
class ItemMapperImplTest {

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemMapperImpl itemMapper;

    private Item testItem;
    private User testOwner;

    @BeforeEach
    void setUp() {
        testOwner = new User();
        testOwner.setId(1L);
        testOwner.setName("Owner Name");
        testOwner.setEmail("owner@example.com");

        testItem = new Item();
        testItem.setId(10L);
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        testItem.setAvailable(true);
        testItem.setOwner(testOwner);
        testItem.setComments(Collections.emptySet());
    }

    @Test
    @DisplayName("mapToDto should map Item to ItemDto correctly")
    void mapToDto_whenItemIsValid_shouldReturnCorrectItemDto() {

        ItemDto itemDto = itemMapper.mapToDto(testItem);

        assertThat("Mapped DTO should not be null", itemDto, is(notNullValue()));
        assertThat("Mapped DTO should have correct ID", itemDto, hasProperty("id", equalTo(10L)));
        assertThat("Mapped DTO should have correct name", itemDto,
            hasProperty("name", equalTo("Test Item")));
        assertThat("Mapped DTO should have correct description", itemDto,
            hasProperty("description", equalTo("Test Description")));
        assertThat("Mapped DTO should have correct availability", itemDto,
            hasProperty("available", equalTo(true)));
    }

    @Test
    @DisplayName("mapToItemWithBookingInfoDto should map Item with no comments correctly")
    void mapToItemWithBookingInfoDto_whenItemHasNoComments_shouldReturnDtoWithEmptyComments() {

        ItemWithBookingInfoDto resultDto = itemMapper.mapToItemWithBookingInfoDto(testItem);

        assertThat("Mapped DTO should not be null", resultDto, is(notNullValue()));
        assertThat("Mapped DTO should have correct ID", resultDto, hasProperty("id", equalTo(10L)));
        assertThat("Mapped DTO should have correct name", resultDto,
            hasProperty("name", equalTo("Test Item")));
        assertThat("Mapped DTO should have correct description", resultDto,
            hasProperty("description", equalTo("Test Description")));
        assertThat("Mapped DTO should have correct availability", resultDto,
            hasProperty("available", equalTo(true)));
        assertThat("Mapped DTO lastBooking should be null", resultDto,
            hasProperty("lastBooking", is(nullValue())));
        assertThat("Mapped DTO nextBooking should be null", resultDto,
            hasProperty("nextBooking", is(nullValue())));
        assertThat("Mapped DTO comments should be an empty set", resultDto.getComments(),
            is(empty()));
        assertThat("CommentMapper should not be called", true);
        verify(commentMapper, never()).mapToDto(any(Comment.class));
    }

    @Test
    @DisplayName("mapToItemWithBookingInfoDto should map Item with comments correctly")
    void mapToItemWithBookingInfoDto_whenItemHasComments_shouldMapCommentsUsingCommentMapper() {
        Comment comment1 = new Comment();
        comment1.setId(101L);
        comment1.setText("Comment 1 text");
        comment1.setItem(testItem);
        comment1.setAuthor(testOwner);
        comment1.setCreatedAt(LocalDateTime.now().minusDays(1));

        Comment comment2 = new Comment();
        comment2.setId(102L);
        comment2.setText("Comment 2 text");
        comment2.setItem(testItem);
        comment2.setAuthor(testOwner);
        comment2.setCreatedAt(LocalDateTime.now());

        testItem.setComments(Set.of(comment1, comment2));

        CommentDto commentDto1 = new CommentDto(101L, "Comment 1 text", 10L, "Owner Name",
            LocalDateTime.now().toString());
        CommentDto commentDto2 = new CommentDto(102L, "Comment 2 text", 10L, "Owner Name",
            LocalDateTime.now().toString());

        when(commentMapper.mapToDto(comment1)).thenReturn(commentDto1);
        when(commentMapper.mapToDto(comment2)).thenReturn(commentDto2);

        ItemWithBookingInfoDto resultDto = itemMapper.mapToItemWithBookingInfoDto(testItem);

        assertThat("Mapped DTO should not be null", resultDto, is(notNullValue()));
        assertThat("Mapped DTO should have correct ID", resultDto, hasProperty("id", equalTo(10L)));
        assertThat("Mapped DTO should have correct name", resultDto,
            hasProperty("name", equalTo("Test Item")));
        assertThat("Mapped DTO should have correct description", resultDto,
            hasProperty("description", equalTo("Test Description")));
        assertThat("Mapped DTO should have correct availability", resultDto,
            hasProperty("available", equalTo(true)));
        assertThat("Mapped DTO lastBooking should be null", resultDto,
            hasProperty("lastBooking", is(nullValue())));
        assertThat("Mapped DTO nextBooking should be null", resultDto,
            hasProperty("nextBooking", is(nullValue())));
        assertThat("Mapped DTO comments set should not be null", resultDto.getComments(),
            is(notNullValue()));
        assertThat("Mapped DTO comments set should have correct size", resultDto.getComments(),
            hasSize(2));
        assertThat("Mapped DTO comments set should contain mapped DTOs", resultDto.getComments(),
            containsInAnyOrder(commentDto1, commentDto2));

        verify(commentMapper, times(1)).mapToDto(comment1);
        verify(commentMapper, times(1)).mapToDto(comment2);
    }

    @Test
    @DisplayName("mapToItem should map NewItemDto to Item correctly")
    void mapToItem_whenNewItemDtoIsValid_shouldReturnCorrectItem() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("New Item Name");
        newItemDto.setDescription("New Item Description");
        newItemDto.setAvailable(true);

        Item item = itemMapper.mapToItem(newItemDto);

        assertThat("Mapped Item should not be null", item, is(notNullValue()));
        assertThat("Mapped Item ID should be null (not set by mapper)", item,
            hasProperty("id", is(nullValue())));
        assertThat("Mapped Item name should be correct", item,
            hasProperty("name", equalTo("New Item Name")));
        assertThat("Mapped Item description should be correct", item,
            hasProperty("description", equalTo("New Item Description")));
        assertThat("Mapped Item availability should be correct", item,
            hasProperty("available", equalTo(true)));
        assertThat("Mapped Item owner should be null", item, hasProperty("owner", is(nullValue())));
        assertThat("Mapped Item comments should be empty", item.getComments(), is(empty()));
    }

    @Test
    @DisplayName("updateItemFields should update all fields when DTO provides all")
    void updateItemFields_whenDtoHasAllFields_shouldUpdateAllFields() {
        UpdateItemDto updateDto = new UpdateItemDto("Updated Name", "Updated Description", false);

        Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

        assertThat("Should return the same item instance", updatedItem, is(sameInstance(testItem)));
        assertThat("Item ID should remain unchanged", updatedItem, hasProperty("id", equalTo(10L)));
        assertThat("Item name should be updated", updatedItem,
            hasProperty("name", equalTo("Updated Name")));
        assertThat("Item description should be updated", updatedItem,
            hasProperty("description", equalTo("Updated Description")));
        assertThat("Item availability should be updated", updatedItem,
            hasProperty("available", equalTo(false)));
        assertThat("Item owner should remain unchanged", updatedItem,
            hasProperty("owner", sameInstance(testOwner)));
    }

    @Test
    @DisplayName("updateItemFields should update only name when DTO provides only name")
    void updateItemFields_whenDtoHasOnlyName_shouldUpdateOnlyName() {
        UpdateItemDto updateDto = new UpdateItemDto("Updated Name", null, null);

        Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

        assertThat("Should return the same item instance", updatedItem, is(sameInstance(testItem)));
        assertThat("Item name should be updated", updatedItem,
            hasProperty("name", equalTo("Updated Name")));
        assertThat("Item description should remain unchanged", updatedItem,
            hasProperty("description", equalTo("Test Description")));
        assertThat("Item availability should remain unchanged", updatedItem,
            hasProperty("available", equalTo(true)));
    }

    @Test
    @DisplayName(
        "updateItemFields should update only description when DTO provides only " + "description")
    void updateItemFields_whenDtoHasOnlyDescription_shouldUpdateOnlyDescription() {
        UpdateItemDto updateDto = new UpdateItemDto(null, "Updated Description", null);

        Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

        assertThat("Should return the same item instance", updatedItem, is(sameInstance(testItem)));
        assertThat("Item name should remain unchanged", updatedItem,
            hasProperty("name", equalTo("Test Item")));
        assertThat("Item description should be updated", updatedItem,
            hasProperty("description", equalTo("Updated Description")));
        assertThat("Item availability should remain unchanged", updatedItem,
            hasProperty("available", equalTo(true)));
    }

    @Test
    @DisplayName(
        "updateItemFields should update only availability when DTO provides only " + "availability")
    void updateItemFields_whenDtoHasOnlyAvailability_shouldUpdateOnlyAvailability() {
        UpdateItemDto updateDto = new UpdateItemDto(null, null, false);

        Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

        assertThat("Should return the same item instance", updatedItem, is(sameInstance(testItem)));
        assertThat("Item name should remain unchanged", updatedItem,
            hasProperty("name", equalTo("Test Item")));
        assertThat("Item description should remain unchanged", updatedItem,
            hasProperty("description", equalTo("Test Description")));
        assertThat("Item availability should be updated", updatedItem,
            hasProperty("available", equalTo(false)));
    }

    @Test
    @DisplayName("updateItemFields should not update fields when DTO provides null for them")
    void updateItemFields_whenDtoHasNullFields_shouldNotUpdateFields() {
        UpdateItemDto updateDto = new UpdateItemDto(null, null, null);

        Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

        assertThat("Should return the same item instance", updatedItem, is(sameInstance(testItem)));
        assertThat("Item name should remain unchanged", updatedItem,
            hasProperty("name", equalTo("Test Item")));
        assertThat("Item description should remain unchanged", updatedItem,
            hasProperty("description", equalTo("Test Description")));
        assertThat("Item availability should remain unchanged", updatedItem,
            hasProperty("available", equalTo(true)));
    }

    @Test
    @DisplayName("updateItemFields should handle empty string updates for name and description")
    void updateUserFields_whenDtoHasEmptyStrings_shouldUpdateFieldsWithEmptyStrings() {
        UpdateItemDto updateDto = new UpdateItemDto("", "", null);

        Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

        assertThat("Should return the same item instance", updatedItem, is(sameInstance(testItem)));
        assertThat("Item name should be updated to empty string", updatedItem,
            hasProperty("name", equalTo("")));
        assertThat("Item description should be updated to empty string", updatedItem,
            hasProperty("description", equalTo("")));
        assertThat("Item availability should remain unchanged", updatedItem,
            hasProperty("available", equalTo(true)));
    }
}