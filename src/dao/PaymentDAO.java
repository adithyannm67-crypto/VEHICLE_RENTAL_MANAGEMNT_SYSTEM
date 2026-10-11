package dao;

import model.Payment;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** All direct database access for the "payments" table. */
public class PaymentDAO {

    public int createPayment(Payment p) throws SQLException {
        String sql = "INSERT INTO payments (rental_id, amount, payment_method, payment_status) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getRentalId());
            ps.setDouble(2, p.getAmount());
            ps.setString(3, p.getPaymentMethod());
            ps.setString(4, p.getPaymentStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<Payment> getPaymentsByRental(int rentalId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE rental_id = ? ORDER BY payment_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rentalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setRentalId(rs.getInt("rental_id"));
        Timestamp ts = rs.getTimestamp("payment_date");
        p.setPaymentDate(ts != null ? ts.toLocalDateTime() : null);
        p.setAmount(rs.getDouble("amount"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setPaymentStatus(rs.getString("payment_status"));
        return p;
    }
}
