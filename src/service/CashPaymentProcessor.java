package service;

public class CashPaymentProcessor implements PaymentMethodProcessor {
    @Override
    public String process(double amount) {
        return "SUCCESS";
    }

    @Override
    public String getMethodName() {
        return "CASH";
    }
}
