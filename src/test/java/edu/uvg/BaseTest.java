package edu.uvg;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;

import java.security.Security;

/**
 * Clase base que registra BouncyCastle antes de cualquier test.
 * Todas las clases de test deben extender esta clase.
 */
public abstract class BaseTest {

    @BeforeAll
    static void registerBouncyCastle() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}
