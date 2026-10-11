package service;

public class CardPaymentProcessor implements PaymentMethodProcessor {
    @Override
    public String process(double amount) {
        return "SUCCESS";
    }

    @Override
    public String getMethodName() {
        return "CARD";
    }
}
