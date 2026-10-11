package service;

import dao.VehicleDAO;
import model.Bike;
import model.Car;
import model.Vehicle;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/** Business logic + validation sitting on top of VehicleDAO. */
public class VehicleService {

    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public ServiceResult<Vehicle> addVehicle(String name, String type, String regNo, String fuel,
                                              int seats, double pricePerDay) {
        ServiceResult<Vehicle> validation = validateVehicleInput(name, type, regNo, fuel, seats, pricePerDay);
        if (validation != null) return validation;

        try {
            if (vehicleDAO.registrationNumberExists(regNo)) {
                return ServiceResult.fail("A vehicle with this registration number already exists.");
            }
            Vehicle v = buildVehicle(0, name, type, regNo, fuel, seats, pricePerDay, "AVAILABLE");
            boolean ok = vehicleDAO.addVehicle(v);
            return ok ? ServiceResult.ok("Vehicle added successfully.", v)
                      : ServiceResult.fail("Could not add vehicle.");
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to save vehicle.");
        }
    }

    public ServiceResult<Vehicle> updateVehicle(int vehicleId, String name, String type, String regNo,
                                                 String fuel, int seats, double pricePerDay, String status) {
        ServiceResult<Vehicle> validation = validateVehicleInput(name, type, regNo, fuel, seats, pricePerDay);
        if (validation != null) return validation;

        try {
            Vehicle existing = vehicleDAO.getVehicleById(vehicleId);
            if (existing == null) return ServiceResult.fail("Vehicle not found.");

            if (!existing.getRegistrationNumber().equalsIgnoreCase(regNo)
                    && vehicleDAO.registrationNumberExists(regNo)) {
                return ServiceResult.fail("Another vehicle already uses this registration number.");
            }
            Vehicle v = buildVehicle(vehicleId, name, type, regNo, fuel, seats, pricePerDay, status);
            boolean ok = vehicleDAO.updateVehicle(v);
            return ok ? ServiceResult.ok("Vehicle updated successfully.", v)
                      : ServiceResult.fail("Could not update vehicle.");
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to save vehicle.");
        }
    }

    public ServiceResult<Void> deleteVehicle(int vehicleId) {
        try {
            boolean ok = vehicleDAO.deleteVehicle(vehicleId);
            return ok ? ServiceResult.ok("Vehicle deleted.", null)
                      : ServiceResult.fail("Could not delete vehicle. It may have existing rental records.");
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "This vehicle has rental history and cannot be deleted. Deactivate it instead.");
        }
    }

    /**
     * Soft-removes a vehicle: status becomes INACTIVE so it cannot be rented, while all
     * rental history stays intact. A vehicle that is out on rent or in maintenance is refused,
     * because returning it / completing maintenance would otherwise flip it back to AVAILABLE.
     */
    public ServiceResult<Void> deactivateVehicle(int vehicleId) {
        try {
            Vehicle v = vehicleDAO.getVehicleById(vehicleId);
            if (v == null) return ServiceResult.fail("Vehicle not found.");
            String status = v.getStatus();
            if ("INACTIVE".equalsIgnoreCase(status)) return ServiceResult.fail("This vehicle is already inactive.");
            if ("RENTED".equalsIgnoreCase(status))
                return ServiceResult.fail("This vehicle is currently rented. Deactivate it after it has been returned.");
            if ("MAINTENANCE".equalsIgnoreCase(status))
                return ServiceResult.fail("This vehicle is under maintenance. Complete the maintenance first.");
            vehicleDAO.updateVehicleStatus(vehicleId, "INACTIVE");
            return ServiceResult.ok("Vehicle deactivated.", null);
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to update the vehicle status.");
        }
    }

    /** Brings an INACTIVE vehicle back into the fleet as AVAILABLE. */
    public ServiceResult<Void> activateVehicle(int vehicleId) {
        try {
            Vehicle v = vehicleDAO.getVehicleById(vehicleId);
            if (v == null) return ServiceResult.fail("Vehicle not found.");
            if (!"INACTIVE".equalsIgnoreCase(v.getStatus()))
                return ServiceResult.fail("Only an inactive vehicle can be activated.");
            vehicleDAO.updateVehicleStatus(vehicleId, "AVAILABLE");
            return ServiceResult.ok("Vehicle activated and available for rent.", null);
        } catch (SQLException e) {
            return ServiceResult.dbError(e, "Unable to update the vehicle status.");
        }
    }

    public List<Vehicle> getAllVehicles() throws SQLException {
        return vehicleDAO.getAllVehicles();
    }

    public List<Vehicle> search(String keyword, String type, String availability) throws SQLException {
        return vehicleDAO.searchVehicles(keyword, type, availability);
    }

    public Vehicle getById(int vehicleId) throws SQLException {
        return vehicleDAO.getVehicleById(vehicleId);
    }

    public void updateStatus(int vehicleId, String status) throws SQLException {
        vehicleDAO.updateVehicleStatus(vehicleId, status);
    }

    private ServiceResult<Vehicle> validateVehicleInput(String name, String type, String regNo, String fuel,
                                                          int seats, double pricePerDay) {
        if (!ValidationUtil.isNotEmpty(name)) return ServiceResult.fail("Vehicle name is required.");
        if (!ValidationUtil.isNotEmpty(type) || !(type.equalsIgnoreCase("CAR") || type.equalsIgnoreCase("BIKE")))
            return ServiceResult.fail("Vehicle type must be CAR or BIKE.");
        if (!ValidationUtil.isNotEmpty(regNo)) return ServiceResult.fail("Registration number is required.");
        if (!ValidationUtil.isNotEmpty(fuel)) return ServiceResult.fail("Fuel type is required.");
        if (!ValidationUtil.isValidSeats(seats)) return ServiceResult.fail("Seats must be between 1 and 10.");
        if (!ValidationUtil.isPositiveNumber(pricePerDay)) return ServiceResult.fail("Price per day must be greater than 0.");
        return null; // no error
    }

    private Vehicle buildVehicle(int id, String name, String type, String regNo, String fuel,
                                  int seats, double price, String status) {
        if (type.equalsIgnoreCase("BIKE")) {
            return new Bike(id, name, regNo, fuel, seats, price, status);
        }
        return new Car(id, name, regNo, fuel, seats, price, status);
    }
}
