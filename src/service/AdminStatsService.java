package service;

import dao.PaymentDAO;
import dao.RentalDAO;
import dao.UserDAO;
import dao.VehicleDAO;
import model.Rental;
import model.User;
import model.Vehicle;

import java.sql.SQLException;
import java.util.List;

/** Gathers the small set of numbers shown on the Admin Dashboard. */
public class AdminStatsService {

    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final UserDAO userDAO = new UserDAO();
    private final RentalDAO rentalDAO = new RentalDAO();

    public static class Stats {
        public int totalVehicles;
        public int availableVehicles;
        public int rentedVehicles;
        public int totalCustomers;
        public int activeRentals;
        public int completedRentals;
    }

    public Stats gather() throws SQLException {
        Stats s = new Stats();
        List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
        s.totalVehicles = vehicles.size();
        for (Vehicle v : vehicles) {
            if ("AVAILABLE".equalsIgnoreCase(v.getStatus())) s.availableVehicles++;
            if ("RENTED".equalsIgnoreCase(v.getStatus())) s.rentedVehicles++;
        }
        List<User> customers = userDAO.getAllCustomers();
        s.totalCustomers = customers.size();

        List<Rental> rentals = rentalDAO.getAllRentals();
        for (Rental r : rentals) {
            if ("CONFIRMED".equalsIgnoreCase(r.getRentalStatus())) s.activeRentals++;
            if ("COMPLETED".equalsIgnoreCase(r.getRentalStatus())) s.completedRentals++;
        }
        return s;
    }
}
