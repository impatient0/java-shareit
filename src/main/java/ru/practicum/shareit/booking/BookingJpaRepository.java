package ru.practicum.shareit.booking;

import java.sql.Timestamp;
import org.springframework.data.domain.Pageable;
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
        "   (:state = ru.practicum.shareit.booking.BookingState.ALL) OR " +
        "   (:state = ru.practicum.shareit.booking.BookingState.CURRENT AND :now BETWEEN b.start_date AND b.end_date) OR " +
        "   (:state = ru.practicum.shareit.booking.BookingState.PAST AND b.end_date < :now) OR " +
        "   (:state = ru.practicum.shareit.booking.BookingState.FUTURE AND b.start_date > :now) OR " +
        "   (:state = ru.practicum.shareit.booking.BookingState.WAITING AND b.status = ru.practicum.shareit.booking.BookingStatus.WAITING) OR " +
        "   (:state = ru.practicum.shareit.booking.BookingState.REJECTED AND b.status = ru.practicum.shareit.booking.BookingStatus.REJECTED)" +
        ") " +
        "ORDER BY b.start_date DESC")
    Page<Booking> findBookingsByBookerAndState(
        @Param("bookerId") Long bookerId,
        @Param("state") BookingState state,
        @Param("now") Timestamp now,
        Pageable pageable
    );

    @Override
    @Query("SELECT b FROM Booking b " +
        "WHERE b.item.owner.id = :ownerId " + // Filter by the item's owner ID
        "AND (" +
        "   (:state = ru.practicum.shareit.booking.BookingState.ALL) OR " +
        "   (:state = ru.practicum.shareit.booking.BookingState.CURRENT AND :now BETWEEN b.start_date AND b.end_date) OR " +
        "   (:state = ru.practicum.shareit.booking.BookingState.PAST AND b.end_date < :now) OR " +
        "   (:state = ru.practicum.shareit.booking.BookingState.FUTURE AND b.start_date > :now) OR " +
        "   (:state = ru.practicum.shareit.booking.BookingState.WAITING AND b.status = ru.practicum.shareit.booking.BookingStatus.WAITING) OR " +
        "   (:state = ru.practicum.shareit.booking.BookingState.REJECTED AND b.status = ru.practicum.shareit.booking.BookingStatus.REJECTED)" +
        ") " +
        "ORDER BY b.start_date DESC")
    Page<Booking> findBookingsByItemOwnerAndState(
        @Param("ownerId") Long ownerId,
        @Param("state") BookingState state,
        @Param("now") Timestamp now,
        Pageable pageable
    );

}
