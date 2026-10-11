package model;

/**
 * Bike adds a one-time helmet/safety-kit charge instead of a per-day fee.
 * Same parent type (Vehicle) but different behaviour -> Polymorphism.
 */
public class Bike extends Vehicle {

    public Bike() { super(); this.vehicleType = "BIKE"; }

    public Bike(int vehicleId, String vehicleName, String registrationNumber,
                String fuelType, int seats, double pricePerDay, String status) {
        super(vehicleId, vehicleName, "BIKE", registrationNumber, fuelType, seats, pricePerDay, status);
    }

    @Override
    public double calculateExtraCharges(int days) {
        // one-time flat helmet/safety-kit charge, regardless of number of days
        return 50.0;
    }

    @Override
    public String getCategoryLabel() {
        return "Bike";
    }
}
