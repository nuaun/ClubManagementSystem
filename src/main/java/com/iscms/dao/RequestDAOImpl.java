package com.iscms.dao;

import com.iscms.model.RegistrationRequest;
import com.iscms.model.TierUpgradeRequest;
import com.iscms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestDAOImpl implements RequestDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void insertTierUpgrade(TierUpgradeRequest req) {
        String sql = "INSERT INTO tier_upgrade_request " +
                "(member_id, membership_id, current_tier, requested_tier, upgrade_fee, expires_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, req.getMemberId());
            ps.setInt(2, req.getMembershipId());
            ps.setString(3, req.getCurrentTier());
            ps.setString(4, req.getRequestedTier());
            ps.setDouble(5, req.getUpgradeFee());
            ps.setTimestamp(6, Timestamp.valueOf(req.getExpiresAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertTierUpgrade failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateTierUpgradeStatus(int requestId, String status) {
        String sql = "UPDATE tier_upgrade_request SET status=? WHERE request_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateTierUpgradeStatus failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TierUpgradeRequest> findPendingTierUpgrades() {
        List<TierUpgradeRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM tier_upgrade_request WHERE status='PENDING' ORDER BY created_at";
        try (Connection conn = getConn(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapTierUpgrade(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findPendingTierUpgrades failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TierUpgradeRequest> findTierUpgradesByMember(int memberId) {
        List<TierUpgradeRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM tier_upgrade_request WHERE member_id=? ORDER BY created_at DESC";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapTierUpgrade(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findTierUpgradesByMember failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void expireOldTierUpgrades() {
        String sql = "UPDATE tier_upgrade_request SET status='FAILED' " +
                "WHERE status='PENDING' AND expires_at < NOW()";
        try (Connection conn = getConn(); Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("expireOldTierUpgrades failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void insertRegistration(RegistrationRequest req) {
        String sql = "INSERT INTO registration_request " +
                "(member_id, tier, package_type, amount, expires_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, req.getMemberId());
            ps.setString(2, req.getTier());
            ps.setString(3, req.getPackageType());
            ps.setDouble(4, req.getAmount());
            ps.setTimestamp(5, Timestamp.valueOf(req.getExpiresAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertRegistration failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateRegistrationStatus(int requestId, String status) {
        String sql = "UPDATE registration_request SET status=? WHERE request_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateRegistrationStatus failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RegistrationRequest> findPendingRegistrations() {
        List<RegistrationRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM registration_request WHERE status='PENDING' ORDER BY created_at";
        try (Connection conn = getConn(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRegistration(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findPendingRegistrations failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void expireOldRegistrations() {
        String sql = "UPDATE registration_request SET status='FAILED' " +
                "WHERE status='PENDING' AND expires_at < NOW()";
        try (Connection conn = getConn(); Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("expireOldRegistrations failed: " + e.getMessage(), e);
        }
    }

    // BUG-2 FIX: Returns requests that are PENDING but already expired, BEFORE marking them FAILED.
    // Used by MemberService.expireOldRequests() to sync member.status → REGISTRATION_FAILED.
    @Override
    public List<RegistrationRequest> findExpiredPendingRegistrations() {
        List<RegistrationRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM registration_request WHERE status='PENDING' AND expires_at < NOW()";
        try (Connection conn = getConn(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRegistration(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findExpiredPendingRegistrations failed: " + e.getMessage(), e);
        }
        return list;
    }

    private TierUpgradeRequest mapTierUpgrade(ResultSet rs) throws SQLException {
        TierUpgradeRequest r = new TierUpgradeRequest();
        r.setRequestId(rs.getInt("request_id"));
        r.setMemberId(rs.getInt("member_id"));
        r.setMembershipId(rs.getInt("membership_id"));
        r.setCurrentTier(rs.getString("current_tier"));
        r.setRequestedTier(rs.getString("requested_tier"));
        r.setUpgradeFee(rs.getDouble("upgrade_fee"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        r.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        return r;
    }

    private RegistrationRequest mapRegistration(ResultSet rs) throws SQLException {
        RegistrationRequest r = new RegistrationRequest();
        r.setRequestId(rs.getInt("request_id"));
        r.setMemberId(rs.getInt("member_id"));
        r.setTier(rs.getString("tier"));
        r.setPackageType(rs.getString("package_type"));
        r.setAmount(rs.getDouble("amount"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        r.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        return r;
    }
}