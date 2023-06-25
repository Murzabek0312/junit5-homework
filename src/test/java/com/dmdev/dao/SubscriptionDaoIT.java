package com.dmdev.dao;

import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionDaoIT extends IntegrationTestBase {

    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

    @Test
    void shouldFindAll() {
        // Given:
        var subscription1 = subscriptionDao.insert(getSubscription("name1"));
        var subscription2 = subscriptionDao.insert(getSubscription("name2"));
        var subscription3 = subscriptionDao.insert(getSubscription("name3"));

        // When:
        var actualResult = subscriptionDao.findAll();

        // Then:
        assertEquals(3, actualResult.size());

        var subscriptionsCount = actualResult.stream()
                .map(Subscription::getId)
                .filter(id -> Objects.equals(id, subscription1.getId())
                        || Objects.equals(id, subscription2.getId())
                        || Objects.equals(id, subscription3.getId()))
                .count();

        assertEquals(3, subscriptionsCount);
    }

    @Test
    void shouldFindById() {
        // Given:
        var name = "name";
        var subscription = subscriptionDao.insert(getSubscription(name));

        // When:
        var actualResult = subscriptionDao.findById(subscription.getId());

        // Then:
        assertTrue(actualResult.isPresent());
        assertEquals(name, actualResult.get().getName());

        // case 2: если по ИД не нашли subscription
        var actualResult2 = subscriptionDao.findById(123);

        assertFalse(actualResult2.isPresent());
    }

    @Test
    void shouldDelete() {
        // Given:
        var subscription = subscriptionDao.insert(getSubscription("name"));

        // When:
        var actualResult = subscriptionDao.delete(subscription.getId());

        // Then:
        assertTrue(actualResult);

        // case 2: попытка удалить несуществующую подписку
        var actualResult2 = subscriptionDao.delete(123);

        assertFalse(actualResult2);
    }

    @Test
    void shouldUpdate() {
        // Given:
        var subscription = subscriptionDao.insert(getSubscription("name1"));
        var expirationDate = Instant.now().plusSeconds(3600);
        var newUserId = 222;
        var changedName = "changedName";
        var appleProvider = Provider.APPLE;
        var canceledStatus = Status.CANCELED;

        subscription.setUserId(newUserId);
        subscription.setName(changedName);
        subscription.setProvider(Provider.APPLE);
        subscription.setExpirationDate(expirationDate);
        subscription.setStatus(canceledStatus);

        // When:
        var actualResult = subscriptionDao.update(subscription);

        // Then:
        assertEquals(subscription.getId(), actualResult.getId());
        assertEquals(newUserId, actualResult.getUserId());
        assertEquals(changedName, actualResult.getName());
        assertEquals(appleProvider, actualResult.getProvider());
        assertEquals(canceledStatus, actualResult.getStatus());
    }

    @Test
    void shouldInsert() {
        // Given-When:
        var subscription = subscriptionDao.insert(getSubscription("name"));

        // Then:
        assertNotNull(subscription.getId());
    }

    @Test
    void shouldFindByUserId() {
        // Given:
        var userId = 987;
        var name1 = "name1";
        var name2 = "name2";
        var subscription1 = getSubscription(name1);
        var subscription2 = getSubscription(name2);
        subscription1.setUserId(userId);
        subscription2.setUserId(userId);
        subscriptionDao.insert(subscription1);
        subscriptionDao.insert(subscription2);

        // When:
        var actualResult = subscriptionDao.findByUserId(userId);

        // Then:
        assertEquals(2, actualResult.size());

        var subscriptionsCount = actualResult.stream()
                .filter(subscription -> subscription.getName().equals(name1)
                        || subscription.getName().equals(name2))
                .count();

        assertEquals(2, subscriptionsCount);

        // case 2: если по userId нет подписки
        var actualResult2 = subscriptionDao.findByUserId(431);

        assertEquals(0, actualResult2.size());
    }

    private static Subscription getSubscription(String name) {
        return Subscription.builder()
                .userId(111)
                .name(name)
                .provider(Provider.GOOGLE)
                .expirationDate(Instant.now())
                .status(Status.ACTIVE)
                .build();
    }
}