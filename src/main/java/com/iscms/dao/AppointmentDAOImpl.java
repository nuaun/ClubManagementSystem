package com.iscms.dao;

import com.iscms.model.PersonalTrainingAppointment;
import com.iscms.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentDAOImpl implements AppointmentDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void insert(PersonalTrainingAppointment apt) {
        String sql = "INSERT INTO personal_training_appointment " +
                "(member_id, trainer_id, appointment_date, start_time, end_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apt.getMemberId());
            ps.setInt(2, apt.getTrainerId());
            ps.setDate(3, Date.valueOf(apt.getAppointmentDate()));
            ps.setTime(4, Time.valueOf(apt.getStartTime()));
            ps.setTime(5, Time.valueOf(apt.getEndTime()));
            ps.setString(6, apt.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insert appointment failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStatus(int appointmentId, String status) {
        String sql = "UPDATE personal_training_appointment SET status=? WHERE appointment_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateStatus failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateNoShowPenalty(int appointmentId, LocalDate penaltyUntil) {
        String sql = "UPDATE personal_training_appointment SET no_show_penalty_until=? WHERE appointment_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(penaltyUntil));
            ps.setInt(2, appointmentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateNoShowPenalty failed: " + e.getMessage(), e);
        }
    }

    // FIX-1: Sadece start_time eşleşmesi yetmez — overlap (çakışma) kontrolü gerekir.
    // Örnek: 10:00-11:00 randevusu varsa, 10:30-11:30 de çakışır ama eski kodda geçerdi.
    // Yeni sorgu: yeni randevunun [startTime, endTime) aralığı mevcut randevuyla kesişiyor mu?
    @Override
    public boolean isSlotTaken(int trainerId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String sql = "SELECT COUNT(*) FROM personal_training_appointment " +
                "WHERE trainer_id=? AND appointment_date=? AND status='SCHEDULED' " +
                "AND start_time < ? AND end_time > ?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(endTime));   // mevcut start < yeni end
            ps.setTime(4, Time.valueOf(startTime)); // mevcut end   > yeni start
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("isSlotTaken failed: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public int countMonthlyAppointments(int memberId, int year, int month) {
        String sql = "SELECT COUNT(*) FROM personal_training_appointment " +
                "WHERE member_id=? AND YEAR(appointment_date)=? AND MONTH(appointment_date)=? " +
                "AND status IN ('SCHEDULED','COMPLETED')";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("countMonthlyAppointments failed: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countWeeklyAppointments(int memberId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        String sql = "SELECT COUNT(*) FROM personal_training_appointment " +
                "WHERE member_id=? AND appointment_date BETWEEN ? AND ? " +
                "AND status IN ('SCHEDULED','COMPLETED')";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setDate(2, Date.valueOf(weekStart));
            ps.setDate(3, Date.valueOf(weekEnd));
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("countWeeklyAppointments failed: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public Optional<PersonalTrainingAppointment> findById(int appointmentId) {
        String sql = "SELECT * FROM personal_training_appointment WHERE appointment_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<PersonalTrainingAppointment> findByMemberId(int memberId) {
        List<PersonalTrainingAppointment> list = new ArrayList<>();
        String sql = "SELECT * FROM personal_training_appointment WHERE member_id=? ORDER BY appointment_date DESC";
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
    public List<PersonalTrainingAppointment> findByTrainerId(int trainerId) {
        List<PersonalTrainingAppointment> list = new ArrayList<>();
        String sql = "SELECT * FROM personal_training_appointment WHERE trainer_id=? ORDER BY appointment_date DESC";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByTrainerId failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<LocalDate> findActiveNoShowPenalty(int memberId) {
        String sql = "SELECT MAX(no_show_penalty_until) FROM personal_training_appointment " +
                "WHERE member_id=? AND no_show_penalty_until >= CURDATE()";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Date d = rs.getDate(1);
                if (d != null) return Optional.of(d.toLocalDate());
            }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findActiveNoShowPenalty failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean hasMemberConflict(int memberId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String sql = "SELECT COUNT(*) FROM personal_training_appointment " +
                "WHERE member_id=? AND appointment_date=? AND status='SCHEDULED' " +
                "AND start_time < ? AND end_time > ?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(endTime));
            ps.setTime(4, Time.valueOf(startTime));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("hasMemberConflict failed: " + e.getMessage(), e);
        }
    }

    private PersonalTrainingAppointment mapRow(ResultSet rs) throws SQLException {
        PersonalTrainingAppointment apt = new PersonalTrainingAppointment();
        apt.setAppointmentId(rs.getInt("appointment_id"));
        apt.setMemberId(rs.getInt("member_id"));
        apt.setTrainerId(rs.getInt("trainer_id"));
        apt.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        apt.setStartTime(rs.getTime("start_time").toLocalTime());
        apt.setEndTime(rs.getTime("end_time").toLocalTime());
        apt.setStatus(rs.getString("status"));
        Date penalty = rs.getDate("no_show_penalty_until");
        if (penalty != null) apt.setNoShowPenaltyUntil(penalty.toLocalDate());
        apt.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return apt;
    }
}