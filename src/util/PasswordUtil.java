package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Basic password hashing using SHA-256.
 *
 * NOTE for students: this is a simple, beginner-friendly approach so that
 * plain-text passwords are never stored in the database. It is good enough
 * for a college project, but a production system would use a slower,
 * salted algorithm such as bcrypt or Argon2.
 */
public class PasswordUtil {

    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to hash password", e);
        }
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return hash(rawPassword).equals(hashedPassword);
    }
}
