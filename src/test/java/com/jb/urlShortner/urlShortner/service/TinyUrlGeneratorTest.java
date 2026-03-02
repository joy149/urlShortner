package com.jb.urlShortner.urlShortner.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TinyUrlGeneratorTest {

    @Test
    void generateRandomHashProducesSevenCharacterAlphaNumericValue() {
        TinyUrlGenerator generator = new TinyUrlGenerator();
        String hash = generator.generateRandomHash();

        assertEquals(7, hash.length());
        assertTrue(hash.matches("^[a-zA-Z0-9]{7}$"));
    }
}
