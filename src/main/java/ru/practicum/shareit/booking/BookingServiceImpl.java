package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.UpdateBookingDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BookingBadRequestException;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemStatus;
import ru.practicum.shareit.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public List<BookingDto> getAllBookings() {
        List<BookingDto> bookings = bookingRepository.findAll().stream()
            .map(bookingMapper::mapToDto).toList();
        log.debug("Fetched {} bookings", bookings.size());
        return bookings;
    }

    @Override
    public BookingDto saveBooking(NewBookingDto booking, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        Item item = itemRepository.findById(booking.getItemId()).orElseThrow(() -> {
            log.warn("Item with id {} not found", booking.getItemId());
            return new BookingBadRequestException(
                "Item with id " + booking.getItemId() + " not found");
        });
        if (item.getOwner().getId().equals(userId)) {
            log.warn("User with id {} is the owner of item with id {}", userId, booking.getItemId());
            throw new BookingBadRequestException(
                "User with id " + userId + " is the owner of item with id " + booking.getItemId());
        }
        if (!item.getStatus().equals(ItemStatus.AVAILABLE)) {
            log.warn("Item with id {} is not available", booking.getItemId());
            throw new BookingBadRequestException(
                "Item with id " + booking.getItemId() + " is not available");
        }
        if (booking.getStart().isBefore(LocalDateTime.now())) {
            log.warn("Booking start time {} is in the past", booking.getStart());
            throw new BookingBadRequestException("Booking start time cannot be in the past");
        }
        if (!booking.getEnd().isAfter(booking.getStart())) {
            log.warn("Booking end time {} is not after start time {}", booking.getEnd(),
                booking.getStart());
            throw new BookingBadRequestException("Booking end time must be after start time");
        }
        Booking newBooking = bookingMapper.mapToBooking(booking);
        newBooking.setBooker(userRepository.findById(userId).get());
        Booking savedBooking = bookingRepository.save(newBooking);
        log.debug("Saved new booking: {}", savedBooking);
        return bookingMapper.mapToDto(savedBooking);
    }

    @Override
    public BookingDto getById(Long id) {
        return bookingMapper.mapToDto(bookingRepository.findById(id).orElseThrow(() -> {
            log.warn("Booking with id {} not found", id);
            return new BookingNotFoundException("Booking with id " + id + " not found");
        }));
    }

    @Override
    public BookingDto update(UpdateBookingDto updateBookingDto, Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            log.warn("Booking with id {} not found for update", bookingId);
            return new BookingNotFoundException("Booking with id " + bookingId + " not found");
        });
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("User with id {} is not the owner of item in booking with id {}",
                userId, bookingId);
            throw new AccessDeniedException(
                "User with id " + userId + " is not the owner of item in booking with id"
                    + bookingId);
        }
        Booking updatedBooking = bookingMapper.updateBookingFields(updateBookingDto, booking);
        bookingRepository.save(updatedBooking);
        log.debug("Updated booking: {}", updatedBooking);
        return bookingMapper.mapToDto(updatedBooking);
    }

    @Override
    public void delete(Long id, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                "User with id " + userId + " not found");
        }
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> {
            log.warn("Booking with id {} not found for deletion", id);
            return new BookingNotFoundException("Booking with id " + id + " not found");
        });
        if (!booking.getBooker().getId().equals(userId)) {
            log.warn("Booking with id {} does not belong to user with id {}", id, userId);
            throw new AccessDeniedException(
                "Booking with id " + id + " does not belong to user with id " + userId);
        }
        log.debug("Deleting booking with id {} by user with id {}", id, userId);
        bookingRepository.deleteById(id);
    }

    @Override
    public List<BookingDto> getBookingsByBooker(Long bookerId) {
        return List.of();
    }

    @Override
    public List<BookingDto> getBookingsByOwner(Long ownerId) {
        return List.of();
    }
}
