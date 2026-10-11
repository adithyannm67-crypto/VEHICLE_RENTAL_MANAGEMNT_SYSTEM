package model;

/**
 * Abstract base class for every vehicle in the fleet.
 * Car and Bike extend this class (Java Inheritance).
 * calculateExtraCharges() is abstract so each subtype can define its
 * own extra-fee rule, demonstrated later via Polymorphism.
 */
public abstract class Vehicle {
    protected int vehicleId;
    protected String vehicleName;
    protected String vehicleType;        // "CAR" or "BIKE"
    protected String registrationNumber;
    protected String fuelType;
    protected int seats;
    protected double pricePerDay;
    protected String status;             // AVAILABLE, RENTED, MAINTENANCE, INACTIVE

    public Vehicle() { }

    public Vehicle(int vehicleId, String vehicleName, String vehicleType, String registrationNumber,
                   String fuelType, int seats, double pricePerDay, String status) {
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.vehicleType = vehicleType;
        this.registrationNumber = registrationNumber;
        this.fuelType = fuelType;
        this.seats = seats;
        this.pricePerDay = pricePerDay;
        this.status = status;
    }

    /** Every subclass must say how much extra it charges for "days" days. */
    public abstract double calculateExtraCharges(int days);

    /** Short label used in the UI, e.g. "Car" or "Bike". */
    public abstract String getCategoryLabel();

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }

    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return vehicleName + " (" + registrationNumber + ") - Rs." + pricePerDay + "/day";
    }
}
