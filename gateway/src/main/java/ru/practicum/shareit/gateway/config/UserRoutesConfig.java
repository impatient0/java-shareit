package ru.practicum.shareit.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import ru.practicum.shareit.common.dto.user.NewUserDto;   // DTO from common module
import ru.practicum.shareit.common.dto.user.UpdateUserDto; // DTO from common module
import ru.practicum.shareit.gateway.validation.DtoValidator; // Your validator helper class

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserRoutesConfig {

    // Inject the DTO validator bean
    private final DtoValidator dtoValidator;

    // Inject the target server URI from application.yaml (or define it here)
    @Value("${shareit-server.url}") // Example: Injecting from properties
    private String serverUri;
    // private final String serverUri = "http://server:9090"; // Alternative: Hardcoded

    private static final String USERS_PATH = "/users";
    private static final String USERS_ID_PATH = USERS_PATH + "/{id}";

    @Bean
    public RouteLocator userRoutes(RouteLocatorBuilder builder) {
        log.info("Configuring routes for User service at URI: {}", serverUri);

        return builder.routes()
            // Route: POST /users -> Create User
            .route("create_user", r -> r
                .path(USERS_PATH)                     // Matches path /users
                .and()
                .method(HttpMethod.POST)              // Matches POST method
                .filters(f -> f.modifyRequestBody(     // Apply filter to validate body
                    NewUserDto.class,                 // Expected input class
                    NewUserDto.class,                 // Output class (same)
                    (exchange, dto) -> {              // Validation function
                        log.debug("Validating NewUserDto for POST {}", USERS_PATH);
                        dtoValidator.validate(dto);   // Use validator bean
                        return Mono.just(dto);        // Return validated DTO if successful
                    }
                ))
                .uri(serverUri)) // Forward to the server

            // Route: PATCH /users/{id} -> Update User
            .route("update_user", r -> r
                .path(USERS_ID_PATH)                  // Matches path /users/{id}
                .and()
                .method(HttpMethod.PATCH)             // Matches PATCH method
                .filters(f -> f.modifyRequestBody(     // Apply filter to validate body
                    UpdateUserDto.class,              // Expected input class
                    UpdateUserDto.class,              // Output class (same)
                    (exchange, dto) -> {              // Validation function
                        String userId = exchange.getRequest().getURI().getPath().substring(USERS_PATH.length() + 1);
                        log.debug("Validating UpdateUserDto for PATCH /users/{}", userId);
                        dtoValidator.validate(dto);   // Use validator bean
                        return Mono.just(dto);        // Return validated DTO if successful
                    }
                ))
                .uri(serverUri)) // Forward to the server

            // Route: GET /users -> Get All Users
            .route("get_all_users", r -> r
                .path(USERS_PATH)                     // Matches path /users
                .and()
                .method(HttpMethod.GET)               // Matches GET method
                // No body validation filter needed for GET
                .uri(serverUri)) // Forward directly

            // Route: GET /users/{id} -> Get User By ID
            .route("get_user_by_id", r -> r
                .path(USERS_ID_PATH)                  // Matches path /users/{id}
                .and()
                .method(HttpMethod.GET)               // Matches GET method
                // No body validation filter needed for GET
                .uri(serverUri)) // Forward directly

            // Route: DELETE /users/{id} -> Delete User By ID
            .route("delete_user", r -> r
                .path(USERS_ID_PATH)                  // Matches path /users/{id}
                .and()
                .method(HttpMethod.DELETE)            // Matches DELETE method
                // No body validation filter needed for DELETE
                .uri(serverUri)) // Forward directly

            // No .build() here if you chain more RouteLocators in other config classes.
            // If this is the ONLY RouteLocator bean, you would add .build() here.
            // Let's assume you might add ItemRoutesConfig etc., so we omit .build()
            // and rely on Spring Boot to collect all RouteLocator beans.
            // For a single config class approach, you WOULD need .build() at the very end.
            // Let's add build() for now assuming this might be the only one initially.
            .build(); // Add .build() if this is the complete definition for now
    }
}