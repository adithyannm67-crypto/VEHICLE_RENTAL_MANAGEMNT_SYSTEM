package service;

import dao.MaintenanceDAO;
import dao.VehicleDAO;
import model.Maintenance;
import model.Vehicle;
import util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MaintenanceService {

    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public ServiceResult<Void> addRecord(int vehicleId, String description, LocalDate date, double cost) {
        if (!ValidationUtil.isNotEmpty(description)) return ServiceResult.fail("Description is required.");
        if (date == null) return ServiceResult.fail("A valid maintenance date is required (yyyy-MM-dd).");
        if (cost < 0) return ServiceResult.fail("Cost cannot be negative.");
        try {
            Vehicle vehicle = vehicleDAO.getVehicleById(vehicleId);
            if (vehicle == null) return ServiceResult.fail("Vehicle not found.");
            if ("RENTED".equalsIgnoreCase(vehicle.getStatus()))
                return ServiceResult.fail("This vehicle is currently rented. Add maintenance after it has been returned.");
            if ("INACTIVE".equalsIgnoreCase(vehicle.getStatus()))
                return ServiceResult.fail("This vehicle is inactive. Activate it before adding maintenance.");

            Maintenance m = new Maintenance();
            m.setVehicleId(vehicleId);
            m.setDescription(description.trim());
            m.setMaintenanceDate(date);
            m.setCost(cost);
            m.setStatus("PENDING");
            maintenanceDAO.addMaintenance(m);

            // vehicle cannot be rented while it is under maintenance
            vehicleDAO.updateVehicleStatus(vehicleId, "MAINTENANCE");
            return ServiceResult.ok("Maintenance record added. Vehicle marked as MAINTENANCE.", null);
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to save the maintenance record.");
        }
    }

    public ServiceResult<Void> markCompleted(int maintenanceId, int vehicleId) {
        try {
            // Guard: completing an already-completed record must not touch the vehicle's status
            for (Maintenance m : maintenanceDAO.getMaintenanceByVehicle(vehicleId)) {
                if (m.getMaintenanceId() == maintenanceId && "COMPLETED".equalsIgnoreCase(m.getStatus())) {
                    return ServiceResult.fail("This maintenance record is already completed.");
                }
            }
            maintenanceDAO.updateMaintenanceStatus(maintenanceId, "COMPLETED");
            // only release the vehicle if it is still in MAINTENANCE (not rented/inactive since)
            Vehicle v = vehicleDAO.getVehicleById(vehicleId);
            if (v != null && "MAINTENANCE".equalsIgnoreCase(v.getStatus())) {
                vehicleDAO.updateVehicleStatus(vehicleId, "AVAILABLE");
            }
            return ServiceResult.ok("Maintenance completed. Vehicle is available again.", null);
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to update the maintenance record.");
        }
    }

    public List<Maintenance> getAll() throws SQLException {
        return maintenanceDAO.getAllMaintenance();
    }
}
