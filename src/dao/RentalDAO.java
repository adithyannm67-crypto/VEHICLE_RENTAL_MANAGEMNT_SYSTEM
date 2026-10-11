package dao;

import model.Rental;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** All direct database access for the "rentals" table. */
public class RentalDAO {

    /**
     * Returns true if the vehicle already has a CONFIRMED booking whose
     * date range overlaps with [pickup, returnDate]. Used to stop double-booking.
     */
    public boolean hasConflict(int vehicleId, LocalDate pickup, LocalDate returnDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rentals WHERE vehicle_id = ? AND rental_status = 'CONFIRMED' " +
                     "AND pickup_date <= ? AND return_date >= ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.setDate(2, Date.valueOf(returnDate));
            ps.setDate(3, Date.valueOf(pickup));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /** Inserts a rental and returns the generated rental_id. */
    public int createRental(Rental r) throws SQLException {
        String sql = "INSERT INTO rentals (user_id, vehicle_id, pickup_date, return_date, total_days, total_amount, rental_status) " +
                     "VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getUserId());
            ps.setInt(2, r.getVehicleId());
            ps.setDate(3, Date.valueOf(r.getPickupDate()));
            ps.setDate(4, Date.valueOf(r.getReturnDate()));
            ps.setInt(5, r.getTotalDays());
            ps.setDouble(6, r.getTotalAmount());
            ps.setString(7, r.getRentalStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateRentalStatus(int rentalId, String status) throws SQLException {
        String sql = "UPDATE rentals SET rental_status=? WHERE rental_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, rentalId);
            return ps.executeUpdate() == 1;
        }
    }

    public Rental getRentalById(int rentalId) throws SQLException {
        String sql = "SELECT r.*, v.vehicle_name, u.name AS customer_name FROM rentals r " +
                     "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "JOIN users u ON r.user_id = u.user_id WHERE r.rental_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rentalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Rental> getRentalsByUser(int userId) throws SQLException {
        List<Rental> list = new ArrayList<>();
        String sql = "SELECT r.*, v.vehicle_name, u.name AS customer_name FROM rentals r " +
                     "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.user_id = ? ORDER BY r.rental_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Rental> getAllRentals() throws SQLException {
        List<Rental> list = new ArrayList<>();
        String sql = "SELECT r.*, v.vehicle_name, u.name AS customer_name FROM rentals r " +
                     "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "JOIN users u ON r.user_id = u.user_id ORDER BY r.rental_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Rental mapRow(ResultSet rs) throws SQLException {
        Rental r = new Rental();
        r.setRentalId(rs.getInt("rental_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setPickupDate(rs.getDate("pickup_date").toLocalDate());
        r.setReturnDate(rs.getDate("return_date").toLocalDate());
        r.setTotalDays(rs.getInt("total_days"));
        r.setTotalAmount(rs.getDouble("total_amount"));
        r.setRentalStatus(rs.getString("rental_status"));
        Timestamp ts = rs.getTimestamp("created_at");
        r.setCreatedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        r.setVehicleName(rs.getString("vehicle_name"));
        r.setCustomerName(rs.getString("customer_name"));
        return r;
    }
}
