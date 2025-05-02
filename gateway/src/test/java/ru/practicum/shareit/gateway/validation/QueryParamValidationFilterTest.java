package ru.practicum.shareit.gateway.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
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
class QueryParamValidationFilterTest {

    private enum TestEnum {
        VALUE1, VALUE2, ANOTHER_VALUE
    }

    private static final String TEST_PARAM_NAME = "status";
    @InjectMocks
    private QueryParamValidationFilter queryParamValidationFilter;
    @Mock
    private GatewayFilterChain mockChain;

    private MockServerWebExchange createExchangeWithQueryParam(String paramName,
        String... paramValues) {
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
    @DisplayName("validateOptionalEnumQueryParam should pass filter when parameter is missing")
    void validateOptionalEnumQueryParam_whenParamIsMissing_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithoutQueryParam();
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(
            TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain)).verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    @DisplayName("validateOptionalEnumQueryParam should pass filter when parameter has an empty "
        + "value list")
    void validateOptionalEnumQueryParam_whenParamHasEmptyValueList_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME);
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(
            TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain)).verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    @DisplayName("validateOptionalEnumQueryParam should pass filter when parameter has a blank "
        + "first value")
    void validateOptionalEnumQueryParam_whenParamHasBlankFirstValue_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "");
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(
            TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain)).verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    @DisplayName("validateOptionalEnumQueryParam should pass filter when parameter has a blank "
        + "first value with spaces")
    void validateOptionalEnumQueryParam_whenParamHasBlankFirstValueWithSpaces_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "   ");
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(
            TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain)).verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    @DisplayName("validateOptionalEnumQueryParam should pass filter when parameter has a valid "
        + "uppercase value")
    void validateOptionalEnumQueryParam_whenParamHasValidUppercaseValue_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "VALUE1");
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(
            TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain)).verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    @DisplayName("validateOptionalEnumQueryParam should pass filter when parameter has a valid "
        + "lowercase value")
    void validateOptionalEnumQueryParam_whenParamHasValidLowercaseValue_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "value2");
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(
            TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain)).verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    @DisplayName("validateOptionalEnumQueryParam should pass filter when parameter has a valid "
        + "mixed case value")
    void validateOptionalEnumQueryParam_whenParamHasValidMixedCaseValue_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME,
            "aNoThEr_VaLuE");
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(
            TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain)).verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    @DisplayName("validateOptionalEnumQueryParam should throw exception when parameter has an "
        + "invalid value")
    void validateOptionalEnumQueryParam_whenParamHasInvalidValue_shouldThrowException() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME,
            "INVALID_VALUE");
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(
            TEST_PARAM_NAME, TestEnum.class);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> filter.filter(exchange, mockChain).block(),
            "Should throw ResponseStatusException for invalid enum value");

        assertEquals("Unknown " + TEST_PARAM_NAME + ": INVALID_VALUE", exception.getReason(),
            "Exception reason should describe the unknown value");

        verify(mockChain, never()).filter(exchange);
    }

    @Test
    @DisplayName("validateOptionalEnumQueryParam should pass filter when parameter has multiple "
        + "values and the first is valid")
    void validateOptionalEnumQueryParam_whenParamHasMultipleValuesFirstIsValid_shouldPassFilter() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME, "VALUE1",
            "INVALID_VALUE");
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(
            TEST_PARAM_NAME, TestEnum.class);

        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, mockChain)).verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    @DisplayName("validateOptionalEnumQueryParam should throw exception when parameter has "
        + "multiple values and the first is invalid")
    void validateOptionalEnumQueryParam_whenParamHasMultipleValuesFirstIsInvalid_shouldThrowException() {
        MockServerWebExchange exchange = createExchangeWithQueryParam(TEST_PARAM_NAME,
            "INVALID_VALUE", "VALUE1");
        GatewayFilter filter = queryParamValidationFilter.validateOptionalEnumQueryParam(
            TEST_PARAM_NAME, TestEnum.class);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> filter.filter(exchange, mockChain).block(),
            "Should throw ResponseStatusException when first of multiple values is invalid");

        assertEquals("Unknown " + TEST_PARAM_NAME + ": INVALID_VALUE", exception.getReason(),
            "Exception reason should describe the unknown value");

        verify(mockChain, never()).filter(exchange);
    }
}