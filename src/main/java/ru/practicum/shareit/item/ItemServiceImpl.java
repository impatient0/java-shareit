package ru.practicum.shareit.item;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemWithBookingInfoDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    public List<ItemDto> getAllItems() {
        List<ItemDto> items = itemRepository.findAll().stream().map(itemMapper::mapToDto).toList();
        log.debug("Fetched {} items", items.size());
        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemWithBookingInfoDto> getAllItemsByOwnerWithBookingInfo(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        LocalDateTime now = LocalDateTime.now();

        List<BookingShortDto> pastAndCurrentBookings = bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(itemIds, now);
        List<BookingShortDto> nextBookings = bookingRepository.findNextApprovedBookingsShortForItems(itemIds, now);

        Map<Long, List<BookingShortDto>> bookingsByItemId = pastAndCurrentBookings.stream()
            .collect(Collectors.groupingBy(BookingShortDto::getItemId));

        Map<Long, List<BookingShortDto>> nextBookingsByItemId = nextBookings.stream()
            .collect(Collectors.groupingBy(BookingShortDto::getItemId));

        log.debug("Fetched {} items with booking info for user with id {}", items.size(), userId);

        return items.stream().map(item -> {
            ItemWithBookingInfoDto dto = itemMapper.mapToItemWithBookingInfoDto(item);

            BookingShortDto lastBookingDto = bookingsByItemId.getOrDefault(item.getId(), Collections.emptyList())
                .stream()
                .findFirst()
                .orElse(null);

            BookingShortDto nextBookingDto = nextBookingsByItemId.getOrDefault(item.getId(), Collections.emptyList())
                .stream()
                .findFirst()
                .orElse(null);

            dto.setLastBooking(lastBookingDto);
            dto.setNextBooking(nextBookingDto);

            return dto;
        }).toList();
    }

    @Override
    public ItemDto saveItem(NewItemDto newItemDto, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        Item item = itemMapper.mapToItem(newItemDto);
        item.setOwner(userRepository.findById(userId).get());
        Item savedItem = itemRepository.save(item);
        log.debug("Saved new item: {}", savedItem);
        return itemMapper.mapToDto(savedItem);
    }

    @Override
    public ItemDto getItemById(Long id) {
        return itemMapper.mapToDto(itemRepository.findById(id).orElseThrow(() -> {
            log.warn("Item with id {} not found", id);
            return new ItemNotFoundException(
                "Item with id " + id + " not found");
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemWithBookingInfoDto getItemByIdWithBookingInfo(Long itemId, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found when requested by user {}", itemId, userId);
            return new ItemNotFoundException("Item with id " + itemId + " not found");
        });
        ItemWithBookingInfoDto itemDto = itemMapper.mapToItemWithBookingInfoDto(item);
        if (item.getOwner().getId().equals(userId)) {
            log.debug("User {} is owner of item {}. Fetching booking info.", userId, itemId);

            LocalDateTime now = LocalDateTime.now();
            List<BookingShortDto> pastOrCurrentBookings = bookingRepository
                .findPastAndCurrentApprovedBookingsShortForItems(Collections.singletonList(itemId), now);
            List<BookingShortDto> nextBookings = bookingRepository
                .findNextApprovedBookingsShortForItems(Collections.singletonList(itemId), now);

            BookingShortDto lastBookingDto = pastOrCurrentBookings.stream()
                .findFirst()
                .orElse(null);

            BookingShortDto nextBookingDto = nextBookings.stream()
                .findFirst()
                .orElse(null);

            itemDto.setLastBooking(lastBookingDto);
            itemDto.setNextBooking(nextBookingDto);
        } else {
            log.debug("User {} is not owner of item {}. Skipping booking info.", userId, itemId);
        }
        return itemDto;
    }

    @Override
    public ItemDto update(UpdateItemDto updateItemDto, Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found for update", itemId);
            return new ItemNotFoundException(
                "Item with id " + itemId + " not found");
        });
        if (!item.getOwner().getId().equals(userId)) {
            log.warn("User with id {} does not own item with id {}", userId, itemId);
            throw new AccessDeniedException(
                "User with id " + userId + " does not own item with id " + itemId);
        }
        Item updatedItem = itemMapper.updateItemFields(updateItemDto, item);
        itemRepository.save(updatedItem);
        log.debug("Updated item: {}", updatedItem);
        return itemMapper.mapToDto(updatedItem);
    }

    @Override
    public List<ItemDto> getItemsByUserId(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        List<ItemDto> items = itemRepository.findByOwnerId(userId).stream().map(itemMapper::mapToDto)
            .toList();
        log.debug("Fetched {} items for user with id {}", items.size(), userId);
        return items;
    }

    @Override
    public void delete(Long id, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        Item item = itemRepository.findById(id).orElseThrow(() -> {
            log.warn("Item with id {} not found for delete", id);
            return new ItemNotFoundException(
                "Item with id " + id + " not found");
        });
        if (!item.getOwner().getId().equals(userId)) {
            log.warn("User with id {} does not own item with id {}", userId, id);
            throw new AccessDeniedException(
                "User with id " + userId + " does not own item with id " + id);
        }
        log.debug("Deleting item with id {} by user with id {}", id, userId);
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> searchItems(String query, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        if (query.isBlank()) {
            log.debug("Search query is blank, returning empty list");
            return List.of();
        }
        // Shouldn't we filter out user's own items when searching?..
        List<ItemDto> items = itemRepository.search(query).stream().map(itemMapper::mapToDto)
            .toList();
        log.debug("Found {} items by query: {}", items.size(), query);
        return items;
    }

    @Override
    public CommentDto saveComment(NewCommentDto newCommentDto, Long itemId, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found", itemId);
            return new ItemNotFoundException(
                "Item with id " + itemId + " not found");
        });
        List<BookingShortDto> bookings = bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
            List.of(itemId), LocalDateTime.now());
        if (bookings.stream().noneMatch(b -> b.getBookerId().equals(userId))) {
            log.warn("User with id {} did not book item with id {}", userId, itemId);
            throw new IllegalArgumentException(
                "User with id " + userId + " did not book item with id " + itemId);
        }
        Comment comment = commentMapper.mapToComment(newCommentDto);
        comment.setItem(item);
        comment.setAuthor(userRepository.findById(userId).get());
        Comment savedComment = commentRepository.save(comment);
        log.debug("Saved new comment: {}", savedComment);
        return commentMapper.mapToDto(savedComment);
    }
}
