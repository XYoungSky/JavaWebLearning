package com.example.demo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {
    @Test
    void hashesAndVerifiesPassword() {
        String hash = PasswordHasher.hash("admin123");
        assertTrue(PasswordHasher.verify("admin123", hash));
        assertFalse(PasswordHasher.verify("wrong", hash));
        assertFalse(hash.contains("admin123"));
    }

    @Test
    void rejectsMalformedHash() {
        assertFalse(PasswordHasher.verify("secret", "not-a-valid-hash"));
        assertFalse(PasswordHasher.verify(null, null));
    }

    @Test
    void verifiesSchemaSeedPassword() {
        assertTrue(PasswordHasher.verify("admin123",
                "pbkdf2$120000$amF2YXdlYi1kZW1vLTAwMQ==$xeyQFBELHIxgug8O3ZURQnOCRqbpsyyvTHaqMWeQr6M="));
    }
}
