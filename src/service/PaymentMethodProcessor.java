package service;

/**
 * Interface for processing a payment. Different payment types (UPI, Card, Cash)
 * implement this interface, demonstrating abstraction + interfaces + polymorphism:
 * the RentalService only ever talks to "PaymentMethodProcessor", never to the
 * concrete class, but the actual behaviour used at runtime depends on which
 * object was passed in.
 */
public interface PaymentMethodProcessor {
    /** Simulates processing "amount" and returns "SUCCESS" or "FAILED". */
    String process(double amount);

    /** Name used when saving payment_method in the database, e.g. "UPI". */
    String getMethodName();
}
