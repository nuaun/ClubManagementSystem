package com.iscms;

import com.iscms.dao.AppointmentDAOImpl;
import com.iscms.dao.TrainerDAOImpl;
import com.iscms.dao.TrainerScheduleDAOImpl;
import com.iscms.model.PersonalTrainingAppointment;
import com.iscms.model.TrainerWorkingDay;
import com.iscms.service.PTService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PTServiceMockTest {

    @Mock private AppointmentDAOImpl     appointmentDAO;
    @Mock private TrainerDAOImpl         trainerDAO;
    @Mock private TrainerScheduleDAOImpl scheduleDAO;

    private PTService ptService;

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(5);
    private static final LocalTime START       = LocalTime.of(10, 0);
    private static final LocalTime END         = LocalTime.of(11, 0);
    private static final int       TRAINER_ID  = 1;
    private static final int       MEMBER_ID   = 42;

    @BeforeEach
    void setUp() {
        ptService = new PTService(appointmentDAO, trainerDAO, scheduleDAO);
    }

    // ── BR-30: CLASSIC cannot book ───────────────────────────────

    @Test
    void bookAppointment_classic_throwsIllegalState() {
        assertThrows(IllegalStateException.class, () ->
                ptService.bookAppointment(MEMBER_ID, "CLASSIC", TRAINER_ID,
                        FUTURE_DATE, START, END));
        verify(appointmentDAO, never()).insert(any());
    }

    // ── Past date ────────────────────────────────────────────────

    @Test
    void bookAppointment_pastDate_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                ptService.bookAppointment(MEMBER_ID, "GOLD", TRAINER_ID,
                        LocalDate.now().minusDays(1), START, END));
        verify(appointmentDAO, never()).insert(any());
    }

    // ── BR-34: no-show penalty blocks booking ────────────────────

    @Test
    void bookAppointment_noShowPenaltyActive_throwsIllegalState() {
        when(appointmentDAO.findActiveNoShowPenalty(MEMBER_ID))
                .thenReturn(Optional.of(LocalDate.now().plusDays(4)));

        assertThrows(IllegalStateException.class, () ->
                ptService.bookAppointment(MEMBER_ID, "GOLD", TRAINER_ID,
                        FUTURE_DATE, START, END));
        verify(appointmentDAO, never()).insert(any());
    }

    // ── BR-31: GOLD max 2/month ──────────────────────────────────

    @Test
    void bookAppointment_gold_thirdSession_throwsIllegalState() {
        when(appointmentDAO.findActiveNoShowPenalty(MEMBER_ID)).thenReturn(Optional.empty());
        when(appointmentDAO.countMonthlyAppointments(MEMBER_ID,
                FUTURE_DATE.getYear(), FUTURE_DATE.getMonthValue())).thenReturn(2);

        assertThrows(IllegalStateException.class, () ->
                ptService.bookAppointment(MEMBER_ID, "GOLD", TRAINER_ID,
                        FUTURE_DATE, START, END));
        verify(appointmentDAO, never()).insert(any());
    }

    // ── BR-31: VIP max 4/month ───────────────────────────────────

    @Test
    void bookAppointment_vip_fifthSession_throwsIllegalState() {
        when(appointmentDAO.findActiveNoShowPenalty(MEMBER_ID)).thenReturn(Optional.empty());
        when(appointmentDAO.countMonthlyAppointments(MEMBER_ID,
                FUTURE_DATE.getYear(), FUTURE_DATE.getMonthValue())).thenReturn(4);

        assertThrows(IllegalStateException.class, () ->
                ptService.bookAppointment(MEMBER_ID, "VIP", TRAINER_ID,
                        FUTURE_DATE, START, END));
        verify(appointmentDAO, never()).insert(any());
    }

    // ── BR-32: member conflict ───────────────────────────────────

    @Test
    void bookAppointment_memberConflict_throwsIllegalState() {
        when(appointmentDAO.findActiveNoShowPenalty(MEMBER_ID)).thenReturn(Optional.empty());
        when(appointmentDAO.countMonthlyAppointments(anyInt(), anyInt(), anyInt())).thenReturn(0);
        when(appointmentDAO.hasMemberConflict(MEMBER_ID, FUTURE_DATE, START, END)).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                ptService.bookAppointment(MEMBER_ID, "GOLD", TRAINER_ID,
                        FUTURE_DATE, START, END));
        verify(appointmentDAO, never()).insert(any());
    }

    // ── Trainer not available ────────────────────────────────────

    @Test
    void bookAppointment_trainerUnavailable_throwsIllegalState() {
        when(appointmentDAO.findActiveNoShowPenalty(MEMBER_ID)).thenReturn(Optional.empty());
        when(appointmentDAO.countMonthlyAppointments(anyInt(), anyInt(), anyInt())).thenReturn(0);
        when(appointmentDAO.hasMemberConflict(anyInt(), any(), any(), any())).thenReturn(false);
        when(scheduleDAO.isWorkingDay(TRAINER_ID, FUTURE_DATE.getDayOfWeek().name())).thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
                ptService.bookAppointment(MEMBER_ID, "GOLD", TRAINER_ID,
                        FUTURE_DATE, START, END));
        verify(appointmentDAO, never()).insert(any());
    }

    // ── Happy path ───────────────────────────────────────────────

    @Test
    void bookAppointment_valid_gold_insertsAppointment() {
        when(appointmentDAO.findActiveNoShowPenalty(MEMBER_ID)).thenReturn(Optional.empty());
        when(appointmentDAO.countMonthlyAppointments(MEMBER_ID,
                FUTURE_DATE.getYear(), FUTURE_DATE.getMonthValue())).thenReturn(1);
        when(appointmentDAO.hasMemberConflict(anyInt(), any(), any(), any())).thenReturn(false);
        when(scheduleDAO.isWorkingDay(TRAINER_ID, FUTURE_DATE.getDayOfWeek().name())).thenReturn(true);
        when(scheduleDAO.isOnLeave(TRAINER_ID, FUTURE_DATE)).thenReturn(false);
        when(scheduleDAO.findWorkingDay(TRAINER_ID, FUTURE_DATE.getDayOfWeek().name()))
                .thenReturn(Optional.of(workingDay(LocalTime.of(8, 0), LocalTime.of(20, 0))));
        when(appointmentDAO.isSlotTaken(TRAINER_ID, FUTURE_DATE, START, END)).thenReturn(false);

        ptService.bookAppointment(MEMBER_ID, "GOLD", TRAINER_ID, FUTURE_DATE, START, END);

        verify(appointmentDAO).insert(any(PersonalTrainingAppointment.class));
    }

    // ── BR-33: cancel within 24h blocked ────────────────────────

    @Test
    void cancelAppointment_within24h_throwsIllegalState() {
        PersonalTrainingAppointment apt = new PersonalTrainingAppointment();
        apt.setAppointmentDate(LocalDate.now());
        apt.setStartTime(LocalTime.now().plusMinutes(30));
        when(appointmentDAO.findById(1)).thenReturn(Optional.of(apt));

        assertThrows(IllegalStateException.class, () ->
                ptService.cancelAppointment(1, "GOLD"));
        verify(appointmentDAO, never()).updateStatus(anyInt(), anyString());
    }

    @Test
    void cancelAppointment_moreThan24h_succeeds() {
        PersonalTrainingAppointment apt = new PersonalTrainingAppointment();
        apt.setAppointmentDate(LocalDate.now().plusDays(3));
        apt.setStartTime(LocalTime.of(10, 0));
        when(appointmentDAO.findById(1)).thenReturn(Optional.of(apt));

        ptService.cancelAppointment(1, "GOLD");

        verify(appointmentDAO).updateStatus(1, "CANCELLED");
    }

    // ── BR-34: markNoShow applies penalty ────────────────────────

    @Test
    void markNoShow_updatesStatusAndPenalty() {
        PersonalTrainingAppointment apt = new PersonalTrainingAppointment();
        apt.setAppointmentId(5);
        when(appointmentDAO.findById(5)).thenReturn(Optional.of(apt));

        ptService.markNoShow(5);

        verify(appointmentDAO).updateStatus(5, "NO_SHOW");
        verify(appointmentDAO).updateNoShowPenalty(eq(5), any(LocalDate.class));
    }

    // ── isAvailable: trainer on leave ────────────────────────────

    @Test
    void isAvailable_trainerOnLeave_returnsFalse() {
        when(scheduleDAO.isWorkingDay(TRAINER_ID, FUTURE_DATE.getDayOfWeek().name())).thenReturn(true);
        when(scheduleDAO.isOnLeave(TRAINER_ID, FUTURE_DATE)).thenReturn(true);

        assertFalse(ptService.isAvailable(TRAINER_ID, FUTURE_DATE, START, END));
    }

    @Test
    void isAvailable_outsideWorkingHours_returnsFalse() {
        when(scheduleDAO.isWorkingDay(TRAINER_ID, FUTURE_DATE.getDayOfWeek().name())).thenReturn(true);
        when(scheduleDAO.isOnLeave(TRAINER_ID, FUTURE_DATE)).thenReturn(false);
        when(scheduleDAO.findWorkingDay(TRAINER_ID, FUTURE_DATE.getDayOfWeek().name()))
                .thenReturn(Optional.of(workingDay(LocalTime.of(12, 0), LocalTime.of(18, 0))));

        assertFalse(ptService.isAvailable(TRAINER_ID, FUTURE_DATE, START, END));
    }

    // ── Helper ───────────────────────────────────────────────────

    private TrainerWorkingDay workingDay(LocalTime start, LocalTime end) {
        TrainerWorkingDay wd = new TrainerWorkingDay();
        wd.setTrainerId(TRAINER_ID);
        wd.setStartTime(start);
        wd.setEndTime(end);
        return wd;
    }
}