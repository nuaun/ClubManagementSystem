package com.iscms.dao;

import com.iscms.model.TrainerLeaveRequest;
import com.iscms.model.TrainerWorkingDay;
import com.iscms.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrainerScheduleDAOImpl implements TrainerScheduleDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void saveWorkingDay(TrainerWorkingDay day) {
        String sql = "INSERT INTO trainer_working_days (trainer_id, day_of_week, start_time, end_time) " +
                "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE start_time=?, end_time=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            Time start = Time.valueOf(day.getStartTime());
            Time end   = Time.valueOf(day.getEndTime());
            ps.setInt(1, day.getTrainerId());
            ps.setString(2, day.getDayOfWeek());
            ps.setTime(3, start);
            ps.setTime(4, end);
            ps.setTime(5, start);
            ps.setTime(6, end);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("saveWorkingDay failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteWorkingDays(int trainerId) {
        String sql = "DELETE FROM trainer_working_days WHERE trainer_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteWorkingDays failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TrainerWorkingDay> findWorkingDays(int trainerId) {
        List<TrainerWorkingDay> list = new ArrayList<>();
        String sql = "SELECT * FROM trainer_working_days WHERE trainer_id=? ORDER BY FIELD(day_of_week," +
                "'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TrainerWorkingDay d = new TrainerWorkingDay();
                    d.setId(rs.getInt("id"));
                    d.setTrainerId(rs.getInt("trainer_id"));
                    d.setDayOfWeek(rs.getString("day_of_week"));
                    d.setStartTime(rs.getTime("start_time").toLocalTime());
                    d.setEndTime(rs.getTime("end_time").toLocalTime());
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findWorkingDays failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean isWorkingDay(int trainerId, String dayOfWeek) {
        String sql = "SELECT COUNT(*) FROM trainer_working_days WHERE trainer_id=? AND day_of_week=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            ps.setString(2, dayOfWeek);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("isWorkingDay failed: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public java.util.Optional<TrainerWorkingDay> findWorkingDay(int trainerId, String dayOfWeek) {
        String sql = "SELECT * FROM trainer_working_days WHERE trainer_id=? AND day_of_week=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            ps.setString(2, dayOfWeek);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TrainerWorkingDay d = new TrainerWorkingDay();
                    d.setId(rs.getInt("id"));
                    d.setTrainerId(rs.getInt("trainer_id"));
                    d.setDayOfWeek(rs.getString("day_of_week"));
                    d.setStartTime(rs.getTime("start_time").toLocalTime());
                    d.setEndTime(rs.getTime("end_time").toLocalTime());
                    return java.util.Optional.of(d);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findWorkingDay failed: " + e.getMessage(), e);
        }
        return java.util.Optional.empty();
    }

    @Override
    public void insertLeaveRequest(TrainerLeaveRequest req) {
        String sql = "INSERT INTO trainer_leave_request " +
                "(trainer_id, leave_date, leave_start, leave_end, reason) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            LocalDate start = req.getLeaveStart() != null ? req.getLeaveStart() : req.getLeaveDate();
            LocalDate end   = req.getLeaveEnd()   != null ? req.getLeaveEnd()   : start;
            ps.setInt(1, req.getTrainerId());
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(start));
            ps.setDate(4, Date.valueOf(end));
            ps.setString(5, req.getReason());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertLeaveRequest failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLeaveStatus(int requestId, String status) {
        String sql = "UPDATE trainer_leave_request SET status=? WHERE request_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateLeaveStatus failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TrainerLeaveRequest> findLeavesByTrainer(int trainerId) {
        List<TrainerLeaveRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM trainer_leave_request WHERE trainer_id=? ORDER BY leave_date DESC";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLeave(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findLeavesByTrainer failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TrainerLeaveRequest> findPendingLeaves() {
        List<TrainerLeaveRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM trainer_leave_request WHERE status='PENDING' ORDER BY created_at";
        try (Connection conn = getConn(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapLeave(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findPendingLeaves failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean isOnLeave(int trainerId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM trainer_leave_request " +
                "WHERE trainer_id=? AND status='APPROVED' " +
                "AND leave_start <= ? AND leave_end >= ?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            ps.setDate(2, Date.valueOf(date));
            ps.setDate(3, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("isOnLeave failed: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void saveLessonSlot(com.iscms.model.TrainerLessonSlot slot) {
        String sql = "INSERT INTO trainer_lesson_slots (trainer_id, day_of_week, start_time, end_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slot.getTrainerId());
            ps.setString(2, slot.getDayOfWeek());
            ps.setTime(3, Time.valueOf(slot.getStartTime()));
            ps.setTime(4, Time.valueOf(slot.getEndTime()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("saveLessonSlot failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteLessonSlots(int trainerId) {
        String sql = "DELETE FROM trainer_lesson_slots WHERE trainer_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteLessonSlots failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteLessonSlot(int slotId) {
        String sql = "DELETE FROM trainer_lesson_slots WHERE slot_id=?";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteLessonSlot failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<com.iscms.model.TrainerLessonSlot> findLessonSlots(int trainerId) {
        List<com.iscms.model.TrainerLessonSlot> list = new ArrayList<>();
        String sql = "SELECT * FROM trainer_lesson_slots WHERE trainer_id=? ORDER BY FIELD(day_of_week," +
                "'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'), start_time";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSlot(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findLessonSlots failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<com.iscms.model.TrainerLessonSlot> findLessonSlotsByDay(int trainerId, String dayOfWeek) {
        List<com.iscms.model.TrainerLessonSlot> list = new ArrayList<>();
        String sql = "SELECT * FROM trainer_lesson_slots WHERE trainer_id=? AND day_of_week=? ORDER BY start_time";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            ps.setString(2, dayOfWeek);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSlot(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findLessonSlotsByDay failed: " + e.getMessage(), e);
        }
        return list;
    }

    private com.iscms.model.TrainerLessonSlot mapSlot(ResultSet rs) throws SQLException {
        com.iscms.model.TrainerLessonSlot s = new com.iscms.model.TrainerLessonSlot();
        s.setSlotId(rs.getInt("slot_id"));
        s.setTrainerId(rs.getInt("trainer_id"));
        s.setDayOfWeek(rs.getString("day_of_week"));
        s.setStartTime(rs.getTime("start_time").toLocalTime());
        s.setEndTime(rs.getTime("end_time").toLocalTime());
        return s;
    }

    private TrainerLeaveRequest mapLeave(ResultSet rs) throws SQLException {
        TrainerLeaveRequest r = new TrainerLeaveRequest();
        r.setRequestId(rs.getInt("request_id"));
        r.setTrainerId(rs.getInt("trainer_id"));
        r.setLeaveDate(rs.getDate("leave_date").toLocalDate());
        Date ls = rs.getDate("leave_start");
        Date le = rs.getDate("leave_end");
        r.setLeaveStart(ls != null ? ls.toLocalDate() : r.getLeaveDate());
        r.setLeaveEnd(le != null ? le.toLocalDate() : r.getLeaveDate());
        r.setReason(rs.getString("reason"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return r;
    }
}