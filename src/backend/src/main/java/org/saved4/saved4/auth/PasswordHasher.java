package org.saved4.saved4.auth;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordHasher {
    private static final Argon2 argon2 = Argon2Factory.create();

    public static String hashPassword(String password) {
        // parameters: iterations, memory in KB, parallelism
        return argon2.hash(3, 65536, 1, password.toCharArray());
    }

    public static boolean verifyPassword(String hash, String inputPassword) {
        return argon2.verify(hash, inputPassword.toCharArray());
    }
}
