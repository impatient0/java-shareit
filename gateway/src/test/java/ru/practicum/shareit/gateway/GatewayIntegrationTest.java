package ru.practicum.shareit.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ru.practicum.shareit.common.dto.booking.NewBookingDto;
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;
import ru.practicum.shareit.common.dto.user.NewUserDto;
import ru.practicum.shareit.common.dto.user.UpdateUserDto;
import ru.practicum.shareit.common.exception.ErrorMessage;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayIntegrationTest {

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    private static final int MOCK_SERVER_PORT = TestSocketUtils.findAvailableTcpPort();

    private MockWebServer mockWebServer;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("shareit-server.url", () -> "http://localhost:" + MOCK_SERVER_PORT);
    }

    @BeforeEach
    void setUpPerTest() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(MOCK_SERVER_PORT);
    }

    @AfterEach
    void tearDownPerTest() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    private void enqueueMockResponse(int status, String body) {
        mockWebServer.enqueue(new MockResponse().setResponseCode(status)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(body));
    }

    private void enqueueMockResponse(int status) {
        mockWebServer.enqueue(new MockResponse().setResponseCode(status));
    }

    private <T> String toJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    private RecordedRequest takeRequestOrFail() throws InterruptedException {
        RecordedRequest request = mockWebServer.takeRequest(500, TimeUnit.MILLISECONDS);
        assertNotNull(request,
            "Expected a request to reach MockWebServer, but none arrived within the timeout.");
        return request;
    }

    @Nested
    @DisplayName("User Routes (/users)")
    class UserRoutesTests {

        @Test
        @DisplayName("POST /users - Valid User -> Forwarded (201 Created)")
        void createUser_whenValid_shouldForwardAndReturnCreated() throws Exception {
            NewUserDto newUser = new NewUserDto("Test User", "test@example.com");
            String expectedResponseBody =
                "{ \"id\": 1, \"name\": \"Test User\", \"email\": " + "\"test@example.com\" }";
            enqueueMockResponse(HttpStatus.CREATED.value(), expectedResponseBody);

            webTestClient.post().uri("/users").contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newUser)).exchange().expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail(); // Consume the expected request
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals("/users", recordedRequest.getPath());
            assertEquals(toJson(newUser), recordedRequest.getBody().readUtf8());
            assertNull(recordedRequest.getHeader(HEADER_USER_ID));
        }

        @Test
        @DisplayName("POST /users - Invalid User (blank name) -> Bad Request (400)")
        void createUser_whenInvalidDtoNameBlank_shouldReturnBadRequest() {
            NewUserDto invalidUser = new NewUserDto(" ", "test@example.com");

            webTestClient.post().uri("/users").contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(invalidUser)).exchange().expectStatus().isBadRequest()
                .expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Validation failed: Name cannot be " + "blank"));
        }

        @Test
        @DisplayName("POST /users - Invalid User (invalid email) -> Bad Request (400)")
        void createUser_whenInvalidDtoEmailInvalid_shouldReturnBadRequest() {
            NewUserDto invalidUser = new NewUserDto("Test User", "invalid-email");

            webTestClient.post().uri("/users").contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(invalidUser)).exchange().expectStatus().isBadRequest()
                .expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Validation failed: Invalid email " + "format"));
        }

        @Test
        @DisplayName("PATCH /users/{id} - Valid Update -> Forwarded (200 OK)")
        void updateUser_whenValid_shouldForwardAndReturnOk() throws Exception {
            long userId = 1L;
            UpdateUserDto updateUser = new UpdateUserDto("Updated Name", null);
            String expectedResponseBody = "{ \"id\": 1, \"name\": \"Updated Name\", \"email\": "
                + "\"original@example.com\" }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.patch().uri("/users/" + userId).contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateUser)).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail(); // Consume the expected request
            assertEquals("PATCH", recordedRequest.getMethod());
            assertEquals("/users/" + userId, recordedRequest.getPath());
            assertEquals(toJson(updateUser), recordedRequest.getBody().readUtf8());
            assertNull(recordedRequest.getHeader(HEADER_USER_ID));
        }

        @Test
        @DisplayName("PATCH /users/{id} - Invalid Update (invalid email) -> Bad Request (400)")
        void updateUser_whenInvalidDtoEmailInvalid_shouldReturnBadRequest() {
            long userId = 1L;
            UpdateUserDto invalidUpdate = new UpdateUserDto(null, "invalid-email");

            webTestClient.patch().uri("/users/" + userId).contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(invalidUpdate)).exchange().expectStatus()
                .isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Validation failed: Invalid email " + "format"));
        }

        @Test
        @DisplayName("GET /users/{id} -> Forwarded (200 OK)")
        void getUserById_shouldForwardAndReturnOk() throws Exception {
            long userId = 1L;
            String expectedResponseBody =
                "{ \"id\": 1, \"name\": \"Test User\", \"email\": " + "\"test@example.com\" }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri("/users/" + userId).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail(); // Consume the expected request
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals("/users/" + userId, recordedRequest.getPath());
            assertNull(recordedRequest.getHeader(HEADER_USER_ID));
        }

        @Test
        @DisplayName("GET /users/{id} - Backend Error -> Proxied (e.g., 404 Not Found)")
        void getUserById_whenBackendReturnsNotFound_shouldProxyNotFound() throws Exception {
            long userId = 999L;
            ErrorMessage backendError = new ErrorMessage("User not found",
                HttpStatus.NOT_FOUND.value());
            enqueueMockResponse(HttpStatus.NOT_FOUND.value(), toJson(backendError));

            webTestClient.get().uri("/users/" + userId).exchange().expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ErrorMessage.class).isEqualTo(backendError);

            RecordedRequest recordedRequest = takeRequestOrFail(); // Consume the expected request
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals("/users/" + userId, recordedRequest.getPath());
        }

        @Test
        @DisplayName("GET /users -> Forwarded (200 OK)")
        void getAllUsers_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody = "[{ \"id\": 1, ... }, { \"id\": 2, ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri("/users").exchange().expectStatus().isOk().expectHeader()
                .contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail(); // Consume the expected request
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals("/users", recordedRequest.getPath());
            assertNull(recordedRequest.getHeader(HEADER_USER_ID));
        }

        @Test
        @DisplayName("DELETE /users/{id} -> Forwarded (204 No Content)")
        void deleteUser_shouldForwardAndReturnNoContent() throws Exception {
            long userId = 1L;
            enqueueMockResponse(HttpStatus.NO_CONTENT.value());

            webTestClient.delete().uri("/users/" + userId).exchange().expectStatus().isNoContent();

            RecordedRequest recordedRequest = takeRequestOrFail(); // Consume the expected request
            assertEquals("DELETE", recordedRequest.getMethod());
            assertEquals("/users/" + userId, recordedRequest.getPath());
            assertNull(recordedRequest.getHeader(HEADER_USER_ID));
        }
    }

    @Nested
    @DisplayName("Item Routes (/items)")
    class ItemRoutesTests {

        private final String validUserIdHeader = "1";
        private final String itemsPath = "/items";
        private final long testItemId = 10L;

        @Test
        @DisplayName("POST /items - Valid Item, Valid Header -> Forwarded (201 Created)")
        void createItem_whenValid_shouldForwardAndReturnCreated() throws Exception {
            NewItemDto newItem = new NewItemDto();
            newItem.setName("Test Item");
            newItem.setDescription("Desc");
            newItem.setAvailable(true);
            String expectedResponseBody = "{ \"id\": 10, \"name\": \"Test Item\", "
                + "\"description\": \"Desc\", \"available\": true }";
            enqueueMockResponse(HttpStatus.CREATED.value(), expectedResponseBody);

            webTestClient.post().uri(itemsPath).header(HEADER_USER_ID, validUserIdHeader)
                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(newItem))
                .exchange().expectStatus().isCreated().expectHeader()
                .contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals(itemsPath, recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
            assertThat(recordedRequest.getBody().readUtf8()).contains("\"name\":\"Test Item\"");
        }

        @Test
        @DisplayName("POST /items - Missing Header -> Bad Request (400)")
        void createItem_whenMissingHeader_shouldReturnBadRequest() {
            NewItemDto newItem = new NewItemDto();
            newItem.setName("Test Item");
            newItem.setDescription("Desc");
            newItem.setAvailable(true);

            webTestClient.post().uri(itemsPath).contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newItem)).exchange().expectStatus().isBadRequest()
                .expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("POST /items - Invalid Header (non-numeric) -> Bad Request (400)")
        void createItem_whenInvalidHeader_shouldReturnBadRequest() {
            NewItemDto newItem = new NewItemDto();
            newItem.setName("Test Item");
            newItem.setDescription("Desc");
            newItem.setAvailable(true);

            webTestClient.post().uri(itemsPath).header(HEADER_USER_ID, "invalid")
                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(newItem))
                .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Invalid format for header 'X-Sharer-User-Id'"));
        }

        @Test
        @DisplayName("POST /items - Valid Header, Invalid DTO (blank name) -> Bad Request (400)")
        void createItem_whenInvalidDtoNameBlank_shouldReturnBadRequest() {
            NewItemDto invalidItem = new NewItemDto();
            invalidItem.setName(" ");
            invalidItem.setDescription("Desc");
            invalidItem.setAvailable(true);

            webTestClient.post().uri(itemsPath).header(HEADER_USER_ID, validUserIdHeader)
                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(invalidItem))
                .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Validation failed: Name cannot be blank"));
        }

        @Test
        @DisplayName(
            "POST /items - Valid Header, Invalid DTO (null available) -> Bad Request " + "(400)")
        void createItem_whenInvalidDtoAvailableNull_shouldReturnBadRequest() {
            NewItemDto invalidItem = new NewItemDto();
            invalidItem.setName("Item");
            invalidItem.setDescription("Desc");
            invalidItem.setAvailable(null);

            webTestClient.post().uri(itemsPath).header(HEADER_USER_ID, validUserIdHeader)
                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(invalidItem))
                .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Validation failed: Item status must be set"));
        }

        @Test
        @DisplayName("PATCH /items/{id} - Valid Update, Valid Header -> Forwarded (200 OK)")
        void updateItem_whenValid_shouldForwardAndReturnOk() throws Exception {
            UpdateItemDto updateItem = new UpdateItemDto("Updated Item", null, null);
            String expectedResponseBody = "{ \"id\": " + testItemId
                + ", \"name\": \"Updated Item\", \"description\": \"Original Desc\", "
                + "\"available\": true }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.patch().uri(itemsPath + "/" + testItemId)
                .header(HEADER_USER_ID, validUserIdHeader).contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateItem)).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("PATCH", recordedRequest.getMethod());
            assertEquals(itemsPath + "/" + testItemId, recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
            assertThat(recordedRequest.getBody().readUtf8()).contains("\"name\":\"Updated Item\"");
        }

        @Test
        @DisplayName("PATCH /items/{id} - Missing Header -> Bad Request (400)")
        void updateItem_whenMissingHeader_shouldReturnBadRequest() {
            UpdateItemDto updateItem = new UpdateItemDto("Updated Item", null, null);

            webTestClient.patch().uri(itemsPath + "/" + testItemId)
                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(updateItem))
                .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("GET /items/{id} - Valid Header -> Forwarded (200 OK)")
        void getItemById_whenValidHeader_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody =
                "{ \"id\": " + testItemId + ", \"name\": \"Test Item\", ... }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(itemsPath + "/" + testItemId)
                .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals(itemsPath + "/" + testItemId, recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
        }

        @Test
        @DisplayName("GET /items/{id} - Missing Header -> Bad Request (400)")
        void getItemById_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(itemsPath + "/" + testItemId).exchange().expectStatus()
                .isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("GET /items - Valid Header -> Forwarded (200 OK)")
        void getUserItems_whenValidHeader_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody = "[{ \"id\": 10, ... }, { \"id\": 12, ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(itemsPath).header(HEADER_USER_ID, validUserIdHeader).exchange()
                .expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class).isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals(itemsPath, recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
        }

        @Test
        @DisplayName("GET /items - Missing Header -> Bad Request (400)")
        void getUserItems_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(itemsPath).exchange().expectStatus().isBadRequest()
                .expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }


        @Test
        @DisplayName("GET /items/search?text=... - Valid Header and Query -> Forwarded (200 OK)")
        void searchItems_whenValid_shouldForwardAndReturnOk() throws Exception {
            String searchText = "findme";
            String expectedResponseBody = "[{ \"id\": 11, \"name\": \"Found Item\", ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(
                    uriBuilder -> uriBuilder.path(itemsPath + "/search").queryParam("text",
                            searchText)
                        .build()).header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus()
                .isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class).isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals(itemsPath + "/search?text=" + searchText, recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
        }

        @Test
        @DisplayName("GET /items/search?text=... - Missing Header -> Bad Request (400)")
        void searchItems_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(
                    uriBuilder -> uriBuilder.path(itemsPath + "/search").queryParam("text",
                            "something")
                        .build()).exchange().expectStatus().isBadRequest()
                .expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }


        @Test
        @DisplayName(
            "POST /items/{itemId}/comment - Valid Comment, Valid Header -> Forwarded " + "(200 OK)")
        void addComment_whenValid_shouldForwardAndReturnOk() throws Exception {
            long itemIdForComment = 20L;
            NewCommentDto newComment = new NewCommentDto();
            newComment.setText("Comment");
            String expectedResponseBody = "{ \"id\": 5, \"text\": \"Comment\", ... }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.post().uri(itemsPath + "/" + itemIdForComment + "/comment")
                .header(HEADER_USER_ID, validUserIdHeader).contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newComment)).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals(itemsPath + "/" + itemIdForComment + "/comment",
                recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
            assertThat(recordedRequest.getBody().readUtf8()).contains("\"text\":\"Comment\"");
        }

        @Test
        @DisplayName("POST /items/{itemId}/comment - Valid Header, Invalid DTO (blank text) -> "
            + "Bad Request (400)")
        void addComment_whenInvalidDtoTextBlank_shouldReturnBadRequest() {
            long itemIdForComment = 20L;
            NewCommentDto invalidComment = new NewCommentDto();
            invalidComment.setText(" ");

            webTestClient.post().uri(itemsPath + "/" + itemIdForComment + "/comment")
                .header(HEADER_USER_ID, validUserIdHeader).contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(invalidComment)).exchange().expectStatus()
                .isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Validation failed: Comment text cannot be blank"));
        }

        @Test
        @DisplayName("POST /items/{itemId}/comment - Missing Header -> Bad Request (400)")
        void addComment_whenMissingHeader_shouldReturnBadRequest() {
            long itemIdForComment = 20L;
            NewCommentDto newComment = new NewCommentDto();
            newComment.setText("Comment");

            webTestClient.post().uri(itemsPath + "/" + itemIdForComment + "/comment")
                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(newComment))
                .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }


        @Test
        @DisplayName("DELETE /items/{id} - Valid Header -> Forwarded (204 No Content)")
        void deleteItem_whenValidHeader_shouldForwardAndReturnNoContent() throws Exception {
            enqueueMockResponse(HttpStatus.NO_CONTENT.value());

            webTestClient.delete().uri(itemsPath + "/" + testItemId)
                .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isNoContent();

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("DELETE", recordedRequest.getMethod());
            assertEquals(itemsPath + "/" + testItemId, recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
        }


        @Test
        @DisplayName("DELETE /items/{id} - Missing Header -> Bad Request (400)")
        void deleteItem_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.delete().uri(itemsPath + "/" + testItemId).exchange().expectStatus()
                .isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }

    }

    // --- Booking Routes Tests ---
    @Nested
    @DisplayName("Booking Routes (/bookings)")
    class BookingRoutesTests {

        private final String validUserIdHeader = "2";
        private final String bookingsPath = "/bookings";
        private final long testBookingId = 50L;
        private final LocalDateTime validStart = LocalDateTime.now().plusDays(1);
        private final LocalDateTime validEnd = LocalDateTime.now().plusDays(2);

        // **** REMOVE Thread.sleep() and request count check from failure tests ****

        @Test
        @DisplayName("POST /bookings - Valid Booking, Valid Header -> Forwarded (201 Created)")
        void createBooking_whenValid_shouldForwardAndReturnCreated() throws Exception {
            NewBookingDto newBooking = new NewBookingDto(1L, validStart, validEnd);
            String expectedResponseBody =
                "{ \"id\": " + testBookingId + ", \"item\": { ... }, ... }";
            enqueueMockResponse(HttpStatus.CREATED.value(), expectedResponseBody);

            webTestClient.post().uri(bookingsPath).header(HEADER_USER_ID, validUserIdHeader)
                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(newBooking))
                .exchange().expectStatus().isCreated().expectHeader()
                .contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals(bookingsPath, recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
            assertThat(recordedRequest.getBody().readUtf8()).contains("\"itemId\":1");
        }

        @Test
        @DisplayName("POST /bookings - Valid Header, Invalid DTO (null start date) -> Bad Request"
            + " (400)")
        void createBooking_whenInvalidDtoStartNull_shouldReturnBadRequest() {
            NewBookingDto invalidBooking = new NewBookingDto(1L, null, validEnd);

            webTestClient.post().uri(bookingsPath).header(HEADER_USER_ID, validUserIdHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(invalidBooking)).exchange().expectStatus()
                .isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Validation failed: Start date cannot be null"));
        }

        @Test
        @DisplayName(
            "POST /bookings - Valid Header, Invalid DTO (null end date) -> Bad Request " + "(400)")
        void createBooking_whenInvalidDtoEndNull_shouldReturnBadRequest() {
            NewBookingDto invalidBooking = new NewBookingDto(1L, validStart, null);

            webTestClient.post().uri(bookingsPath).header(HEADER_USER_ID, validUserIdHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(invalidBooking)).exchange().expectStatus()
                .isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Validation failed: End date cannot be null"));
        }

        @Test
        @DisplayName(
            "POST /bookings - Valid Header, Invalid DTO (null item ID) -> Bad Request " + "(400)")
        void createBooking_whenInvalidDtoItemIdNull_shouldReturnBadRequest() {
            NewBookingDto invalidBooking = new NewBookingDto(null, validStart, validEnd);

            webTestClient.post().uri(bookingsPath).header(HEADER_USER_ID, validUserIdHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(invalidBooking)).exchange().expectStatus()
                .isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Validation failed: Item ID cannot be null"));
        }

        @Test
        @DisplayName("POST /bookings - Missing Header -> Bad Request (400)")
        void createBooking_whenMissingHeader_shouldReturnBadRequest() {
            NewBookingDto newBooking = new NewBookingDto(1L, validStart, validEnd);

            webTestClient.post().uri(bookingsPath).contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newBooking)).exchange().expectStatus().isBadRequest()
                .expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("PATCH /bookings/{id}?approved=true - Valid Header and Query -> Forwarded "
            + "(200 OK)")
        void approveBooking_whenValid_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody =
                "{ \"id\": " + testBookingId + ", \"status\": \"APPROVED\", ... }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.patch().uri(
                    uriBuilder -> uriBuilder.path(bookingsPath + "/" + testBookingId)
                        .queryParam("approved", "true").build())
                .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("PATCH", recordedRequest.getMethod());
            assertEquals(bookingsPath + "/" + testBookingId + "?approved=true",
                recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
            assertEquals(0, recordedRequest.getBodySize());
        }

        @Test
        @DisplayName("PATCH /bookings/{id}?approved=... - Missing 'approved' Query Param -> Not "
            + "Found (404)")
        void approveBooking_whenMissingApprovedQuery_shouldReturnNotFound() {
            webTestClient.patch().uri(bookingsPath + "/" + testBookingId)
                .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isNotFound();

        }

        @Test
        @DisplayName("PATCH /bookings/{id}?approved=invalid - Invalid 'approved' Query Value -> "
            + "Not Found (404)")
        void approveBooking_whenInvalidApprovedQueryValue_shouldReturnNotFound() {
            webTestClient.patch().uri(
                    uriBuilder -> uriBuilder.path(bookingsPath + "/" + testBookingId)
                        .queryParam("approved", "maybe").build())
                .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isNotFound();

        }

        @Test
        @DisplayName("PATCH /bookings/{id}?approved=true - Missing Header -> Bad Request (400)")
        void approveBooking_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.patch().uri(
                    uriBuilder -> uriBuilder.path(bookingsPath + "/" + testBookingId)
                        .queryParam("approved", "true").build()).exchange().expectStatus()
                .isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("GET /bookings/{id} - Valid Header -> Forwarded (200 OK)")
        void getBookingById_whenValidHeader_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody = "{ \"id\": " + testBookingId + ", ... }";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(bookingsPath + "/" + testBookingId)
                .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals(bookingsPath + "/" + testBookingId, recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
        }

        @Test
        @DisplayName("GET /bookings/{id} - Missing Header -> Bad Request (400)")
        void getBookingById_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(bookingsPath + "/" + testBookingId).exchange().expectStatus()
                .isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }


        @Test
        @DisplayName(
            "GET /bookings?state=WAITING - Valid Header, Valid 'state' -> Forwarded (200" + " OK)")
        void getBookingsByBooker_whenValidState_shouldForwardAndReturnOk() throws Exception {
            String state = "WAITING";
            String expectedResponseBody = "[{ \"id\": 51, \"status\": \"WAITING\", ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(bookingsPath).queryParam("state", state).build())
                .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals(bookingsPath + "?state=" + state, recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
        }

        @Test
        @DisplayName(
            "GET /bookings?state=INVALID - Valid Header, Invalid 'state' -> Bad Request " + "(400)")
        void getBookingsByBooker_whenInvalidState_shouldReturnBadRequest() {
            String invalidState = "INVALID_STATUS";

            webTestClient.get().uri(
                    uriBuilder -> uriBuilder.path(bookingsPath).queryParam("state", invalidState)
                        .build()).header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus()
                .isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Unknown state: " + invalidState));
        }

        @Test
        @DisplayName("GET /bookings - Valid Header, Missing 'state' -> Forwarded (200 OK)")
        void getBookingsByBooker_whenMissingState_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody = "[{ \"id\": 51, ... }, { \"id\": 52, ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(bookingsPath).header(HEADER_USER_ID, validUserIdHeader)
                .exchange().expectStatus().isOk().expectHeader()
                .contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals(bookingsPath, recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
        }

        @Test
        @DisplayName("GET /bookings - Missing Header -> Bad Request (400)")
        void getBookingsByBooker_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(bookingsPath).queryParam("state", "ALL").build())
                .exchange().expectStatus().isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }

        @Test
        @DisplayName("GET /bookings/owner?state=ALL - Valid Header -> Forwarded (200 OK)")
        void getBookingsByOwner_whenValid_shouldForwardAndReturnOk() throws Exception {
            String state = "ALL";
            String expectedResponseBody = "[{ \"id\": 55, ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(
                    uriBuilder -> uriBuilder.path(bookingsPath + "/owner").queryParam("state",
                            state)
                        .build()).header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus()
                .isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class).isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals(bookingsPath + "/owner?state=" + state, recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
        }

        @Test
        @DisplayName("GET /bookings/owner - Valid Header, Missing 'state' -> Forwarded (200 OK)")
        void getBookingsByOwner_whenMissingState_shouldForwardAndReturnOk() throws Exception {
            String expectedResponseBody = "[{ \"id\": 55, ... }, { \"id\": 56, ... }]";
            enqueueMockResponse(HttpStatus.OK.value(), expectedResponseBody);

            webTestClient.get().uri(bookingsPath + "/owner")
                .header(HEADER_USER_ID, validUserIdHeader).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(String.class)
                .isEqualTo(expectedResponseBody);

            RecordedRequest recordedRequest = takeRequestOrFail();
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals(bookingsPath + "/owner", recordedRequest.getPath());
            assertEquals(validUserIdHeader, recordedRequest.getHeader(HEADER_USER_ID));
        }


        @Test
        @DisplayName("GET /bookings/owner - Missing Header -> Bad Request (400)")
        void getBookingsByOwner_whenMissingHeader_shouldReturnBadRequest() {
            webTestClient.get().uri(bookingsPath + "/owner").exchange().expectStatus()
                .isBadRequest().expectBody(ErrorMessage.class).value(
                    error -> assertThat(error.getMessage()).isEqualTo(
                        "Required header 'X-Sharer-User-Id' is missing"));
        }
    }
}