package dao;

import model.User;
import util.DBConnection;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** All direct database access for the "users" table. */
public class UserDAO {

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a new user. Password must already be hashed by the caller (service layer). */
    public boolean registerUser(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, phone, password, address, role) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getAddress());
            ps.setString(6, user.getRole());
            return ps.executeUpdate() == 1;
        }
    }

    /** Returns the matching User only if the raw password matches the stored hash, else null. */
    public User login(String email, String rawPassword) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtil.matches(rawPassword, storedHash)) {
                        return mapRow(rs);
                    }
                }
            }
        }
        return null;
    }

    /** Updates the editable profile fields of one user. */
    public boolean updateProfile(int userId, String name, String phone, String address) throws SQLException {
        String sql = "UPDATE users SET name=?, phone=?, address=? WHERE user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setInt(4, userId);
            return ps.executeUpdate() >= 0;
        }
    }

    public List<User> getAllCustomers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'CUSTOMER' ORDER BY user_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Creates the default admin account (admin@vrs.com / Admin@123) the first time the app runs. */
    public void ensureDefaultAdmin() throws SQLException {
        if (!emailExists("admin@vrs.com")) {
            User admin = new User(0, "System Admin", "admin@vrs.com", "9999999999",
                    PasswordUtil.hash("Admin@123"), "Head Office", "ADMIN");
            registerUser(admin);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("password"),
                rs.getString("address"),
                rs.getString("role")
        );
    }
}
