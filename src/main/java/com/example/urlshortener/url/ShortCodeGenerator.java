package com.example.urlshortener.url;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

// I made this to generate my random short codes
// I use a-z, A-Z, 0-9 so 62 possible chars per spot
@Component
public class ShortCodeGenerator {

    // these are the chars I allow in my codes
    private static final String CHARACTERS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // I use SecureRandom so my codes are hard to guess
    private final SecureRandom random = new SecureRandom();

    // I call this to get a random code of the length I want, like 6 gives "aB3xYz"
    public String generate(int length) {

        // I check this so I never make an empty code that would break my db
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive, got: " + length);
        }

        StringBuilder result = new StringBuilder(length);

        // I pick one random char at a time until I reach the length
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }

        return result.toString();
    }
}
