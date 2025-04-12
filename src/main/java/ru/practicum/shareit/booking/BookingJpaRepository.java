package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unused")
public interface BookingJpaRepository extends BookingRepository, JpaRepository<Booking, Long> {

    @Override
    @Query("SELECT b FROM Booking b " +
        "WHERE b.booker.id = :bookerId " +
        "AND (" +
        "   (:stateName = 'ALL') OR " +
        "   (:stateName = 'CURRENT' AND :now BETWEEN b.startDate AND b.endDate) OR " +
        "   (:stateName = 'PAST' AND b.endDate < :now) OR " +
        "   (:stateName = 'FUTURE' AND b.startDate > :now) OR " +
        "   (:stateName = 'WAITING' AND b.status = ru.practicum.shareit.booking.BookingStatus.WAITING) OR " +
        "   (:stateName = 'REJECTED' AND b.status = ru.practicum.shareit.booking.BookingStatus.REJECTED)" +
        ") " +
        "ORDER BY b.startDate DESC")
    Page<Booking> findBookingsByBookerAndState(
        @Param("bookerId") Long bookerId,
        @Param("stateName") String stateName,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @Override
    @Query("SELECT b FROM Booking b " +
        "WHERE b.item.owner.id = :ownerId " +
        "AND (" +
        "   (:stateName = 'ALL') OR " +
        "   (:stateName = 'CURRENT' AND :now BETWEEN b.startDate AND b.endDate) OR " +
        "   (:stateName = 'PAST' AND b.endDate < :now) OR " +
        "   (:stateName = 'FUTURE' AND b.startDate > :now) OR " +
        "   (:stateName = 'WAITING' AND b.status = ru.practicum.shareit.booking.BookingStatus.WAITING) OR " +
        "   (:stateName = 'REJECTED' AND b.status = ru.practicum.shareit.booking.BookingStatus.REJECTED)" +
        ") " +
        "ORDER BY b.startDate DESC")
    Page<Booking> findBookingsByItemOwnerAndState(
        @Param("ownerId") Long ownerId,
        @Param("stateName") String stateName,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

}
