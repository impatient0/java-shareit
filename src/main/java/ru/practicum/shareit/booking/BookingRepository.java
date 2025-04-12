package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingShortDto;

public interface BookingRepository {

    Booking save(Booking booking);

    List<Booking> findAll();

    Optional<Booking> findById(Long id);

    void deleteById(Long id);

    Page<Booking> findBookingsByBookerAndState(Long bookerId, String stateName, LocalDateTime now, Pageable pageable);

    Page<Booking> findBookingsByItemOwnerAndState(Long ownerId, String stateName, LocalDateTime now, Pageable pageable);

    List<BookingShortDto> findPastAndCurrentApprovedBookingsShortForItems(List<Long> itemIds, LocalDateTime now);

    List<BookingShortDto> findNextApprovedBookingsShortForItems(List<Long> itemIds, LocalDateTime now);
}
