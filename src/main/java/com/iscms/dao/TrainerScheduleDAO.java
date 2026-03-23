package com.iscms.dao;

import com.iscms.model.TrainerLeaveRequest;
import com.iscms.model.TrainerWorkingDay;
import java.time.LocalDate;
import java.util.List;
import com.iscms.model.TrainerLessonSlot;

public interface TrainerScheduleDAO {
    // Working days
    void saveWorkingDay(TrainerWorkingDay day);
    void deleteWorkingDays(int trainerId);
    List<TrainerWorkingDay> findWorkingDays(int trainerId);
    boolean isWorkingDay(int trainerId, String dayOfWeek);
    java.util.Optional<TrainerWorkingDay> findWorkingDay(int trainerId, String dayOfWeek); // BUG-5 FIX

    // Leave requests
    void insertLeaveRequest(TrainerLeaveRequest req);
    void updateLeaveStatus(int requestId, String status);
    List<TrainerLeaveRequest> findLeavesByTrainer(int trainerId);
    List<TrainerLeaveRequest> findPendingLeaves();
    boolean isOnLeave(int trainerId, LocalDate date);
    void saveLessonSlot(TrainerLessonSlot slot);
    void deleteLessonSlots(int trainerId);
    void deleteLessonSlot(int slotId);
    List<TrainerLessonSlot> findLessonSlots(int trainerId);
    List<TrainerLessonSlot> findLessonSlotsByDay(int trainerId, String dayOfWeek);
}