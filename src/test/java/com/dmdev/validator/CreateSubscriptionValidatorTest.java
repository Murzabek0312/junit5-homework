package com.dmdev.validator;

import com.dmdev.dto.CreateSubscriptionDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateSubscriptionValidatorTest {

    private final CreateSubscriptionValidator validator = CreateSubscriptionValidator.getInstance();

    @Test
    void shouldPassValidate() {
        // Given:
        var userId = 123;
        var name = "name";
        var provider = "GOOGLE";
        var expirationDate = Instant.now().plusSeconds(3600);

        var createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(userId)
                .name(name)
                .provider(provider)
                .expirationDate(expirationDate)
                .build();

        // When:
        var actualResult = validator.validate(createSubscriptionDto);

        // Then:
        assertFalse(actualResult.hasErrors());
    }

    @Test
    void shouldHasErrorIfInvalidId() {
        // Given:
        var name = "name";
        var provider = "GOOGLE";
        var expirationDate = Instant.now().plusSeconds(3600);
        var errorCode = 100;
        var errorMessage = "userId is invalid";

        var createSubscriptionDto = CreateSubscriptionDto.builder()
                .name(name)
                .provider(provider)
                .expirationDate(expirationDate)
                .build();

        // When:
        var actualResult = validator.validate(createSubscriptionDto);

        // Then:
        assertEquals(1, actualResult.getErrors().size());

        var error = actualResult.getErrors().get(0);
        assertEquals(errorCode, error.getCode());
        assertEquals(errorMessage, error.getMessage());
    }

    @Test
    void shouldHasErrorIfInvalidName() {
        // Given:
        var userId = 123;
        var provider = "GOOGLE";
        var expirationDate = Instant.now().plusSeconds(3600);
        var errorCode = 101;
        var errorMessage = "name is invalid";

        var createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(userId)
                .provider(provider)
                .expirationDate(expirationDate)
                .build();

        // When:
        var actualResult = validator.validate(createSubscriptionDto);

        // Then:
        assertEquals(1, actualResult.getErrors().size());

        var error = actualResult.getErrors().get(0);
        assertEquals(errorCode, error.getCode());
        assertEquals(errorMessage, error.getMessage());
    }

    @Test
    void shouldHasErrorIfInvalidProvider() {
        // Given:
        var userId = 123;
        var name = "name";
        var expirationDate = Instant.now().plusSeconds(3600);
        var errorCode = 102;
        var errorMessage = "provider is invalid";

        var createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(userId)
                .name(name)
                .expirationDate(expirationDate)
                .build();

        // When:
        var actualResult = validator.validate(createSubscriptionDto);

        // Then:
        assertEquals(1, actualResult.getErrors().size());

        var error = actualResult.getErrors().get(0);
        assertEquals(errorCode, error.getCode());
        assertEquals(errorMessage, error.getMessage());
    }

    @Test
    void shouldHasErrorIfInvalidExpirationDate() {
        // Given:
        var userId = 123;
        var name = "name";
        var provider = "GOOGLE";
        var errorCode = 103;
        var errorMessage = "expirationDate is invalid";
        var createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(userId)
                .name(name)
                .provider(provider)
                .build();

        // When:
        var actualResult = validator.validate(createSubscriptionDto);

        // Then:
        assertEquals(1, actualResult.getErrors().size());

        var error = actualResult.getErrors().get(0);
        assertEquals(errorCode, error.getCode());
        assertEquals(errorMessage, error.getMessage());
    }

    @Test
    void shouldHasErrorsIfSomeInvalidParams() {
        // Given:
        var userInvalidMsg = "userId is invalid";
        var nameInvalidMsg = "name is invalid";
        var provideInvalidMsg = "provider is invalid";
        var expirationDate = Instant.now().plusSeconds(3600);

        var createSubscriptionDto = CreateSubscriptionDto.builder()
                .expirationDate(expirationDate)
                .build();

        // When:
        var actualResult = validator.validate(createSubscriptionDto);

        // Then:
        assertEquals(3, actualResult.getErrors().size());

        var errors = actualResult.getErrors();
        var errorsCount = errors.stream()
                .filter(error -> error.getMessage().equals(userInvalidMsg)
                        || error.getMessage().equals(nameInvalidMsg)
                        || error.getMessage().equals(provideInvalidMsg))
                .count();
        assertEquals(3, errorsCount);
    }
}