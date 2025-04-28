package ru.practicum.shareit.gateway.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

enum TestEnum {
    VALUE1,
    VALUE2,
    ANOTHER_VALUE
}

@ExtendWith(MockitoExtension.class)
class QueryParamValidationFilterTest {

    @InjectMocks
    private QueryParamValidationFilter queryParamValidationFilter;

    @Mock
    private GatewayFilterChain mockChain;

    private static final String TEST_PARAM_NAME = "status";

    private MockServerWebExchange createExchangeWithQueryParam(String paramName, String... paramValues) {
        MockServerHttpRequest.BaseBuilder<?> requestBuilder = MockServerHttpRequest.get("/test");

        if (paramValues != null && paramValues.length > 0) {
            requestBuilder.queryParam(paramName, (Object[]) paramValues);
        } else if (paramName != null) {
            requestBuilder.queryParam(paramName);
        }

        MockServerHttpRequest request = requestBuilder.build();
        return MockServerWebExchange.from(request);
    }

    private MockServerWebExchange createExchangeWithoutQueryParam() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        return MockServerWebExchange.from(request);
    }


    @Test
    void validateOptionalEnumQueryParam_whenParamIsMissing_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithoutQueryParam();
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    void validateOptionalEnumQueryParam_whenParamHasEmptyValueList_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME);
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    void validateOptionalEnumQueryParam_whenParamHasBlankFirstValue_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, ""); // Blank value
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    void validateOptionalEnumQueryParam_whenParamHasBlankFirstValueWithSpaces_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "   "); // Blank value with spaces
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        verify(mockChain).filter(exchange);
    }


    @Test
    void validateOptionalEnumQueryParam_whenParamHasValidUppercaseValue_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "VALUE1"); // Valid uppercase
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    void validateOptionalEnumQueryParam_whenParamHasValidLowercaseValue_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "value2"); // Valid lowercase
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    void validateOptionalEnumQueryParam_whenParamHasValidMixedCaseValue_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "aNoThEr_VaLuE"); // Valid mixed case
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    void validateOptionalEnumQueryParam_whenParamHasInvalidValue_shouldThrowException() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "INVALID_VALUE"); // Invalid value
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(TEST_PARAM_NAME, TestEnum.class);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> filter.filter(exchange, mockChain).block());

        assertEquals("Unknown " + TEST_PARAM_NAME + ": INVALID_VALUE", exception.getReason());

        verify(mockChain, never()).filter(exchange);
    }

    @Test
    void validateOptionalEnumQueryParam_whenParamHasMultipleValuesFirstIsValid_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "VALUE1", "INVALID_VALUE");
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    void validateOptionalEnumQueryParam_whenParamHasMultipleValuesFirstIsInvalid_shouldThrowException() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "INVALID_VALUE", "VALUE1");
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(TEST_PARAM_NAME, TestEnum.class);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> filter.filter(exchange, mockChain).block());

        assertEquals("Unknown " + TEST_PARAM_NAME + ": INVALID_VALUE", exception.getReason());

        verify(mockChain, never()).filter(exchange);
    }
}