package ru.practicum.shareit.server.item;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.dto.booking.BookingShortDto;
import ru.practicum.shareit.common.dto.item.CommentDto;
import ru.practicum.shareit.common.dto.item.ItemDto;
import ru.practicum.shareit.common.dto.item.ItemWithBookingInfoDto;
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.exception.AccessDeniedException;
import ru.practicum.shareit.server.exception.ItemNotFoundException;
import ru.practicum.shareit.server.exception.UserNotFoundException;
import ru.practicum.shareit.server.item.mapper.CommentMapper;
import ru.practicum.shareit.server.item.mapper.ItemMapper;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Item Service Implementation Tests")
class ItemServiceImplTest {

    private final Long ownerUserId = 1L;
    private final Long otherUserId = 2L;
    private final Long item1Id = 10L;
    private final Long item2Id = 11L;
    private final Long comment1Id = 100L;
    private final Long lastBookingId = 200L;
    private final Long nextBookingId = 201L;
    @Captor
    ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    ArgumentCaptor<Comment> commentArgumentCaptor;
    @Captor
    ArgumentCaptor<LocalDateTime> timeArgumentCaptor;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;
    @InjectMocks
    private ItemServiceImpl itemService;
    private User ownerUser;
    private User otherUser;
    private Item item1;
    private Item item2;
    private ItemDto itemDto1;
    private ItemDto itemDto2;
    private ItemWithBookingInfoDto itemWithBookingInfoDto1;
    private NewItemDto newItemDto;
    private UpdateItemDto updateItemDto;
    private Comment comment1;
    private CommentDto commentDto1;
    private NewCommentDto newCommentDto;
    private BookingShortDto lastBookingDto;
    private BookingShortDto nextBookingDto;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(ownerUserId);
        ownerUser.setName("Owner");
        ownerUser.setEmail("owner@example.com");

        otherUser = new User();
        otherUser.setId(otherUserId);
        otherUser.setName("Other");
        otherUser.setEmail("other@example.com");

        item1 = new Item();
        item1.setId(item1Id);
        item1.setName("Item One");
        item1.setDescription("Desc One");
        item1.setAvailable(true);
        item1.setOwner(ownerUser);
        item1.setComments(Collections.emptySet()); // Initialize to avoid NPE

        item2 = new Item();
        item2.setId(item2Id);
        item2.setName("Item Two");
        item2.setDescription("Desc Two");
        item2.setAvailable(false);
        item2.setOwner(ownerUser);
        item2.setComments(Collections.emptySet());

        itemDto1 = new ItemDto(item1Id, "Item One", "Desc One", true);
        itemDto2 = new ItemDto(item2Id, "Item Two", "Desc Two", false);

        // DTO for Item 1 including potential booking info (initially null)
        itemWithBookingInfoDto1 = new ItemWithBookingInfoDto(item1Id, "Item One", "Desc One", true,
            Collections.emptySet(), null, null);

        newItemDto = new NewItemDto();
        newItemDto.setName("New Item");
        newItemDto.setDescription("New Desc");
        newItemDto.setAvailable(true);

        updateItemDto = new UpdateItemDto("Updated Name", "Updated Desc", false);

        comment1 = new Comment();
        comment1.setId(comment1Id);
        comment1.setText("Test Comment");
        comment1.setItem(item1);
        comment1.setAuthor(otherUser); // Comment usually by someone other than owner
        comment1.setCreatedAt(LocalDateTime.now().minusDays(1));

        commentDto1 = new CommentDto(comment1Id, "Test Comment", item1Id, otherUser.getName(),
            comment1.getCreatedAt().toString());

        newCommentDto = new NewCommentDto();
        newCommentDto.setText("A new comment");

        lastBookingDto = new BookingShortDto(lastBookingId, otherUserId, item1Id,
            LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        nextBookingDto = new BookingShortDto(nextBookingId, otherUserId, item1Id,
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
    }

    // --- getAllItems() ---
    @Nested
    @DisplayName("getAllItems Tests")
    class GetAllItemsTests {

        @Test
        @DisplayName("should return all items")
        void getAllItems_shouldReturnAllItemDtos() {
            when(itemRepository.findAll()).thenReturn(List.of(item1, item2));
            when(itemMapper.mapToDto(item1)).thenReturn(itemDto1);
            when(itemMapper.mapToDto(item2)).thenReturn(itemDto2);

            List<ItemDto> result = itemService.getAllItems();

            assertThat(result, hasSize(2));
            assertThat(result, contains(itemDto1, itemDto2));
            verify(itemRepository).findAll();
            verify(itemMapper, times(2)).mapToDto(any(Item.class));
        }

        @Test
        @DisplayName("should return empty list when no items")
        void getAllItems_whenNoItems_shouldReturnEmptyList() {
            when(itemRepository.findAll()).thenReturn(Collections.emptyList());

            List<ItemDto> result = itemService.getAllItems();

            assertThat(result, is(empty()));
            verify(itemRepository).findAll();
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("saveItem Tests")
    class SaveItemTests {

        @Test
        @DisplayName("should save item and return DTO")
        void saveItem_whenUserExists_shouldSaveAndReturnDto() {
            Item itemToSave = new Item(); // Item mapped from DTO (no ID, no owner yet)
            itemToSave.setName(newItemDto.getName());
            itemToSave.setDescription(newItemDto.getDescription());
            itemToSave.setAvailable(newItemDto.getAvailable());

            Item savedItem = new Item(); // Item after save (with ID and owner)
            savedItem.setId(item1Id);
            savedItem.setName(newItemDto.getName());
            savedItem.setDescription(newItemDto.getDescription());
            savedItem.setAvailable(newItemDto.getAvailable());
            savedItem.setOwner(ownerUser);

            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemMapper.mapToItem(newItemDto)).thenReturn(itemToSave);
            when(itemRepository.save(any(Item.class))).thenReturn(
                savedItem); // Return the fully formed saved item
            when(itemMapper.mapToDto(savedItem)).thenReturn(
                itemDto1); // Assume DTO matches saved item

            ItemDto result = itemService.saveItem(newItemDto, ownerUserId);

            assertThat(result, equalTo(itemDto1));
            verify(userRepository).findById(ownerUserId);
            verify(itemMapper).mapToItem(newItemDto);
            verify(itemRepository).save(itemArgumentCaptor.capture());
            Item capturedItem = itemArgumentCaptor.getValue();
            assertThat(capturedItem.getOwner(),
                equalTo(ownerUser)); // Verify owner was set before saving
            assertThat(capturedItem.getName(), equalTo(newItemDto.getName()));
            verify(itemMapper).mapToDto(savedItem);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when owner not found")
        void saveItem_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> itemService.saveItem(newItemDto, ownerUserId));

            verify(userRepository).findById(ownerUserId);
            verify(itemMapper, never()).mapToItem(any());
            verify(itemRepository, never()).save(any());
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("getItemById Tests")
    class GetItemByIdTests {

        @Test
        @DisplayName("should return ItemDto when item found")
        void getItemById_whenFound_shouldReturnDto() {
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(itemMapper.mapToDto(item1)).thenReturn(itemDto1);

            ItemDto result = itemService.getItemById(item1Id);

            assertThat(result, equalTo(itemDto1));
            verify(itemRepository).findById(item1Id);
            verify(itemMapper).mapToDto(item1);
        }

        @Test
        @DisplayName("should throw ItemNotFoundException when item not found")
        void getItemById_whenNotFound_shouldThrowItemNotFoundException() {
            when(itemRepository.findById(item1Id)).thenReturn(Optional.empty());

            assertThrows(ItemNotFoundException.class, () -> itemService.getItemById(item1Id));

            verify(itemRepository).findById(item1Id);
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        @Test
        @DisplayName("should update item and return DTO when user is owner")
        void update_whenUserIsOwner_shouldUpdateAndReturnDto() {
            Item updatedItem = new Item();
            updatedItem.setId(item1Id);
            updatedItem.setName(updateItemDto.getName());
            updatedItem.setDescription(updateItemDto.getDescription());
            updatedItem.setAvailable(updateItemDto.getAvailable());
            updatedItem.setOwner(ownerUser);

            ItemDto finalDto = new ItemDto(item1Id, updateItemDto.getName(),
                updateItemDto.getDescription(), updateItemDto.getAvailable());

            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1)); // Existing item
            when(itemMapper.updateItemFields(updateItemDto, item1)).thenReturn(updatedItem);
            when(itemRepository.save(updatedItem)).thenReturn(updatedItem);
            when(itemMapper.mapToDto(updatedItem)).thenReturn(finalDto);

            ItemDto result = itemService.update(updateItemDto, ownerUserId, item1Id);

            assertThat(result, equalTo(finalDto));
            verify(itemRepository).findById(item1Id);
            verify(itemMapper).updateItemFields(updateItemDto, item1);
            verify(itemRepository).save(updatedItem);
            verify(itemMapper).mapToDto(updatedItem);
        }

        @Test
        @DisplayName("should throw ItemNotFoundException when item not found")
        void update_whenItemNotFound_shouldThrowItemNotFoundException() {
            when(itemRepository.findById(item1Id)).thenReturn(Optional.empty());

            assertThrows(ItemNotFoundException.class,
                () -> itemService.update(updateItemDto, ownerUserId, item1Id));

            verify(itemRepository).findById(item1Id);
            verify(itemMapper, never()).updateItemFields(any(), any());
            verify(itemRepository, never()).save(any());
            verify(itemMapper, never()).mapToDto(any());
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not owner")
        void update_whenUserIsNotOwner_shouldThrowAccessDeniedException() {
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));

            assertThrows(AccessDeniedException.class,
                () -> itemService.update(updateItemDto, otherUserId, item1Id));

            verify(itemRepository).findById(item1Id);
            verify(itemMapper, never()).updateItemFields(any(), any());
            verify(itemRepository, never()).save(any());
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("getItemsByUserId Tests")
    class GetItemsByUserIdTests {

        @Test
        @DisplayName("should return items for existing user")
        void getItemsByUserId_whenUserExists_shouldReturnItems() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findByOwnerId(ownerUserId)).thenReturn(List.of(item1, item2));
            when(itemMapper.mapToDto(item1)).thenReturn(itemDto1);
            when(itemMapper.mapToDto(item2)).thenReturn(itemDto2);

            List<ItemDto> result = itemService.getItemsByUserId(ownerUserId);

            assertThat(result, hasSize(2));
            assertThat(result, contains(itemDto1, itemDto2));
            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findByOwnerId(ownerUserId);
            verify(itemMapper, times(2)).mapToDto(any(Item.class));
        }

        @Test
        @DisplayName("should return empty list for user with no items")
        void getItemsByUserId_whenUserHasNoItems_shouldReturnEmptyList() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findByOwnerId(ownerUserId)).thenReturn(Collections.emptyList());

            List<ItemDto> result = itemService.getItemsByUserId(ownerUserId);

            assertThat(result, is(empty()));
            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findByOwnerId(ownerUserId);
            verify(itemMapper, never()).mapToDto(any());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void getItemsByUserId_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> itemService.getItemsByUserId(ownerUserId));

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository, never()).findByOwnerId(anyLong());
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("should delete item when user is owner")
        void delete_whenUserIsOwner_shouldDeleteItem() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));

            assertDoesNotThrow(() -> itemService.delete(item1Id, ownerUserId));

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findById(item1Id);
            verify(itemRepository).deleteById(item1Id);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void delete_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> itemService.delete(item1Id, ownerUserId));

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository, never()).findById(anyLong());
            verify(itemRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("should throw ItemNotFoundException when item not found")
        void delete_whenItemNotFound_shouldThrowItemNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.empty());

            assertThrows(ItemNotFoundException.class,
                () -> itemService.delete(item1Id, ownerUserId));

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findById(item1Id);
            verify(itemRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not owner")
        void delete_whenUserIsNotOwner_shouldThrowAccessDeniedException() {
            when(userRepository.findById(otherUserId)).thenReturn(
                Optional.of(otherUser)); // other user trying to delete
            when(itemRepository.findById(item1Id)).thenReturn(
                Optional.of(item1)); // item owned by ownerUser

            assertThrows(AccessDeniedException.class,
                () -> itemService.delete(item1Id, otherUserId));

            verify(userRepository).findById(otherUserId);
            verify(itemRepository).findById(item1Id);
            verify(itemRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("searchItems Tests")
    class SearchItemsTests {

        @Test
        @DisplayName("should return items matching query")
        void searchItems_whenQueryNotBlankAndUserExists_shouldReturnMatchingItems() {
            String query = "One";
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.search(query)).thenReturn(
                List.of(item1)); // Only item1 matches "One"
            when(itemMapper.mapToDto(item1)).thenReturn(itemDto1);

            List<ItemDto> result = itemService.searchItems(query, otherUserId);

            assertThat(result, hasSize(1));
            assertThat(result, contains(itemDto1));
            verify(userRepository).findById(otherUserId);
            verify(itemRepository).search(query);
            verify(itemMapper).mapToDto(item1);
        }

        @Test
        @DisplayName("should return empty list when query matches no items")
        void searchItems_whenQueryMatchesNothing_shouldReturnEmptyList() {
            String query = "NonExistent";
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.search(query)).thenReturn(Collections.emptyList());

            List<ItemDto> result = itemService.searchItems(query, otherUserId);

            assertThat(result, is(empty()));
            verify(userRepository).findById(otherUserId);
            verify(itemRepository).search(query);
            verify(itemMapper, never()).mapToDto(any());
        }

        @Test
        @DisplayName("should return empty list when query is blank")
        void searchItems_whenQueryIsBlank_shouldReturnEmptyList() {
            String query = "   ";
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));

            List<ItemDto> result = itemService.searchItems(query, otherUserId);

            assertThat(result, is(empty()));
            verify(userRepository).findById(otherUserId);
            verify(itemRepository, never()).search(anyString());
            verify(itemMapper, never()).mapToDto(any());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void searchItems_whenUserNotFound_shouldThrowUserNotFoundException() {
            String query = "test";
            when(userRepository.findById(otherUserId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> itemService.searchItems(query, otherUserId));

            verify(userRepository).findById(otherUserId);
            verify(itemRepository, never()).search(anyString());
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("saveComment Tests")
    class SaveCommentTests {

        @Test
        @DisplayName("should save comment when user exists, item exists, and user booked item")
        void saveComment_whenValid_shouldSaveAndReturnDto() {
            Comment commentToSave = new Comment();
            commentToSave.setText(newCommentDto.getText());

            Comment savedComment = new Comment();
            savedComment.setId(comment1Id);
            savedComment.setText(newCommentDto.getText());
            savedComment.setItem(item1);
            savedComment.setAuthor(otherUser);
            savedComment.setCreatedAt(LocalDateTime.now());

            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                eq(List.of(item1Id)), any(LocalDateTime.class))).thenReturn(
                List.of(lastBookingDto));
            when(commentMapper.mapToComment(newCommentDto)).thenReturn(commentToSave);
            when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
            when(commentMapper.mapToDto(savedComment)).thenReturn(commentDto1);

            CommentDto result = itemService.saveComment(newCommentDto, item1Id, otherUserId);

            assertThat(result, equalTo(commentDto1));
            verify(userRepository).findById(otherUserId);
            verify(itemRepository).findById(item1Id);
            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(
                eq(List.of(item1Id)), any(LocalDateTime.class));
            verify(commentMapper).mapToComment(newCommentDto);
            verify(commentRepository).save(commentArgumentCaptor.capture());
            Comment capturedComment = commentArgumentCaptor.getValue();
            assertThat(capturedComment.getAuthor(), equalTo(otherUser));
            assertThat(capturedComment.getItem(), equalTo(item1));
            assertThat(capturedComment.getText(), equalTo(newCommentDto.getText()));
            verify(commentMapper).mapToDto(savedComment);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void saveComment_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(otherUserId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> itemService.saveComment(newCommentDto, item1Id, otherUserId));

            verifyNoInteractions(itemRepository, bookingRepository, commentRepository,
                commentMapper);
        }

        @Test
        @DisplayName("should throw ItemNotFoundException when item not found")
        void saveComment_whenItemNotFound_shouldThrowItemNotFoundException() {
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.empty());

            assertThrows(ItemNotFoundException.class,
                () -> itemService.saveComment(newCommentDto, item1Id, otherUserId));

            verify(userRepository).findById(otherUserId);
            verify(itemRepository).findById(item1Id);
            verifyNoInteractions(bookingRepository, commentRepository, commentMapper);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when user did not book item")
        void saveComment_whenUserDidNotBookItem_shouldThrowIllegalArgumentException() {
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                eq(List.of(item1Id)), any(LocalDateTime.class))).thenReturn(
                Collections.emptyList());

            assertThrows(IllegalArgumentException.class,
                () -> itemService.saveComment(newCommentDto, item1Id, otherUserId));

            verify(userRepository).findById(otherUserId);
            verify(itemRepository).findById(item1Id);
            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(
                eq(List.of(item1Id)), any(LocalDateTime.class));
            verifyNoInteractions(commentRepository, commentMapper);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when user booked a different item")
        void saveComment_whenUserBookedDifferentItem_shouldThrowIllegalArgumentException() {
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            BookingShortDto bookingDifferentItem = new BookingShortDto(300L, otherUserId, item2Id,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                eq(List.of(item1Id)), any(LocalDateTime.class))).thenReturn(
                Collections.emptyList());

            assertThrows(IllegalArgumentException.class,
                () -> itemService.saveComment(newCommentDto, item1Id, otherUserId));

            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(
                eq(List.of(item1Id)), any(LocalDateTime.class));
            verifyNoInteractions(commentRepository, commentMapper);
        }
    }

    @Nested
    @DisplayName("getItemByIdWithBookingInfo Tests")
    class GetItemByIdWithBookingInfoTests {

        @Test
        @DisplayName("should return item with booking info when user is owner")
        void getItemByIdWithBookingInfo_whenUserIsOwner_shouldReturnDtoWithBookings() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(itemMapper.mapToItemWithBookingInfoDto(item1)).thenReturn(itemWithBookingInfoDto1);
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                eq(List.of(item1Id)), any(LocalDateTime.class))).thenReturn(
                List.of(lastBookingDto));
            when(bookingRepository.findNextApprovedBookingsShortForItems(eq(List.of(item1Id)),
                any(LocalDateTime.class))).thenReturn(List.of(nextBookingDto));

            ItemWithBookingInfoDto result = itemService.getItemByIdWithBookingInfo(item1Id,
                ownerUserId);

            assertThat(result, is(notNullValue()));
            assertThat(result.getId(), equalTo(item1Id));
            assertThat("Last booking should be set", result.getLastBooking(),
                equalTo(lastBookingDto));
            assertThat("Next booking should be set", result.getNextBooking(),
                equalTo(nextBookingDto));
            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findById(item1Id);
            verify(itemMapper).mapToItemWithBookingInfoDto(item1);
            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(
                eq(List.of(item1Id)), any(LocalDateTime.class));
            verify(bookingRepository).findNextApprovedBookingsShortForItems(eq(List.of(item1Id)),
                any(LocalDateTime.class));
        }

        @Test
        @DisplayName("should return item with null booking info when user is owner but no "
            + "bookings exist")
        void getItemByIdWithBookingInfo_whenUserIsOwnerAndNoBookings_shouldReturnDtoWithNullBookings() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(itemMapper.mapToItemWithBookingInfoDto(item1)).thenReturn(itemWithBookingInfoDto1);
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                eq(List.of(item1Id)), any(LocalDateTime.class))).thenReturn(
                Collections.emptyList());
            when(bookingRepository.findNextApprovedBookingsShortForItems(eq(List.of(item1Id)),
                any(LocalDateTime.class))).thenReturn(Collections.emptyList());

            ItemWithBookingInfoDto result = itemService.getItemByIdWithBookingInfo(item1Id,
                ownerUserId);

            assertThat(result, is(notNullValue()));
            assertThat(result.getId(), equalTo(item1Id));
            assertThat("Last booking should be null", result.getLastBooking(), is(nullValue()));
            assertThat("Next booking should be null", result.getNextBooking(), is(nullValue()));
            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(
                eq(List.of(item1Id)), any(LocalDateTime.class));
            verify(bookingRepository).findNextApprovedBookingsShortForItems(eq(List.of(item1Id)),
                any(LocalDateTime.class));
        }

        @Test
        @DisplayName("should return item with null booking info when user is not owner")
        void getItemByIdWithBookingInfo_whenUserIsNotOwner_shouldReturnDtoWithNullBookings() {
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(itemMapper.mapToItemWithBookingInfoDto(item1)).thenReturn(itemWithBookingInfoDto1);

            ItemWithBookingInfoDto result = itemService.getItemByIdWithBookingInfo(item1Id,
                otherUserId);

            assertThat(result, is(notNullValue()));
            assertThat(result.getId(), equalTo(item1Id));
            assertThat("Last booking should be null", result.getLastBooking(), is(nullValue()));
            assertThat("Next booking should be null", result.getNextBooking(), is(nullValue()));
            verify(userRepository).findById(otherUserId);
            verify(itemRepository).findById(item1Id);
            verify(itemMapper).mapToItemWithBookingInfoDto(item1);
            verify(bookingRepository, never()).findPastAndCurrentApprovedBookingsShortForItems(
                anyList(), any(LocalDateTime.class));
            verify(bookingRepository, never()).findNextApprovedBookingsShortForItems(anyList(),
                any(LocalDateTime.class));
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user requesting info not found")
        void getItemByIdWithBookingInfo_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> itemService.getItemByIdWithBookingInfo(item1Id, ownerUserId));
            verify(userRepository).findById(ownerUserId);
            verifyNoInteractions(itemRepository, itemMapper, bookingRepository);
        }

        @Test
        @DisplayName("should throw ItemNotFoundException when item not found")
        void getItemByIdWithBookingInfo_whenItemNotFound_shouldThrowItemNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.empty());

            assertThrows(ItemNotFoundException.class,
                () -> itemService.getItemByIdWithBookingInfo(item1Id, ownerUserId));
            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findById(item1Id);
            verifyNoInteractions(itemMapper, bookingRepository);
        }
    }


    @Nested
    @DisplayName("getAllItemsByOwnerWithBookingInfo Tests")
    class GetAllItemsByOwnerWithBookingInfoTests {

        @Test
        @DisplayName("should return items with booking info for owner")
        void getAllItemsByOwner_whenValid_shouldReturnItemsWithBookings() {
            ItemWithBookingInfoDto itemWithBookingInfoDto2 = new ItemWithBookingInfoDto(item2Id,
                "Item Two", "Desc Two", false, Collections.emptySet(), null, null);

            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findByOwnerId(ownerUserId)).thenReturn(List.of(item1, item2));
            when(itemMapper.mapToItemWithBookingInfoDto(item1)).thenReturn(itemWithBookingInfoDto1);
            when(itemMapper.mapToItemWithBookingInfoDto(item2)).thenReturn(itemWithBookingInfoDto2);
            List<Long> itemIds = List.of(item1Id, item2Id);
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(eq(itemIds),
                any(LocalDateTime.class))).thenReturn(List.of(lastBookingDto));
            when(bookingRepository.findNextApprovedBookingsShortForItems(eq(itemIds),
                any(LocalDateTime.class))).thenReturn(List.of(nextBookingDto));

            List<ItemWithBookingInfoDto> result = itemService.getAllItemsByOwnerWithBookingInfo(
                ownerUserId);

            assertThat(result, hasSize(2));
            ItemWithBookingInfoDto resultItem1 = result.stream()
                .filter(i -> i.getId().equals(item1Id)).findFirst().orElseThrow();
            assertThat(resultItem1.getLastBooking(), equalTo(lastBookingDto));
            assertThat(resultItem1.getNextBooking(), equalTo(nextBookingDto));
            ItemWithBookingInfoDto resultItem2 = result.stream()
                .filter(i -> i.getId().equals(item2Id)).findFirst().orElseThrow();
            assertThat(resultItem2.getLastBooking(), is(nullValue()));
            assertThat(resultItem2.getNextBooking(), is(nullValue()));

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findByOwnerId(ownerUserId);
            verify(itemMapper, times(2)).mapToItemWithBookingInfoDto(any(Item.class));
            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(eq(itemIds),
                timeArgumentCaptor.capture());
            verify(bookingRepository).findNextApprovedBookingsShortForItems(eq(itemIds),
                eq(timeArgumentCaptor.getValue())); // Ensure same 'now' time used
        }

        @Test
        @DisplayName("should return empty list when owner has no items")
        void getAllItemsByOwner_whenOwnerHasNoItems_shouldReturnEmptyList() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findByOwnerId(ownerUserId)).thenReturn(Collections.emptyList());

            List<ItemWithBookingInfoDto> result = itemService.getAllItemsByOwnerWithBookingInfo(
                ownerUserId);

            assertThat(result, is(empty()));
            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findByOwnerId(ownerUserId);
            verifyNoInteractions(itemMapper, bookingRepository);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void getAllItemsByOwner_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> itemService.getAllItemsByOwnerWithBookingInfo(ownerUserId));
            verify(userRepository).findById(ownerUserId);
            verifyNoInteractions(itemRepository, itemMapper, bookingRepository);
        }
    }

}