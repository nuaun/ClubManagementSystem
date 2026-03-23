package com.iscms.dao;

import com.iscms.model.Trainer;
import com.iscms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainerDAOImpl implements TrainerDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void insert(Trainer t) {
        String sql = "INSERT INTO trainer (full_name, username, password, specialty, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getFullName());
            ps.setString(2, t.getUsername());
            ps.setString(3, t.getPassword());
            ps.setString(4, t.getSpecialty());
            ps.setBoolean(5, t.isActive());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insert trainer failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateActiveStatus(int trainerId, boolean isActive) {
        String sql = "UPDATE trainer SET is_active=? WHERE trainer_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isActive);
            ps.setInt(2, trainerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateActiveStatus failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateFailedAttempts(int trainerId, int attempts) {
        String sql = "UPDATE trainer SET failed_attempts=? WHERE trainer_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attempts);
            ps.setInt(2, trainerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateFailedAttempts failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLockStatus(int trainerId, boolean isLocked) {
        String sql = "UPDATE trainer SET is_locked=? WHERE trainer_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isLocked);
            ps.setInt(2, trainerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateLockStatus failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateCredentials(int trainerId, String username, String password) {
        String sql = "UPDATE trainer SET username=?, password=? WHERE trainer_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setInt(3, trainerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateCredentials failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Trainer> findById(int trainerId) {
        String sql = "SELECT * FROM trainer WHERE trainer_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        String sql = "SELECT * FROM trainer WHERE username=?";
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
    public List<Trainer> findAll() {
        List<Trainer> list = new ArrayList<>();
        String sql = "SELECT * FROM trainer ORDER BY full_name";
        try (Connection conn = getConn(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll trainers failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Trainer> findActive() {
        List<Trainer> list = new ArrayList<>();
        String sql = "SELECT * FROM trainer WHERE is_active=1 ORDER BY full_name";
        try (Connection conn = getConn(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findActive trainers failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void updateProfile(int trainerId, String username, String specialty, String hashedPassword) {
        String sql = hashedPassword == null
                ? "UPDATE trainer SET username=?, specialty=? WHERE trainer_id=?"
                : "UPDATE trainer SET username=?, specialty=?, password=? WHERE trainer_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, specialty);
            if (hashedPassword != null) {
                ps.setString(3, hashedPassword);
                ps.setInt(4, trainerId);
            } else {
                ps.setInt(3, trainerId);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateProfile failed: " + e.getMessage(), e);
        }
    }

    private Trainer mapRow(ResultSet rs) throws SQLException {
        Trainer t = new Trainer();
        t.setTrainerId(rs.getInt("trainer_id"));
        t.setFullName(rs.getString("full_name"));
        t.setUsername(rs.getString("username"));
        t.setPassword(rs.getString("password"));
        t.setSpecialty(rs.getString("specialty"));
        t.setActive(rs.getBoolean("is_active"));
        t.setFailedAttempts(rs.getInt("failed_attempts"));
        t.setLocked(rs.getBoolean("is_locked"));
        t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return t;
    }
}