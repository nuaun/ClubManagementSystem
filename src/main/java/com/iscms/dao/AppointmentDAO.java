package com.iscms.dao;

import com.iscms.model.PersonalTrainingAppointment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentDAO {
    void insert(PersonalTrainingAppointment apt);
    void updateStatus(int appointmentId, String status);
    void updateNoShowPenalty(int appointmentId, LocalDate penaltyUntil);
    boolean isSlotTaken(int trainerId, LocalDate date, LocalTime startTime, LocalTime endTime);
    boolean hasMemberConflict(int memberId, LocalDate date, LocalTime startTime, LocalTime endTime);
    int countMonthlyAppointments(int memberId, int year, int month);
    int countWeeklyAppointments(int memberId, LocalDate weekStart);
    Optional<PersonalTrainingAppointment> findById(int appointmentId);
    List<PersonalTrainingAppointment> findByMemberId(int memberId);
    List<PersonalTrainingAppointment> findByTrainerId(int trainerId);
    Optional<LocalDate> findActiveNoShowPenalty(int memberId);
}
