package com.dmdev.mapper;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Subscription;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateSubscriptionMapperTest {

    private final CreateSubscriptionMapper mapper = CreateSubscriptionMapper.getInstance();

    @Test
    void shouldReturnSubscription() {
        // Given:
        var userId = 12;
        var name = "name";
        var provider = "GOOGLE";
        var expirationDate = Instant.now();
        var subscriptionDto = CreateSubscriptionDto.builder()
                .userId(userId)
                .name(name)
                .provider(provider)
                .expirationDate(expirationDate)
                .build();

        // When:
        Subscription actualResult = mapper.map(subscriptionDto);

        // Then:
        assertEquals(userId, actualResult.getUserId());
        assertEquals(name, actualResult.getName());
        assertEquals(provider, actualResult.getProvider().name());
        assertEquals(expirationDate, actualResult.getExpirationDate());
    }
}