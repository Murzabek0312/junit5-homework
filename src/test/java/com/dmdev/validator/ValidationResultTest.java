package com.dmdev.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationResultTest {

    private final ValidationResult validationResult = new ValidationResult();

    @Test
    void shouldAdd() {
        // Given:
        var error1 = Error.of(111, "errorMessage1");
        var error2 = Error.of(222, "errorMessage2");
        var error3 = Error.of(333, "errorMessage3");

        // When:
        validationResult.add(error1);
        validationResult.add(error2);
        validationResult.add(error3);

        // Then:
        assertEquals(3, validationResult.getErrors().size());

        var errors = validationResult.getErrors();
        var errorsCount = errors.stream()
                .filter(x -> x.getCode() == 111
                        || x.getCode() == 222
                        || x.getCode() == 333)
                .count();

        assertEquals(3, errorsCount);
    }

    @Test
    void hasErrors() {
        // Given:
        var error1 = Error.of(111, "errorMessage1");
        var error2 = Error.of(222, "errorMessage2");

        // When:
        validationResult.add(error1);
        validationResult.add(error2);

        // Then:
        assertTrue(validationResult.hasErrors());
    }
}