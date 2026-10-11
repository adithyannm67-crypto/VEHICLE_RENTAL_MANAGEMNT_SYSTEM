package model;

import java.time.LocalDateTime;

/** Represents one row in the "payments" table. */
public class Payment {
    private int paymentId;
    private int rentalId;
    private LocalDateTime paymentDate;
    private double amount;
    private String paymentMethod; // UPI, CARD, CASH
    private String paymentStatus; // SUCCESS, FAILED

    public Payment() { }

    public Payment(int paymentId, int rentalId, LocalDateTime paymentDate,
                   double amount, String paymentMethod, String paymentStatus) {
        this.paymentId = paymentId;
        this.rentalId = rentalId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getRentalId() { return rentalId; }
    public void setRentalId(int rentalId) { this.rentalId = rentalId; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
