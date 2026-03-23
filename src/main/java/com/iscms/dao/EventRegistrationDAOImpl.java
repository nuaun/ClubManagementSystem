package com.iscms.dao;

import com.iscms.model.EventRegistration;
import com.iscms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventRegistrationDAOImpl implements EventRegistrationDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void insert(EventRegistration reg) {
        String sql = "INSERT INTO event_registration (member_id, event_id, payment_status, waitlist_position) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reg.getMemberId());
            ps.setInt(2, reg.getEventId());
            String payStatus = reg.getPaymentStatus() != null ? reg.getPaymentStatus() : "FREE";
            ps.setString(3, payStatus);
            if (reg.getWaitlistPosition() != null) ps.setInt(4, reg.getWaitlistPosition());
            else ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insert registration failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteByMemberAndEvent(int memberId, int eventId) {
        String sql = "DELETE FROM event_registration WHERE member_id=? AND event_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, eventId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteByMemberAndEvent failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteByEventId(int eventId) {
        String sql = "DELETE FROM event_registration WHERE event_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteByEventId failed: " + e.getMessage(), e);
        }
    }

    // FIX-3: Etkinlik iptalinde kayıtları silme — status güncelle, veri korunsun.
    // FIX-BUG7: Sadece fiilen kayıtlı olanlar (waitlist_position IS NULL) iptal edilmeli.
    // Waitlist kayıtları zaten WAITLISTED statüsünde — onları CANCELLED yapmak
    // hem countRegistered hem de raporlama sorgularını bozuyor.
    @Override
    public void cancelAllByEventId(int eventId) {
        String sql = "UPDATE event_registration SET payment_status='CANCELLED' " +
                "WHERE event_id=? AND waitlist_position IS NULL";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("cancelAllByEventId failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void promoteFromWaitlist(int registrationId) {
        String sql = "UPDATE event_registration SET waitlist_position=NULL WHERE registration_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, registrationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("promoteFromWaitlist failed: " + e.getMessage(), e);
        }
    }

    // FIX-KRİTİK-3: promote sonrası kalan sıra numaraları 1 azaltılır
    // Örn: [2,3,4] → [1,2,3]
    @Override
    public void reorderWaitlistAfterPromotion(int eventId) {
        String sql = "UPDATE event_registration " +
                "SET waitlist_position = waitlist_position - 1 " +
                "WHERE event_id = ? AND waitlist_position IS NOT NULL";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("reorderWaitlistAfterPromotion failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByMemberAndEvent(int memberId, int eventId) {
        String sql = "SELECT COUNT(*) FROM event_registration WHERE member_id=? AND event_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("existsByMemberAndEvent failed: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public int countRegistered(int eventId) {
        // FIX-BUG7: WAITLISTED kayıtlar da kapasite sayısına dahil edilmemeli.
        // Sadece gerçek kayıtlar (waitlist_position IS NULL, CANCELLED/WAITLISTED olmayan) sayılır.
        String sql = "SELECT COUNT(*) FROM event_registration " +
                "WHERE event_id=? AND waitlist_position IS NULL " +
                "AND payment_status NOT IN ('CANCELLED', 'WAITLISTED')";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("countRegistered failed: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int getMaxWaitlistPosition(int eventId) {
        String sql = "SELECT COALESCE(MAX(waitlist_position), 0) FROM event_registration WHERE event_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("getMaxWaitlistPosition failed: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public List<EventRegistration> findByEventId(int eventId) {
        List<EventRegistration> list = new ArrayList<>();
        String sql = "SELECT * FROM event_registration WHERE event_id=? ORDER BY waitlist_position IS NULL DESC";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByEventId failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<EventRegistration> findByMemberId(int memberId) {
        List<EventRegistration> list = new ArrayList<>();
        String sql = "SELECT * FROM event_registration WHERE member_id=? ORDER BY registration_date DESC";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByMemberId failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<EventRegistration> findFirstWaitlist(int eventId) {
        String sql = "SELECT * FROM event_registration WHERE event_id=? AND waitlist_position IS NOT NULL " +
                "ORDER BY waitlist_position ASC LIMIT 1";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findFirstWaitlist failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private EventRegistration mapRow(ResultSet rs) throws SQLException {
        EventRegistration reg = new EventRegistration();
        reg.setRegistrationId(rs.getInt("registration_id"));
        reg.setMemberId(rs.getInt("member_id"));
        reg.setEventId(rs.getInt("event_id"));
        Timestamp ts = rs.getTimestamp("registration_date");
        if (ts != null) reg.setRegistrationDate(ts.toLocalDateTime());
        reg.setPaymentStatus(rs.getString("payment_status"));
        int wp = rs.getInt("waitlist_position");
        reg.setWaitlistPosition(rs.wasNull() ? null : wp);
        return reg;
    }
}