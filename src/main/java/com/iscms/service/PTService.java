package com.iscms.service;

import com.iscms.dao.AppointmentDAOImpl;
import com.iscms.dao.TrainerDAOImpl;
import com.iscms.model.PersonalTrainingAppointment;
import com.iscms.model.Trainer;
import com.iscms.dao.TrainerScheduleDAOImpl;
import com.iscms.model.TrainerLeaveRequest;
import com.iscms.model.TrainerWorkingDay;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class PTService {

    private final AppointmentDAOImpl     appointmentDAO;
    private final TrainerDAOImpl         trainerDAO;
    private final TrainerScheduleDAOImpl scheduleDAO;

    /** Production constructor. */
    public PTService() {
        this.appointmentDAO = new AppointmentDAOImpl();
        this.trainerDAO     = new TrainerDAOImpl();
        this.scheduleDAO    = new TrainerScheduleDAOImpl();
    }

    /** Test constructor — public so test package can access. */
    public PTService(AppointmentDAOImpl appointmentDAO, TrainerDAOImpl trainerDAO,
                     TrainerScheduleDAOImpl scheduleDAO) {
        this.appointmentDAO = appointmentDAO;
        this.trainerDAO     = trainerDAO;
        this.scheduleDAO    = scheduleDAO;
    }

    public void bookAppointment(int memberId, String tier, int trainerId,
                                LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot book appointments in the past.");
        }

        if ("CLASSIC".equals(tier)) {
            throw new IllegalStateException("PT sessions are available for Gold and VIP members only (BR-30).");
        }

        Optional<LocalDate> penalty = appointmentDAO.findActiveNoShowPenalty(memberId);
        if (penalty.isPresent()) {
            throw new IllegalStateException(
                    "You have a no-show penalty until " + penalty.get() + ". Cannot book new appointments (BR-34).");
        }

        if ("GOLD".equals(tier)) {
            int monthly = appointmentDAO.countMonthlyAppointments(memberId, date.getYear(), date.getMonthValue());
            if (monthly >= 2) {
                throw new IllegalStateException("Gold members can book max 2 PT sessions per month (BR-31).");
            }
        } else if ("VIP".equals(tier)) {
            int monthly = appointmentDAO.countMonthlyAppointments(memberId, date.getYear(), date.getMonthValue());
            if (monthly >= 4) {
                throw new IllegalStateException("VIP members can book max 4 PT sessions per month (BR-31).");
            }
        }

        if (appointmentDAO.hasMemberConflict(memberId, date, startTime, endTime)) {
            throw new IllegalStateException(
                    "You already have an appointment at this time on " + date + ".");
        }

        if (!isAvailable(trainerId, date, startTime, endTime)) {
            throw new IllegalStateException(
                    "Trainer is not available at this time. " +
                            "Please check their working hours and existing bookings (BR-32).");
        }

        PersonalTrainingAppointment apt = new PersonalTrainingAppointment();
        apt.setMemberId(memberId);
        apt.setTrainerId(trainerId);
        apt.setAppointmentDate(date);
        apt.setStartTime(startTime);
        apt.setEndTime(endTime);
        apt.setStatus("SCHEDULED");
        appointmentDAO.insert(apt);
    }

    public void cancelAppointment(int appointmentId, String tier) {
        PersonalTrainingAppointment apt = appointmentDAO.findById(appointmentId).orElseThrow();
        LocalDateTime deadline = apt.getAppointmentDate().atTime(apt.getStartTime()).minusHours(24);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new IllegalStateException("Cannot cancel within 24 hours of the appointment (BR-33).");
        }
        appointmentDAO.updateStatus(appointmentId, "CANCELLED");
    }

    public void markNoShow(int appointmentId) {
        PersonalTrainingAppointment apt = appointmentDAO.findById(appointmentId).orElseThrow();
        appointmentDAO.updateStatus(appointmentId, "NO_SHOW");
        appointmentDAO.updateNoShowPenalty(appointmentId, LocalDate.now().plusDays(7));
    }

    public void markCompleted(int appointmentId) {
        appointmentDAO.updateStatus(appointmentId, "COMPLETED");
    }

    public List<Trainer> getActiveTrainers()              { return trainerDAO.findActive(); }
    public List<Trainer> getAllTrainers()                  { return trainerDAO.findAll(); }

    public List<PersonalTrainingAppointment> getMemberAppointments(int memberId) {
        return appointmentDAO.findByMemberId(memberId);
    }

    public List<PersonalTrainingAppointment> getTrainerAppointments(int trainerId) {
        return appointmentDAO.findByTrainerId(trainerId);
    }

    public void addTrainer(Trainer trainer) {
        trainerDAO.insert(trainer);
    }

    public void setTrainerActive(int trainerId, boolean isActive) {
        trainerDAO.updateActiveStatus(trainerId, isActive);
    }

    public void unlockTrainer(int trainerId) {
        trainerDAO.updateLockStatus(trainerId, false);
        trainerDAO.updateFailedAttempts(trainerId, 0);
    }

    public void updateTrainerProfile(int trainerId, String username, String specialty, String plainPassword) {
        String hashed = (plainPassword == null || plainPassword.isEmpty())
                ? null
                : org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt(12));
        trainerDAO.updateProfile(trainerId, username, specialty, hashed);
    }

    public void saveWorkingDays(int trainerId, List<TrainerWorkingDay> days) {
        scheduleDAO.deleteWorkingDays(trainerId);
        for (TrainerWorkingDay d : days) {
            d.setTrainerId(trainerId);
            scheduleDAO.saveWorkingDay(d);
        }
    }

    public List<TrainerWorkingDay> getWorkingDays(int trainerId) {
        return scheduleDAO.findWorkingDays(trainerId);
    }

    public void submitLeaveRequest(TrainerLeaveRequest req) {
        scheduleDAO.insertLeaveRequest(req);
    }

    public List<TrainerLeaveRequest> getLeavesByTrainer(int trainerId) {
        return scheduleDAO.findLeavesByTrainer(trainerId);
    }

    public List<TrainerLeaveRequest> getPendingLeaves() {
        return scheduleDAO.findPendingLeaves();
    }

    public void approveLeave(int requestId) {
        scheduleDAO.updateLeaveStatus(requestId, "APPROVED");
    }

    public void rejectLeave(int requestId) {
        scheduleDAO.updateLeaveStatus(requestId, "REJECTED");
    }

    public void saveLessonSlots(int trainerId, List<com.iscms.model.TrainerLessonSlot> slots) {
        scheduleDAO.deleteLessonSlots(trainerId);
        for (com.iscms.model.TrainerLessonSlot s : slots) {
            s.setTrainerId(trainerId);
            scheduleDAO.saveLessonSlot(s);
        }
    }

    public void addLessonSlot(com.iscms.model.TrainerLessonSlot slot) {
        scheduleDAO.saveLessonSlot(slot);
    }

    public void deleteLessonSlot(int slotId) {
        scheduleDAO.deleteLessonSlot(slotId);
    }

    public List<com.iscms.model.TrainerLessonSlot> getLessonSlots(int trainerId) {
        return scheduleDAO.findLessonSlots(trainerId);
    }

    public List<com.iscms.model.TrainerLessonSlot> getLessonSlotsByDay(int trainerId, String dayOfWeek) {
        return scheduleDAO.findLessonSlotsByDay(trainerId, dayOfWeek);
    }

    public boolean isSlotTaken(int trainerId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return appointmentDAO.isSlotTaken(trainerId, date, startTime, endTime);
    }

    public boolean isAvailable(int trainerId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String dayOfWeek = date.getDayOfWeek().name();
        if (!scheduleDAO.isWorkingDay(trainerId, dayOfWeek)) return false;
        if (scheduleDAO.isOnLeave(trainerId, date)) return false;

        Optional<TrainerWorkingDay> wdOpt = scheduleDAO.findWorkingDay(trainerId, dayOfWeek);
        if (wdOpt.isPresent()) {
            TrainerWorkingDay wd = wdOpt.get();
            if (startTime.isBefore(wd.getStartTime()) || endTime.isAfter(wd.getEndTime())) {
                return false;
            }
        }

        if (appointmentDAO.isSlotTaken(trainerId, date, startTime, endTime)) return false;
        return true;
    }
}