package dao;

import model.Maintenance;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** All direct database access for the "maintenance" table. */
public class MaintenanceDAO {

    public boolean addMaintenance(Maintenance m) throws SQLException {
        String sql = "INSERT INTO maintenance (vehicle_id, description, maintenance_date, cost, status) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getVehicleId());
            ps.setString(2, m.getDescription());
            ps.setDate(3, Date.valueOf(m.getMaintenanceDate()));
            ps.setDouble(4, m.getCost());
            ps.setString(5, m.getStatus());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateMaintenanceStatus(int maintenanceId, String status) throws SQLException {
        String sql = "UPDATE maintenance SET status=? WHERE maintenance_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, maintenanceId);
            return ps.executeUpdate() == 1;
        }
    }

    public List<Maintenance> getAllMaintenance() throws SQLException {
        List<Maintenance> list = new ArrayList<>();
        String sql = "SELECT m.*, v.vehicle_name FROM maintenance m " +
                     "JOIN vehicles v ON m.vehicle_id = v.vehicle_id ORDER BY m.maintenance_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Maintenance> getMaintenanceByVehicle(int vehicleId) throws SQLException {
        List<Maintenance> list = new ArrayList<>();
        String sql = "SELECT m.*, v.vehicle_name FROM maintenance m " +
                     "JOIN vehicles v ON m.vehicle_id = v.vehicle_id WHERE m.vehicle_id = ? ORDER BY m.maintenance_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Maintenance mapRow(ResultSet rs) throws SQLException {
        Maintenance m = new Maintenance();
        m.setMaintenanceId(rs.getInt("maintenance_id"));
        m.setVehicleId(rs.getInt("vehicle_id"));
        m.setDescription(rs.getString("description"));
        m.setMaintenanceDate(rs.getDate("maintenance_date").toLocalDate());
        m.setCost(rs.getDouble("cost"));
        m.setStatus(rs.getString("status"));
        m.setVehicleName(rs.getString("vehicle_name"));
        return m;
    }
}
