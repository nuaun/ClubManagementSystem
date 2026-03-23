package com.iscms.dao;

import com.iscms.model.Manager;
import com.iscms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManagerDAOImpl implements ManagerDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void insert(Manager manager) {
        // FIX-9 (katman): Hash işi Service katmanına taşındı.
        // DAO'ya gelen şifre artık zaten hashlenmiş olarak gelir.
        // ManagerService.addManager() çağırmadan önce AuthService.hashPassword() uygulanmalı.
        String sql = "INSERT INTO manager (full_name, username, email, role, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manager.getFullName());
            ps.setString(2, manager.getUsername());
            ps.setString(3, manager.getEmail());
            ps.setString(4, manager.getRole() != null ? manager.getRole() : "MANAGER");
            ps.setString(5, manager.getPassword()); // geldiği haliyle kaydet — hash Service'de yapıldı
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insert manager failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int managerId) {
        String sql = "DELETE FROM manager WHERE manager_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete manager failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Manager> findAll() {
        List<Manager> list = new ArrayList<>();
        String sql = "SELECT * FROM manager ORDER BY created_at DESC";
        try (Connection conn = getConn(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll managers failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<Manager> findByEmail(String email) {
        String sql = "SELECT * FROM manager WHERE email=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByEmail failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Manager> findByUsername(String username) {
        String sql = "SELECT * FROM manager WHERE username=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByUsername failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void updateFailedAttempts(int managerId, int attempts) {
        String sql = "UPDATE manager SET failed_attempts=? WHERE manager_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attempts);
            ps.setInt(2, managerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateFailedAttempts failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLockStatus(int managerId, boolean isLocked) {
        String sql = "UPDATE manager SET is_locked=? WHERE manager_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isLocked);
            ps.setInt(2, managerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateLockStatus failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updatePassword(int managerId, String hashedPassword) {
        String sql = "UPDATE manager SET password=? WHERE manager_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, managerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updatePassword failed: " + e.getMessage(), e);
        }
    }

    private Manager mapRow(ResultSet rs) throws SQLException {
        Manager mg = new Manager();
        mg.setManagerId(rs.getInt("manager_id"));
        mg.setFullName(rs.getString("full_name"));
        mg.setUsername(rs.getString("username"));
        mg.setEmail(rs.getString("email"));
        mg.setRole(rs.getString("role"));
        mg.setPassword(rs.getString("password"));
        mg.setFailedAttempts(rs.getInt("failed_attempts"));
        mg.setLocked(rs.getBoolean("is_locked"));
        mg.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return mg;
    }
}