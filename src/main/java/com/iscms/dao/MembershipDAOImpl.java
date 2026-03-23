package com.iscms.dao;

import com.iscms.model.Membership;
import com.iscms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MembershipDAOImpl implements MembershipDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void insert(Membership ms) {
        String sql = "INSERT INTO membership (member_id, tier, package, start_date, end_date, status, freeze_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ms.getMemberId());
            ps.setString(2, ms.getTier());
            ps.setString(3, ms.getPackageType());
            ps.setDate(4, Date.valueOf(ms.getStartDate()));
            ps.setDate(5, Date.valueOf(ms.getEndDate()));
            ps.setString(6, ms.getStatus());
            ps.setInt(7, ms.getFreezeCount());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insert membership failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Membership ms) {
        String sql = "UPDATE membership SET tier=?, package=?, start_date=?, end_date=?, status=?, freeze_count=? " +
                "WHERE membership_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ms.getTier());
            ps.setString(2, ms.getPackageType());
            ps.setDate(3, Date.valueOf(ms.getStartDate()));
            ps.setDate(4, Date.valueOf(ms.getEndDate()));
            ps.setString(5, ms.getStatus());
            ps.setInt(6, ms.getFreezeCount());
            ps.setInt(7, ms.getMembershipId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("update membership failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStatus(int membershipId, String status) {
        String sql = "UPDATE membership SET status=? WHERE membership_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, membershipId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateStatus failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void incrementFreezeCount(int membershipId) {
        String sql = "UPDATE membership SET freeze_count = freeze_count + 1 WHERE membership_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, membershipId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("incrementFreezeCount failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Membership> findById(int membershipId) {
        String sql = "SELECT * FROM membership WHERE membership_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, membershipId);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Membership> findActiveByMemberId(int memberId) {
        String sql = "SELECT * FROM membership WHERE member_id=? AND status='ACTIVE' ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findActiveByMemberId failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // BUG-3 FIX: needed to check whether a FROZEN membership's freeze period has elapsed at login
    @Override
    public Optional<Membership> findFrozenByMemberId(int memberId) {
        String sql = "SELECT * FROM membership WHERE member_id=? AND status='FROZEN' ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findFrozenByMemberId failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Membership> findAllByMemberId(int memberId) {
        List<Membership> list = new ArrayList<>();
        String sql = "SELECT * FROM membership WHERE member_id=? ORDER BY created_at DESC";
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
    public List<Membership> findExpiringSoon(int days) {
        List<Membership> list = new ArrayList<>();
        String sql = "SELECT * FROM membership WHERE status='ACTIVE' " +
                "AND end_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findExpiringSoon failed: " + e.getMessage(), e);
        }
        return list;
    }

    private Membership mapRow(ResultSet rs) throws SQLException {
        Membership ms = new Membership();
        ms.setMembershipId(rs.getInt("membership_id"));
        ms.setMemberId(rs.getInt("member_id"));
        ms.setTier(rs.getString("tier"));
        ms.setPackageType(rs.getString("package"));
        ms.setStartDate(rs.getDate("start_date").toLocalDate());
        ms.setEndDate(rs.getDate("end_date").toLocalDate());
        ms.setStatus(rs.getString("status"));
        ms.setFreezeCount(rs.getInt("freeze_count"));
        ms.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return ms;
    }
}