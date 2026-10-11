package util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/** Reusable validation helpers used by the service layer and the UI forms. */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10}$");

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return isNotEmpty(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return isNotEmpty(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /** At least 6 characters, containing a letter and a digit. */
    public static boolean isValidPassword(String password) {
        if (!isNotEmpty(password) || password.length() < 6) return false;
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }

    public static boolean isPositiveNumber(double value) {
        return value > 0;
    }

    public static boolean isValidSeats(int seats) {
        return seats > 0 && seats <= 10;
    }

    public static boolean isValidDateRange(LocalDate pickup, LocalDate returnDate) {
        if (pickup == null || returnDate == null) return false;
        if (pickup.isBefore(LocalDate.now())) return false;      // pickup cannot be in the past
        return returnDate.isAfter(pickup);                        // return must be after pickup
    }

    /** Parses "yyyy-MM-dd" text from a form field; returns null if invalid. */
    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
