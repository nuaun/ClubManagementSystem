package com.iscms.dao;

import com.iscms.model.Payment;
import com.iscms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAOImpl implements PaymentDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void insert(Payment p) {
        String sql = "INSERT INTO payment (member_id, amount, payment_date, payment_type, description, status, recorded_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getMemberId());
            ps.setDouble(2, p.getAmount());
            ps.setTimestamp(3, p.getPaymentDate() != null ?
                    Timestamp.valueOf(p.getPaymentDate()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(4, p.getPaymentType());
            ps.setString(5, p.getDescription());
            ps.setString(6, p.getStatus());
            ps.setInt(7, p.getRecordedBy());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insert payment failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStatus(int paymentId, String status) {
        String sql = "UPDATE payment SET status=? WHERE payment_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, paymentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateStatus failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Payment> findAllByMemberId(int memberId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payment WHERE member_id=? ORDER BY payment_date DESC";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findAllByMemberId failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Payment> findByStatus(String status) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payment WHERE status=? ORDER BY payment_date DESC";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByStatus failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payment ORDER BY payment_date DESC";
        try (Connection conn = getConn(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll failed: " + e.getMessage(), e);
        }
        return list;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setMemberId(rs.getInt("member_id"));
        p.setAmount(rs.getDouble("amount"));
        p.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
        p.setPaymentType(rs.getString("payment_type"));
        p.setDescription(rs.getString("description"));
        p.setStatus(rs.getString("status"));
        p.setRecordedBy(rs.getInt("recorded_by"));
        return p;
    }
}