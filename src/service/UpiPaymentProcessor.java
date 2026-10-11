package service;

public class UpiPaymentProcessor implements PaymentMethodProcessor {
    @Override
    public String process(double amount) {
        // Simulated/mock payment: for a college project we simply mark it successful.
        return "SUCCESS";
    }

    @Override
    public String getMethodName() {
        return "UPI";
    }
}
