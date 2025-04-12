package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface BookingRepository {

    Booking save(Booking booking);

    List<Booking> findAll();

    Optional<Booking> findById(Long id);

    void deleteById(Long id);

    Page<Booking> findBookingsByBookerAndState(Long bookerId, String stateName, Timestamp now, Pageable pageable);

    Page<Booking> findBookingsByItemOwnerAndState(Long ownerId, String stateName, Timestamp now, Pageable pageable);

}
