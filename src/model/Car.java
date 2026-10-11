package model;

/**
 * Car adds a flat "insurance" style extra charge per rental.
 * This is a concrete example of Inheritance + Method Overriding.
 */
public class Car extends Vehicle {

    public Car() { super(); this.vehicleType = "CAR"; }

    public Car(int vehicleId, String vehicleName, String registrationNumber,
               String fuelType, int seats, double pricePerDay, String status) {
        super(vehicleId, vehicleName, "CAR", registrationNumber, fuelType, seats, pricePerDay, status);
    }

    @Override
    public double calculateExtraCharges(int days) {
        // flat insurance/service fee of Rs.100 per day for cars
        return 100.0 * days;
    }

    @Override
    public String getCategoryLabel() {
        return "Car";
    }
}
