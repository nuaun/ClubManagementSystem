package com.iscms.dao;

import com.iscms.model.Member;
import com.iscms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDAOImpl implements MemberDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void insert(Member m) {
        String sql = "INSERT INTO member (full_name, date_of_birth, gender, phone, email, password, " +
                "weight, height, bmi_value, bmi_category, " +
                "emergency_contact_name, emergency_contact_phone, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getFullName());
            // FIX-BUG9: dateOfBirth null ise Date.valueOf() NullPointerException fırlatır
            ps.setDate(2, m.getDateOfBirth() != null ? Date.valueOf(m.getDateOfBirth()) : null);
            ps.setString(3, m.getGender());
            ps.setString(4, m.getPhone());
            ps.setString(5, m.getEmail());
            ps.setString(6, m.getPassword());
            ps.setObject(7, m.getWeight());
            ps.setObject(8, m.getHeight());
            ps.setObject(9, m.getBmiValue());
            ps.setString(10, m.getBmiCategory());
            ps.setString(11, m.getEmergencyContactName());
            ps.setString(12, m.getEmergencyContactPhone());
            ps.setString(13, m.getStatus() != null ? m.getStatus() : "PASSIVE");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insert member failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Member m) {
        String sql = "UPDATE member SET full_name=?, date_of_birth=?, gender=?, phone=?, email=?, " +
                "weight=?, height=?, bmi_value=?, bmi_category=?, bmi_updated_at=NOW(), " +
                "emergency_contact_name=?, emergency_contact_phone=? " +
                "WHERE member_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getFullName());
            // NULL-GUARD: dateOfBirth may be null for members registered without DOB
            ps.setDate(2, m.getDateOfBirth() != null ? Date.valueOf(m.getDateOfBirth()) : null);
            ps.setString(3, m.getGender());
            ps.setString(4, m.getPhone());
            ps.setString(5, m.getEmail());
            ps.setObject(6, m.getWeight());
            ps.setObject(7, m.getHeight());
            ps.setObject(8, m.getBmiValue());
            ps.setString(9, m.getBmiCategory());
            ps.setString(10, m.getEmergencyContactName());
            ps.setString(11, m.getEmergencyContactPhone());
            ps.setInt(12, m.getMemberId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("update member failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStatus(int memberId, String status) {
        String sql = "UPDATE member SET status=? WHERE member_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateStatus failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateFailedAttempts(int memberId, int attempts) {
        String sql = "UPDATE member SET failed_attempts=? WHERE member_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attempts);
            ps.setInt(2, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateFailedAttempts failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLockStatus(int memberId, boolean isLocked) {
        String sql = "UPDATE member SET is_locked=? WHERE member_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isLocked);
            ps.setInt(2, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateLockStatus failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Member> findById(int memberId) {
        String sql = "SELECT * FROM member WHERE member_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Member> findByPhone(String phone) {
        String sql = "SELECT * FROM member WHERE phone=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByPhone failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Member> findAll() {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM member ORDER BY created_at DESC";
        try (Connection conn = getConn(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Member> findByStatus(String status) {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE status=?";
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
    public boolean existsByPhone(String phone) {
        String sql = "SELECT COUNT(*) FROM member WHERE phone=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("existsByPhone failed: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM member WHERE email=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("existsByEmail failed: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void resetPassword(int memberId, String hashedPassword) {
        String sql = "UPDATE member SET password=?, failed_attempts=0, is_locked=0 WHERE member_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("resetPassword failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void setArchivedAt(int memberId) {
        String sql = "UPDATE member SET archived_at=NOW() WHERE member_id=? AND archived_at IS NULL";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("setArchivedAt failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Integer> findArchivedMemberIdsBefore(java.time.LocalDateTime cutoff) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT member_id FROM member WHERE status='ARCHIVED' " +
                "AND archived_at IS NOT NULL AND archived_at < ?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(cutoff));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("member_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findArchivedMemberIdsBefore failed: " + e.getMessage(), e);
        }
        return ids;
    }

    @Override
    public void anonymize(int memberId) {
        String sql = "UPDATE member SET full_name='[DELETED]', phone=CONCAT('DEL_', member_id), " +
                "email=NULL, date_of_birth='1900-01-01', " +
                "emergency_contact_name=NULL, emergency_contact_phone=NULL, " +
                "weight=NULL, height=NULL, bmi_value=NULL, bmi_category=NULL " +
                "WHERE member_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("anonymize failed: " + e.getMessage(), e);
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setMemberId(rs.getInt("member_id"));
        m.setFullName(rs.getString("full_name"));
        // NULL-GUARD: anonymized members have '1900-01-01' but newly inserted ones may be null
        Date dob = rs.getDate("date_of_birth");
        m.setDateOfBirth(dob != null ? dob.toLocalDate() : null);
        m.setGender(rs.getString("gender"));
        m.setPhone(rs.getString("phone"));
        m.setEmail(rs.getString("email"));
        m.setPassword(rs.getString("password"));
        m.setWeight(rs.getObject("weight", Double.class));
        m.setHeight(rs.getObject("height", Double.class));
        m.setBmiValue(rs.getObject("bmi_value", Double.class));
        m.setBmiCategory(rs.getString("bmi_category"));
        Timestamp bmiTs = rs.getTimestamp("bmi_updated_at");
        if (bmiTs != null) m.setBmiUpdatedAt(bmiTs.toLocalDateTime());
        m.setEmergencyContactName(rs.getString("emergency_contact_name"));
        m.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        m.setStatus(rs.getString("status"));
        m.setFailedAttempts(rs.getInt("failed_attempts"));
        m.setLocked(rs.getBoolean("is_locked"));
        Timestamp archived = rs.getTimestamp("archived_at");
        if (archived != null) m.setArchivedAt(archived.toLocalDateTime());
        // NULL-GUARD: created_at should always exist but guard defensively
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) m.setCreatedAt(created.toLocalDateTime());
        return m;
    }
}