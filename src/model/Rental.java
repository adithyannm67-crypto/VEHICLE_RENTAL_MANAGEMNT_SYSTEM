package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Represents one row in the "rentals" table (a booking). */
public class Rental {
    private int rentalId;
    private int userId;
    private int vehicleId;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private int totalDays;
    private double totalAmount;
    private String rentalStatus; // CONFIRMED, COMPLETED, CANCELLED
    private LocalDateTime createdAt;

    // extra display-only fields filled in by joined queries (not DB columns)
    private String vehicleName;
    private String customerName;

    public Rental() { }

    public Rental(int rentalId, int userId, int vehicleId, LocalDate pickupDate, LocalDate returnDate,
                  int totalDays, double totalAmount, String rentalStatus, LocalDateTime createdAt) {
        this.rentalId = rentalId;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        this.totalDays = totalDays;
        this.totalAmount = totalAmount;
        this.rentalStatus = rentalStatus;
        this.createdAt = createdAt;
    }

    public String getBookingId() {
        return "BK" + (10000 + rentalId);
    }

    public int getRentalId() { return rentalId; }
    public void setRentalId(int rentalId) { this.rentalId = rentalId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getRentalStatus() { return rentalStatus; }
    public void setRentalStatus(String rentalStatus) { this.rentalStatus = rentalStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
