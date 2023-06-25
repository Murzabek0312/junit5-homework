package com.dmdev.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderTest {

    @ParameterizedTest
    @ValueSource(strings = {"google", "apple"})
    void shouldFindByNameOpt(String name) {
        // When:
        var actualResult = Provider.findByNameOpt(name);

        //Then:
        assertNotNull(actualResult.get());
    }

    @Test
    void shouldThrowExceptionIfInvalidName() {
        // Given:
        var name = "invalidName";
        var exception = NoSuchElementException.class;

        //When-Then:
        assertThrows(exception, () -> Provider.findByName(name));
    }
}