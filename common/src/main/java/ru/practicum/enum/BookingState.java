package ru.practicum; // Or a suitable common package

public enum BookingState {
    ALL,      // All bookings for the user
    CURRENT,  // Start <= now < End
    PAST,     // End < now
    FUTURE,   // Start > now
    WAITING,  // status = WAITING
    REJECTED  // status = REJECTED
}