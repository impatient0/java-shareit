package ru.practicum.shareit.server.booking;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.practicum.shareit.common.dto.booking.BookingShortDto;
import ru.practicum.shareit.common.enums.BookingStatus;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.user.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DisplayName("Booking Repository DataJpa Tests")
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"));

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }
    private User owner;
    private User booker1;
    private User booker2;
    private Item item1;
    private Item item2;

    private Booking booking1Past;
    private Booking booking2Current;
    private Booking booking3Future;
    private Booking booking4Rejected;
    private Booking booking5OtherUser;
    private Booking booking6OwnerItem;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner = entityManager.persistAndFlush(owner);

        booker1 = new User();
        booker1.setName("Booker One");
        booker1.setEmail("booker1@example.com");
        booker1 = entityManager.persistAndFlush(booker1);

        booker2 = new User();
        booker2.setName("Booker Two");
        booker2.setEmail("booker2@example.com");
        booker2 = entityManager.persistAndFlush(booker2);

        item1 = new Item();
        item1.setName("Item One");
        item1.setDescription("Owned by Owner");
        item1.setAvailable(true);
        item1.setOwner(owner);
        item1 = entityManager.persistAndFlush(item1);

        item2 = new Item();
        item2.setName("Item Two");
        item2.setDescription("Owned by Booker One");
        item2.setAvailable(true);
        item2.setOwner(booker1);
        item2 = entityManager.persistAndFlush(item2);

        booking1Past = new Booking();
        booking1Past.setBooker(booker1);
        booking1Past.setItem(item1);
        booking1Past.setStartDate(now.minusDays(5));
        booking1Past.setEndDate(now.minusDays(4));
        booking1Past.setStatus(BookingStatus.APPROVED);

        booking2Current = new Booking();
        booking2Current.setBooker(booker1);
        booking2Current.setItem(item1);
        booking2Current.setStartDate(now.minusHours(1));
        booking2Current.setEndDate(now.plusHours(1));
        booking2Current.setStatus(BookingStatus.APPROVED);

        booking3Future = new Booking();
        booking3Future.setBooker(booker1);
        booking3Future.setItem(item1);
        booking3Future.setStartDate(now.plusDays(1));
        booking3Future.setEndDate(now.plusDays(2));
        booking3Future.setStatus(BookingStatus.WAITING);

        booking4Rejected = new Booking();
        booking4Rejected.setBooker(booker1);
        booking4Rejected.setItem(item1);
        booking4Rejected.setStartDate(now.plusDays(3));
        booking4Rejected.setEndDate(now.plusDays(4));
        booking4Rejected.setStatus(BookingStatus.REJECTED);

        booking5OtherUser = new Booking();
        booking5OtherUser.setBooker(booker2);
        booking5OtherUser.setItem(item1);
        booking5OtherUser.setStartDate(now.plusDays(5));
        booking5OtherUser.setEndDate(now.plusDays(6));
        booking5OtherUser.setStatus(BookingStatus.APPROVED);

        booking6OwnerItem = new Booking();
        booking6OwnerItem.setBooker(booker2);
        booking6OwnerItem.setItem(item2);
        booking6OwnerItem.setStartDate(now.plusDays(7));
        booking6OwnerItem.setEndDate(now.plusDays(8));
        booking6OwnerItem.setStatus(BookingStatus.WAITING);

        entityManager.persist(booking1Past);
        entityManager.persist(booking2Current);
        entityManager.persist(booking3Future);
        entityManager.persist(booking4Rejected);
        entityManager.persist(booking5OtherUser);
        entityManager.persist(booking6OwnerItem);
        entityManager.flush();
    }

    @Test
    @DisplayName("findBookingsByBookerAndState (ALL) should return all bookings for booker1")
    void findByBooker_StateALL_shouldReturnAllBooker1Bookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());
        Page<Booking> result = bookingRepository.findBookingsByBookerAndState(booker1.getId(), "ALL", now, pageable);

        assertThat(result.getContent(), hasSize(4));
        assertThat(result.getContent(), containsInAnyOrder(booking1Past, booking2Current, booking3Future, booking4Rejected));
        assertThat(result.getTotalElements(), equalTo(4L));
    }

    @Test
    @DisplayName("findBookingsByBookerAndState (CURRENT) should return current booking")
    void findByBooker_StateCURRENT_shouldReturnCurrentBooking() {
        Pageable pageable = Pageable.unpaged();
        Page<Booking> result = bookingRepository.findBookingsByBookerAndState(booker1.getId(), "CURRENT", now, pageable);

        assertThat(result.getContent(), hasSize(1));
        assertThat(result.getContent().getFirst(), equalTo(booking2Current));
    }

    @Test
    @DisplayName("findBookingsByBookerAndState (PAST) should return past booking")
    void findByBooker_StatePAST_shouldReturnPastBooking() {
        Pageable pageable = Pageable.unpaged();
        Page<Booking> result = bookingRepository.findBookingsByBookerAndState(booker1.getId(), "PAST", now, pageable);

        assertThat(result.getContent(), hasSize(1));
        assertThat(result.getContent().getFirst(), equalTo(booking1Past));
    }

    @Test
    @DisplayName("findBookingsByBookerAndState (FUTURE) should return future bookings")
    void findByBooker_StateFUTURE_shouldReturnFutureBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());
        Page<Booking> result = bookingRepository.findBookingsByBookerAndState(booker1.getId(), "FUTURE", now, pageable);

        assertThat(result.getContent(), hasSize(2));
        assertThat(result.getContent(), containsInAnyOrder(booking3Future, booking4Rejected));
    }

    @Test
    @DisplayName("findBookingsByBookerAndState (WAITING) should return waiting booking")
    void findByBooker_StateWAITING_shouldReturnWaitingBooking() {
        Pageable pageable = Pageable.unpaged();
        Page<Booking> result = bookingRepository.findBookingsByBookerAndState(booker1.getId(), "WAITING", now, pageable);

        assertThat(result.getContent(), hasSize(1));
        assertThat(result.getContent().getFirst(), equalTo(booking3Future));
    }

    @Test
    @DisplayName("findBookingsByBookerAndState (REJECTED) should return rejected booking")
    void findByBooker_StateREJECTED_shouldReturnRejectedBooking() {
        Pageable pageable = Pageable.unpaged();
        Page<Booking> result = bookingRepository.findBookingsByBookerAndState(booker1.getId(), "REJECTED", now, pageable);

        assertThat(result.getContent(), hasSize(1));
        assertThat(result.getContent().getFirst(), equalTo(booking4Rejected));
    }

    @Test
    @DisplayName("findBookingsByBookerAndState should apply pagination")
    void findByBooker_StateALL_WithPagination_shouldReturnPaginated() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("startDate").descending());
        Page<Booking> result = bookingRepository.findBookingsByBookerAndState(booker1.getId(), "ALL", now, pageable);

        assertThat(result.getContent(), hasSize(2));
        assertThat(result.getContent().get(0).getStatus(), equalTo(BookingStatus.REJECTED));
        assertThat(result.getContent().get(1).getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(result.getTotalPages(), equalTo(2));
        assertThat(result.getTotalElements(), equalTo(4L));
    }

    @Test
    @DisplayName("findBookingsByItemOwnerAndState (ALL) should return all bookings for owner's items")
    void findByOwner_StateALL_shouldReturnAllOwnerItemBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());
        Page<Booking> result = bookingRepository.findBookingsByItemOwnerAndState(owner.getId(), "ALL", now, pageable);

        assertThat(result.getContent(), hasSize(5));
        assertThat(result.getContent(), containsInAnyOrder(booking1Past, booking2Current, booking3Future, booking4Rejected, booking5OtherUser));
        assertThat(result.getTotalElements(), equalTo(5L));
    }

    @Test
    @DisplayName("findBookingsByItemOwnerAndState (WAITING) should return waiting bookings for owner's items")
    void findByOwner_StateWAITING_shouldReturnWaitingBooking() {
        Pageable pageable = Pageable.unpaged();
        Page<Booking> result = bookingRepository.findBookingsByItemOwnerAndState(owner.getId(), "WAITING", now, pageable);

        assertThat(result.getContent(), hasSize(1));
        assertThat(result.getContent().getFirst(), equalTo(booking3Future));
    }

    @Test
    @DisplayName("findBookingsByItemOwnerAndState (FUTURE) should return future bookings for owner's items")
    void findByOwner_StateFUTURE_shouldReturnFutureBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());
        Page<Booking> result = bookingRepository.findBookingsByItemOwnerAndState(owner.getId(), "FUTURE", now, pageable);

        assertThat(result.getContent(), hasSize(3));
        assertThat(result.getContent(), containsInAnyOrder(booking3Future, booking4Rejected, booking5OtherUser));
    }

    @Test
    @DisplayName("findBookingsByItemOwnerAndState for owner of item2 (booker1)")
    void findByOwner_OwnerIsBooker1_StateWAITING_shouldReturnBooking6() {
        Pageable pageable = Pageable.unpaged();
        Page<Booking> result = bookingRepository.findBookingsByItemOwnerAndState(booker1.getId(), "WAITING", now, pageable);

        assertThat(result.getContent(), hasSize(1));
        assertThat(result.getContent().getFirst(), equalTo(booking6OwnerItem));
    }

    @Test
    @DisplayName("findPastAndCurrentApprovedBookingsShortForItems should return past and current approved")
    void findPastCurrentApproved_shouldReturnCorrectDtos() {
        List<BookingShortDto> result = bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(List.of(item1.getId()), now);

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(booking2Current.getId()));
        assertThat(result.get(1).getId(), equalTo(booking1Past.getId()));
        BookingShortDto dto = result.stream().filter(d -> d.getId().equals(booking2Current.getId())).findFirst().orElseThrow();
        assertThat(dto.getBookerId(), equalTo(booker1.getId()));
        assertThat(dto.getItemId(), equalTo(item1.getId()));
        assertThat(dto.getStart(), equalTo(booking2Current.getStartDate()));
        assertThat(dto.getEnd(), equalTo(booking2Current.getEndDate()));
    }

    @Test
    @DisplayName("findPastAndCurrentApprovedBookingsShortForItems should ignore future/non-approved")
    void findPastCurrentApproved_shouldIgnoreFutureAndNonApproved() {
        List<BookingShortDto> result = bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(List.of(item1.getId()), now);

        assertThat(result, hasSize(2));
        assertTrue(result.stream().noneMatch(dto -> dto.getId().equals(booking3Future.getId())));
        assertTrue(result.stream().noneMatch(dto -> dto.getId().equals(booking4Rejected.getId())));
        assertTrue(result.stream().noneMatch(dto -> dto.getId().equals(booking5OtherUser.getId())));
    }

    @Test
    @DisplayName("findPastAndCurrentApprovedBookingsShortForItems handles multiple items")
    void findPastCurrentApproved_whenMultipleItems_shouldReturnCorrectDtos() {
        List<BookingShortDto> result = bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(List.of(item1.getId(), item2.getId()), now);

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(booking2Current.getId()));
        assertThat(result.get(1).getId(), equalTo(booking1Past.getId()));
    }

    @Test
    @DisplayName("findNextApprovedBookingsShortForItems should return future approved")
    void findNextApproved_shouldReturnCorrectDtos() {
        List<BookingShortDto> result = bookingRepository.findNextApprovedBookingsShortForItems(List.of(item1.getId()), now);

        assertThat(result, hasSize(1));
        assertThat(result.getFirst().getId(), equalTo(booking5OtherUser.getId()));
        BookingShortDto dto = result.getFirst();
        assertThat(dto.getBookerId(), equalTo(booker2.getId()));
        assertThat(dto.getItemId(), equalTo(item1.getId()));
        assertThat(dto.getStart(), equalTo(booking5OtherUser.getStartDate()));
        assertThat(dto.getEnd(), equalTo(booking5OtherUser.getEndDate()));
    }

    @Test
    @DisplayName("findNextApprovedBookingsShortForItems should ignore past/current/non-approved")
    void findNextApproved_shouldIgnorePastCurrentNonApproved() {
        List<BookingShortDto> result = bookingRepository.findNextApprovedBookingsShortForItems(List.of(item1.getId()), now);

        assertThat(result, hasSize(1));
        assertTrue(result.stream().noneMatch(dto -> dto.getId().equals(booking1Past.getId())));
        assertTrue(result.stream().noneMatch(dto -> dto.getId().equals(booking2Current.getId())));
        assertTrue(result.stream().noneMatch(dto -> dto.getId().equals(booking3Future.getId())));
        assertTrue(result.stream().noneMatch(dto -> dto.getId().equals(booking4Rejected.getId())));
    }

    @Test
    @DisplayName("findNextApprovedBookingsShortForItems handles multiple items")
    void findNextApproved_whenMultipleItems_shouldReturnCorrectDtos() {
        List<BookingShortDto> result = bookingRepository.findNextApprovedBookingsShortForItems(List.of(item1.getId(), item2.getId()), now);

        assertThat(result, hasSize(1));
        assertThat(result.getFirst().getId(), equalTo(booking5OtherUser.getId()));
    }

    @Test
    @DisplayName("findNextApprovedBookingsShortForItems ordering check")
    void findNextApproved_shouldOrderByStartDateAsc() {
        Booking anotherFutureApproved = new Booking();
        anotherFutureApproved.setBooker(booker1);
        anotherFutureApproved.setItem(item1);
        anotherFutureApproved.setStartDate(now.plusDays(10));
        anotherFutureApproved.setEndDate(now.plusDays(11));
        anotherFutureApproved.setStatus(BookingStatus.APPROVED);
        entityManager.persistAndFlush(anotherFutureApproved);

        List<BookingShortDto> result = bookingRepository.findNextApprovedBookingsShortForItems(List.of(item1.getId()), now);

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(booking5OtherUser.getId()));
        assertThat(result.get(1).getId(), equalTo(anotherFutureApproved.getId()));
    }
}