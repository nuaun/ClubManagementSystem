package com.iscms.dao;

import com.iscms.model.Event;
import com.iscms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventDAOImpl implements EventDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void insert(Event e) {
        String sql = "INSERT INTO event (event_name, category, event_date, start_time, end_time, " +
                "location, capacity, fee, min_tier, early_access_hours, description, status, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getEventName());
            ps.setString(2, e.getCategory());
            ps.setDate(3, Date.valueOf(e.getEventDate()));
            ps.setTime(4, Time.valueOf(e.getStartTime()));
            ps.setTime(5, Time.valueOf(e.getEndTime()));
            ps.setString(6, e.getLocation());
            ps.setInt(7, e.getCapacity());
            ps.setDouble(8, e.getFee());
            ps.setString(9, e.getMinTier());
            ps.setInt(10, e.getEarlyAccessHours());
            ps.setString(11, e.getDescription());
            ps.setString(12, e.getStatus() != null ? e.getStatus() : "ACTIVE");
            ps.setInt(13, e.getCreatedBy());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("insert event failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void update(Event e) {
        String sql = "UPDATE event SET event_name=?, category=?, event_date=?, start_time=?, end_time=?, " +
                "location=?, capacity=?, fee=?, min_tier=?, description=? WHERE event_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getEventName());
            ps.setString(2, e.getCategory());
            ps.setDate(3, Date.valueOf(e.getEventDate()));
            ps.setTime(4, Time.valueOf(e.getStartTime()));
            ps.setTime(5, Time.valueOf(e.getEndTime()));
            ps.setString(6, e.getLocation());
            ps.setInt(7, e.getCapacity());
            ps.setDouble(8, e.getFee());
            ps.setString(9, e.getMinTier());
            ps.setString(10, e.getDescription());
            ps.setInt(11, e.getEventId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("update event failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void updateStatus(int eventId, String status) {
        String sql = "UPDATE event SET status=? WHERE event_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, eventId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("updateStatus failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Optional<Event> findById(int eventId) {
        String sql = "SELECT * FROM event WHERE event_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("findById failed: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Event> findAll() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event ORDER BY event_date DESC";
        try (Connection conn = getConn(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            throw new RuntimeException("findAll failed: " + ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Event> findActiveEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event WHERE status='ACTIVE' AND event_date >= CURDATE() ORDER BY event_date";
        try (Connection conn = getConn(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            throw new RuntimeException("findActiveEvents failed: " + ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Event> findByMinTier(String tier) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event WHERE min_tier=? AND status='ACTIVE' ORDER BY event_date";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tier);
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("findByMinTier failed: " + ex.getMessage(), ex);
        }
        return list;
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setEventId(rs.getInt("event_id"));
        e.setEventName(rs.getString("event_name"));
        e.setCategory(rs.getString("category"));
        e.setEventDate(rs.getDate("event_date").toLocalDate());
        e.setStartTime(rs.getTime("start_time").toLocalTime());
        e.setEndTime(rs.getTime("end_time").toLocalTime());
        e.setLocation(rs.getString("location"));
        e.setCapacity(rs.getInt("capacity"));
        e.setFee(rs.getDouble("fee"));
        e.setMinTier(rs.getString("min_tier"));
        e.setEarlyAccessHours(rs.getInt("early_access_hours"));
        e.setDescription(rs.getString("description"));
        e.setStatus(rs.getString("status"));
        e.setCreatedBy(rs.getInt("created_by"));
        e.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return e;
    }
}