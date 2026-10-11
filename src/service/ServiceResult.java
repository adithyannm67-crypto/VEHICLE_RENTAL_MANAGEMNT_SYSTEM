package service;

/**
 * Simple generic wrapper so service methods can return both a success/failure
 * flag, a human-readable message, and (optionally) the resulting object,
 * instead of throwing exceptions for expected business-rule failures
 * (e.g. "email already registered").
 */
public class ServiceResult<T> {
    private final boolean success;
    private final String message;
    private final T data;

    private ServiceResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ServiceResult<T> ok(String message, T data) {
        return new ServiceResult<>(true, message, data);
    }

    public static <T> ServiceResult<T> fail(String message) {
        return new ServiceResult<>(false, message, null);
    }

    /**
     * For SQL failures: the technical details go to the console (for developers),
     * while the user only ever sees the short friendly message.
     */
    public static <T> ServiceResult<T> dbError(java.sql.SQLException e, String friendlyMessage) {
        System.err.println("[VRS] " + friendlyMessage + " -> " + e.getMessage());
        e.printStackTrace();
        return fail(friendlyMessage);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
