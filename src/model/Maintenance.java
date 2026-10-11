package model;

import java.time.LocalDate;

/** Represents one row in the "maintenance" table. */
public class Maintenance {
    private int maintenanceId;
    private int vehicleId;
    private String description;
    private LocalDate maintenanceDate;
    private double cost;
    private String status; // PENDING, IN_PROGRESS, COMPLETED

    // display-only, filled by joined queries
    private String vehicleName;

    public Maintenance() { }

    public Maintenance(int maintenanceId, int vehicleId, String description,
                       LocalDate maintenanceDate, double cost, String status) {
        this.maintenanceId = maintenanceId;
        this.vehicleId = vehicleId;
        this.description = description;
        this.maintenanceDate = maintenanceDate;
        this.cost = cost;
        this.status = status;
    }

    public int getMaintenanceId() { return maintenanceId; }
    public void setMaintenanceId(int maintenanceId) { this.maintenanceId = maintenanceId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getMaintenanceDate() { return maintenanceDate; }
    public void setMaintenanceDate(LocalDate maintenanceDate) { this.maintenanceDate = maintenanceDate; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }
}
