package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionDao subscriptionDao;

    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;

    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;

    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<Subscription> argumentCaptor;

    @InjectMocks
    private SubscriptionService service;

    @Test
    void shouldUpsert() {
        // Given:
        var userId = 12;
        var name = "name";
        var providerName = "GOOGLE";
        var provider = Provider.GOOGLE;
        var active = Status.ACTIVE;
        var expirationDate = Instant.now();
        var validationResult = mock(ValidationResult.class);

        var subscriptionDto = CreateSubscriptionDto.builder()
                .userId(userId)
                .name(name)
                .provider(providerName)
                .expirationDate(expirationDate)
                .build();

        var subscription =
                Subscription.builder()
                        .userId(userId)
                        .name(name)
                        .provider(provider)
                        .status(Status.CANCELED)
                        .expirationDate(expirationDate.minusSeconds(3600))
                        .build();

        doReturn(validationResult).when(createSubscriptionValidator).validate(subscriptionDto);
        doReturn(List.of(subscription)).when(subscriptionDao).findByUserId(userId);
        doReturn(subscription).when(subscriptionDao).upsert(subscription);

        // When:
        var actualResult = service.upsert(subscriptionDto);

        // Then:
        assertEquals(expirationDate, actualResult.getExpirationDate());
        assertEquals(active, actualResult.getStatus());
    }

    @Test
    void shouldCreateSubscriptionIfNotFoundById() {
        // Given:
        var userId = 12;
        var name = "name";
        var invalidName = "invalidName";
        var providerName = "GOOGLE";
        var provider = Provider.GOOGLE;
        var expirationDate = Instant.now();
        var validationResult = mock(ValidationResult.class);
        var subscriptionDto = CreateSubscriptionDto.builder()
                .userId(userId)
                .name(invalidName)
                .provider(providerName)
                .expirationDate(expirationDate)
                .build();

        var subscription =
                Subscription.builder()
                        .userId(userId)
                        .name(name)
                        .provider(provider)
                        .status(Status.ACTIVE)
                        .expirationDate(expirationDate.minusSeconds(3600))
                        .build();

        doReturn(validationResult).when(createSubscriptionValidator).validate(subscriptionDto);
        doReturn(List.of(subscription)).when(subscriptionDao).findByUserId(userId);

        // When:
        service.upsert(subscriptionDto);

        // Then:
        verify(createSubscriptionMapper).map(subscriptionDto);

    }

    @Test
    void shouldValidationExceptionIfInvalidDto() {
        // Given:
        var userId = 12;
        var name = "name";
        var providerName = "GOOGLE";
        var expirationDate = Instant.now();

        var subscriptionDto = CreateSubscriptionDto.builder()
                .userId(userId)
                .name(name)
                .provider(providerName)
                .expirationDate(expirationDate)
                .build();
        var error = Error.of(123, "errorMessage");
        var validationResult = new ValidationResult();
        validationResult.add(error);

        doReturn(validationResult).when(createSubscriptionValidator).validate(subscriptionDto);

        // When-Then:
        assertThrows(ValidationException.class, () -> service.upsert(subscriptionDto));
    }


    @Test
    void shouldUpdateToCancelStatus() {
        // Given:
        var subscriptionId = 123;
        var canceledStatus = Status.CANCELED;
        var subscription =
                Subscription.builder()
                        .status(Status.ACTIVE)
                        .build();

        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscriptionId);

        // When:
        service.cancel(subscriptionId);

        // Then:
        verify(subscriptionDao).update(argumentCaptor.capture());
        assertEquals(canceledStatus, argumentCaptor.getValue().getStatus());
    }

    @Test
    void shouldThrowIllegalArgumentExIfSubNotFound() {
        // Given:
        var subscriptionId = 123;
        var exception = IllegalArgumentException.class;

        doReturn(Optional.ofNullable(null)).when(subscriptionDao).findById(subscriptionId);

        // When-Then:
        assertThrows(exception, () -> service.cancel(subscriptionId));
    }

    @Test
    void shouldThrowExceptionIfInvalidStatus() {
        // Given:
        var subscriptionId = 123;
        var subscription =
                Subscription.builder()
                        .status(Status.EXPIRED)
                        .build();

        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscriptionId);
        var subscriptionException = SubscriptionException.class;

        // When-Then:
        assertThrows(subscriptionException, () -> service.cancel(subscriptionId));
    }

    @Test
    void shouldUpdateToExpireStatus() {
        // Given:
        var subscriptionId = 123;
        var now = Instant.now();
        var subscription =
                Subscription.builder()
                        .status(Status.ACTIVE)
                        .build();

        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscriptionId);
        doReturn(now).when(clock).instant();

        // When:
        service.expire(subscriptionId);

        // Then:
        verify(subscriptionDao).update(argumentCaptor.capture());
        var captorValue = argumentCaptor.getValue();

        assertEquals(Status.EXPIRED, captorValue.getStatus());
        assertEquals(now, captorValue.getExpirationDate());

        // case 2: бросаем IllegalArgumentException если по ИД не нашли subscription
        // Given:
        doReturn(Optional.ofNullable(null)).when(subscriptionDao).findById(subscriptionId);
        var exception = IllegalArgumentException.class;

        // When-Then:
        assertThrows(exception, () -> service.expire(subscriptionId));
    }

    @Test
    void shouldThrowSubscriptionExIfInvalidStatus() {
        // Given:
        var subscriptionId = 123;
        var exception = SubscriptionException.class;

        var subscription =
                Subscription.builder()
                        .status(Status.EXPIRED)
                        .build();

        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscriptionId);

        // When-Then:
        assertThrows(exception, () -> service.expire(subscriptionId));
    }
}