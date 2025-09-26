package com.jb.urlShortner.urlShortner.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TinyUrlGenerator {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int HASH_LENGTH = 7;

    public String generateRandomHash() {
        StringBuilder sb = new StringBuilder(HASH_LENGTH);
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < HASH_LENGTH; i++) {
            int randomIndex = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(randomIndex));
        }
        return sb.toString();
    }
}
