package dao;

import model.Bike;
import model.Car;
import model.Vehicle;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** All direct database access for the "vehicles" table. */
public class VehicleDAO {

    public boolean registrationNumberExists(String regNo) throws SQLException {
        String sql = "SELECT vehicle_id FROM vehicles WHERE registration_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, regNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean addVehicle(Vehicle v) throws SQLException {
        String sql = "INSERT INTO vehicles (vehicle_name, vehicle_type, registration_number, fuel_type, seats, price_per_day, status) " +
                     "VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getVehicleName());
            ps.setString(2, v.getVehicleType());
            ps.setString(3, v.getRegistrationNumber());
            ps.setString(4, v.getFuelType());
            ps.setInt(5, v.getSeats());
            ps.setDouble(6, v.getPricePerDay());
            ps.setString(7, v.getStatus());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateVehicle(Vehicle v) throws SQLException {
        String sql = "UPDATE vehicles SET vehicle_name=?, vehicle_type=?, registration_number=?, fuel_type=?, " +
                     "seats=?, price_per_day=?, status=? WHERE vehicle_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getVehicleName());
            ps.setString(2, v.getVehicleType());
            ps.setString(3, v.getRegistrationNumber());
            ps.setString(4, v.getFuelType());
            ps.setInt(5, v.getSeats());
            ps.setDouble(6, v.getPricePerDay());
            ps.setString(7, v.getStatus());
            ps.setInt(8, v.getVehicleId());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateVehicleStatus(int vehicleId, String status) throws SQLException {
        String sql = "UPDATE vehicles SET status=? WHERE vehicle_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, vehicleId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean deleteVehicle(int vehicleId) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE vehicle_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            return ps.executeUpdate() == 1;
        }
    }

    public Vehicle getVehicleById(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE vehicle_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Vehicle> getAllVehicles() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicles ORDER BY vehicle_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Searches vehicles by an optional keyword (matched against name/registration),
     * an optional type filter ("ALL", "CAR", "BIKE") and an optional availability
     * filter ("ALL", "AVAILABLE", "RENTED", "MAINTENANCE", "INACTIVE").
     */
    public List<Vehicle> searchVehicles(String keyword, String type, String availability) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vehicles WHERE 1=1 ");
        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (vehicle_name LIKE ? OR registration_number LIKE ?) ");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }
        if (type != null && !type.equalsIgnoreCase("ALL")) {
            sql.append("AND vehicle_type = ? ");
            params.add(type);
        }
        if (availability != null && !availability.equalsIgnoreCase("ALL")) {
            sql.append("AND status = ? ");
            params.add(availability);
        }
        sql.append("ORDER BY vehicle_id");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Factory method: builds a Car or Bike object depending on the vehicle_type column. */
    private Vehicle mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("vehicle_id");
        String name = rs.getString("vehicle_name");
        String type = rs.getString("vehicle_type");
        String reg = rs.getString("registration_number");
        String fuel = rs.getString("fuel_type");
        int seats = rs.getInt("seats");
        double price = rs.getDouble("price_per_day");
        String status = rs.getString("status");

        if ("BIKE".equalsIgnoreCase(type)) {
            return new Bike(id, name, reg, fuel, seats, price, status);
        }
        return new Car(id, name, reg, fuel, seats, price, status);
    }
}
