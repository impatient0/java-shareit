package ru.practicum.shareit.gateway.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class HeaderValidationFilterTest {

    @InjectMocks
    private HeaderValidationFilter headerValidationFilter;

    @Mock
    private GatewayFilterChain mockChain;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private MockServerWebExchange createExchangeWithHeader(String headerValue) {
        MockServerHttpRequest.BaseBuilder<?> requestBuilder = MockServerHttpRequest.get("/test");

        if (headerValue != null) {
            requestBuilder.header(USER_ID_HEADER, headerValue);
        }

        MockServerHttpRequest request = requestBuilder.build();

        return MockServerWebExchange.from(request);
    }

    private MockServerWebExchange createExchangeWithoutHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        return MockServerWebExchange.from(request);
    }


    @Test
    void validateUserIdHeader_whenHeaderIsPresentAndValid_shouldPass() {
        MockServerWebExchange exchange = createExchangeWithHeader("1");
        GatewayFilter filter = headerValidationFilter.validateUserIdHeader();

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();
    }

    @Test
    void validateUserIdHeader_whenHeaderIsMissing_shouldThrowResponseStatusException() {
        MockServerWebExchange exchange = createExchangeWithoutHeader();
        GatewayFilter filter = headerValidationFilter.validateUserIdHeader();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> filter.filter(exchange, mockChain).block());

        assertEquals("Required header '" + USER_ID_HEADER + "' is missing", exception.getReason());
    }

    @Test
    void validateUserIdHeader_whenHeaderIsBlank_shouldThrowResponseStatusException() {
        MockServerWebExchange exchange = createExchangeWithHeader("");
        GatewayFilter filter = headerValidationFilter.validateUserIdHeader();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> filter.filter(exchange, mockChain).block());

        assertEquals("Required header '" + USER_ID_HEADER + "' is missing", exception.getReason());
    }

    @Test
    void validateUserIdHeader_whenHeaderIsNotANumber_shouldThrowResponseStatusException() {
        MockServerWebExchange exchange = createExchangeWithHeader("abc");
        GatewayFilter filter = headerValidationFilter.validateUserIdHeader();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> filter.filter(exchange, mockChain).block());

        assertEquals("Invalid format for header '" + USER_ID_HEADER + "'", exception.getReason());
    }

    @Test
    void validateUserIdHeader_whenHeaderIsNegativeNumber_shouldPass() {
        MockServerWebExchange exchange = createExchangeWithHeader("-1");
        GatewayFilter filter = headerValidationFilter.validateUserIdHeader();

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();
    }

    @Test
    void validateUserIdHeader_whenHeaderIsZero_shouldPass() {
        MockServerWebExchange exchange = createExchangeWithHeader("0");
        GatewayFilter filter = headerValidationFilter.validateUserIdHeader();

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();
    }
}