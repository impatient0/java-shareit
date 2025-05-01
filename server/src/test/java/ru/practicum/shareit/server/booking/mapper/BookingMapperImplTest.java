package ru.practicum.shareit.server.booking.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.dto.booking.BookingDto;
import ru.practicum.shareit.common.dto.booking.NewBookingDto;
import ru.practicum.shareit.common.dto.item.ItemDto;
import ru.practicum.shareit.common.dto.user.UserDto;
import ru.practicum.shareit.common.enums.BookingStatus;
import ru.practicum.shareit.server.booking.Booking;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.item.mapper.ItemMapper;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.mapper.UserMapper;

@DisplayName("Booking Mapper Implementation Tests")
@ExtendWith(MockitoExtension.class)
class BookingMapperImplTest {

    @Mock
    private ItemMapper itemMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BookingMapperImpl bookingMapper;

    private Item testItem;
    private User testBooker;
    private Booking testBooking;
    private ItemDto testItemDto;
    private UserDto testUserDto;
    private LocalDateTime testStart;
    private LocalDateTime testEnd;

    @BeforeEach
    void setUp() {
        testStart = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS);
        testEnd = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS);

        testItem = new Item();
        testItem.setId(10L);
        testItem.setName("Test Item");
        testItem.setDescription("Desc");
        testItem.setAvailable(true);
        User owner = new User();
        owner.setId(1L);
        testItem.setOwner(owner);

        testBooker = new User();
        testBooker.setId(2L);
        testBooker.setName("Test Booker");
        testBooker.setEmail("booker@example.com");

        testBooking = new Booking();
        testBooking.setId(100L);
        testBooking.setStartDate(testStart);
        testBooking.setEndDate(testEnd);
        testBooking.setItem(testItem);
        testBooking.setBooker(testBooker);
        testBooking.setStatus(BookingStatus.WAITING);

        testItemDto = new ItemDto(testItem.getId(), testItem.getName(), testItem.getDescription(),
            testItem.getAvailable());
        testUserDto = new UserDto(testBooker.getId(), testBooker.getName(), testBooker.getEmail());
    }

    @Test
    @DisplayName("mapToDto should map Booking to BookingDto correctly")
    void mapToDto_whenBookingIsValid_shouldReturnCorrectBookingDto() {
        when(itemMapper.mapToDto(testItem)).thenReturn(testItemDto);
        when(userMapper.mapToDto(testBooker)).thenReturn(testUserDto);

        BookingDto bookingDto = bookingMapper.mapToDto(testBooking);

        assertThat("Mapped DTO should not be null", bookingDto, is(notNullValue()));
        assertThat("Mapped DTO should have correct ID", bookingDto,
            hasProperty("id", equalTo(100L)));
        assertThat("Mapped DTO should have correct start date", bookingDto,
            hasProperty("start", equalTo(testStart)));
        assertThat("Mapped DTO should have correct end date", bookingDto,
            hasProperty("end", equalTo(testEnd)));
        assertThat("Mapped DTO should contain correct ItemDto", bookingDto,
            hasProperty("item", is(sameInstance(testItemDto))));
        assertThat("Mapped DTO should contain correct UserDto (Booker)", bookingDto,
            hasProperty("booker", is(sameInstance(testUserDto))));
        assertThat("Mapped DTO should have correct status string", bookingDto,
            hasProperty("status", equalTo(BookingStatus.WAITING.toString())));

        verify(itemMapper).mapToDto(testItem);
        verify(userMapper).mapToDto(testBooker);
    }

    @Test
    @DisplayName("mapToDto should map different BookingStatus values correctly")
    void mapToDto_whenStatusIsApproved_shouldMapStatusToString() {
        testBooking.setStatus(BookingStatus.APPROVED);
        when(itemMapper.mapToDto(testItem)).thenReturn(testItemDto); // Still need mocks
        when(userMapper.mapToDto(testBooker)).thenReturn(testUserDto);

        BookingDto bookingDto = bookingMapper.mapToDto(testBooking);

        assertThat("Mapped DTO status should be 'APPROVED'", bookingDto,
            hasProperty("status", equalTo(BookingStatus.APPROVED.toString())));
    }

    @Test
    @DisplayName("mapToDto should throw NullPointerException if Status is null")
    void mapToDto_whenStatusIsNull_shouldThrowNullPointerException() {
        testBooking.setStatus(null);
        when(itemMapper.mapToDto(testItem)).thenReturn(testItemDto);
        when(userMapper.mapToDto(testBooker)).thenReturn(testUserDto);

        assertThrows(NullPointerException.class, () -> bookingMapper.mapToDto(testBooking),
            "Should throw NullPointerException when Status is null");
    }

    @Test
    @DisplayName("mapToBooking should map NewBookingDto to partial Booking correctly")
    void mapToBooking_whenNewBookingDtoIsValid_shouldReturnPartialBooking() {
        Long newItemId = 50L; // Note: This ID is in the DTO but NOT used by the mapper
        NewBookingDto newBookingDto = new NewBookingDto(newItemId, testStart, testEnd);

        Booking booking = bookingMapper.mapToBooking(newBookingDto);

        assertThat("Mapped Booking should not be null", booking, is(notNullValue()));
        assertThat("Mapped Booking should have correct start date", booking,
            hasProperty("startDate", equalTo(testStart)));
        assertThat("Mapped Booking should have correct end date", booking,
            hasProperty("endDate", equalTo(testEnd)));
        assertThat("Mapped Booking status should be WAITING", booking,
            hasProperty("status", equalTo(BookingStatus.WAITING)));
        assertThat("Mapped Booking ID should be null", booking, hasProperty("id", is(nullValue())));
        assertThat("Mapped Booking Item should be null", booking,
            hasProperty("item", is(nullValue())));
        assertThat("Mapped Booking Booker should be null", booking,
            hasProperty("booker", is(nullValue())));
    }

    @Test
    @DisplayName("mapToBooking should map null dates from NewBookingDto")
    void mapToBooking_whenDatesAreNull_shouldMapNullDates() {
        NewBookingDto newBookingDto = new NewBookingDto(50L, null, null);

        Booking booking = bookingMapper.mapToBooking(newBookingDto);

        assertThat("Mapped Booking should not be null", booking, is(notNullValue()));
        assertThat("Mapped Booking start date should be null", booking,
            hasProperty("startDate", is(nullValue())));
        assertThat("Mapped Booking end date should be null", booking,
            hasProperty("endDate", is(nullValue())));
        assertThat("Mapped Booking status should still be WAITING", booking,
            hasProperty("status", equalTo(BookingStatus.WAITING)));
        assertThat("Mapped Booking Item should be null", booking,
            hasProperty("item", is(nullValue())));
        assertThat("Mapped Booking Booker should be null", booking,
            hasProperty("booker", is(nullValue())));
    }
}