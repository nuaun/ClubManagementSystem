package com.iscms.ui;

import com.iscms.model.*;
import com.iscms.service.MemberService;
import com.iscms.service.PTService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PTPanel extends JPanel {

    private final Member member;
    private final PTService ptService         = new PTService();
    private final MemberService memberService = new MemberService();

    private DefaultTableModel aptModel;
    private LocalDate currentWeekStart;

    public PTPanel(Member member) {
        this.member = member;
        this.currentWeekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Book Appointment", buildBookPanel());
        tabs.addTab("My Appointments",  buildMyAppointmentsPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildBookPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        Optional<Membership> msOpt = memberService.getActiveMembership(member.getMemberId());
        String tier = msOpt.map(Membership::getTier).orElse("CLASSIC");

        if ("CLASSIC".equals(tier)) {
            JLabel lbl = new JLabel("PT sessions are available for Gold and VIP members only.",
                    SwingConstants.CENTER);
            lbl.setForeground(Color.RED);
            lbl.setFont(new Font("Arial", Font.BOLD, 13));
            main.add(lbl, BorderLayout.CENTER);
            return main;
        }

        List<Trainer> trainers = ptService.getActiveTrainers();
        if (trainers.isEmpty()) {
            main.add(new JLabel("No active trainers available.", SwingConstants.CENTER),
                    BorderLayout.CENTER);
            return main;
        }

        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel trainerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> cbTrainer = new JComboBox<>();
        for (Trainer t : trainers) {
            cbTrainer.addItem(t.getTrainerId() + " — " + t.getFullName()
                    + " (" + (t.getSpecialty() != null ? t.getSpecialty() : "General") + ")");
        }
        String limitInfo = "GOLD".equals(tier) ? "  Gold: max 2 sessions/month"
                : "  VIP: max 4 sessions/month";
        JLabel lblLimit = new JLabel(limitInfo);
        lblLimit.setForeground(new Color(33, 87, 141));
        lblLimit.setFont(new Font("Arial", Font.BOLD, 12));
        trainerPanel.add(new JLabel("Trainer:"));
        trainerPanel.add(cbTrainer);
        trainerPanel.add(lblLimit);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnPrev = new JButton("◀ Prev Week");
        JButton btnNext = new JButton("Next Week ▶");
        JLabel  lblWeek = new JLabel("", SwingConstants.CENTER);
        lblWeek.setFont(new Font("Arial", Font.BOLD, 13));
        navPanel.add(btnPrev);
        navPanel.add(lblWeek);
        navPanel.add(btnNext);

        topPanel.add(trainerPanel, BorderLayout.NORTH);
        topPanel.add(navPanel, BorderLayout.SOUTH);
        main.add(topPanel, BorderLayout.NORTH);

        JPanel slotGrid = new JPanel();
        slotGrid.setLayout(new BoxLayout(slotGrid, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(slotGrid);
        main.add(scroll, BorderLayout.CENTER);

        Runnable[] refreshRef = new Runnable[1];
        refreshRef[0] = () -> {
            slotGrid.removeAll();
            int trainerId = trainers.get(cbTrainer.getSelectedIndex()).getTrainerId();
            lblWeek.setText(currentWeekStart + "  —  " + currentWeekStart.plusDays(6));

            List<TrainerWorkingDay> workingDays = ptService.getWorkingDays(trainerId);

            // İzin listesini önceden al — isAvailable() yerine bunu kullan (bug fix)
            List<TrainerLeaveRequest> leaveList = ptService.getLeavesByTrainer(trainerId);

            for (int i = 0; i < 7; i++) {
                LocalDate date    = currentWeekStart.plusDays(i);
                String    dayName = date.getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();

                boolean isWorkingDay = workingDays.stream()
                        .anyMatch(w -> w.getDayOfWeek().equals(dayName));
                if (!isWorkingDay) continue;

                // FIX: isAvailable() hem izin hem dolu slot için false dönüyordu.
                // Sadece onaylı izin kaydını kontrol ediyoruz.
                final LocalDate checkDate = date;
                boolean onLeave = leaveList.stream()
                        .anyMatch(l -> "APPROVED".equals(l.getStatus())
                                && l.getLeaveStart() != null && l.getLeaveEnd() != null
                                && !checkDate.isBefore(l.getLeaveStart())
                                && !checkDate.isAfter(l.getLeaveEnd()));

                JPanel dayHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
                String workHours = workingDays.stream()
                        .filter(w -> w.getDayOfWeek().equals(dayName))
                        .map(w -> w.getStartTime() + " - " + w.getEndTime())
                        .findFirst().orElse("");
                JLabel lblDay = new JLabel("  📅 " + dayName + " — " + date + "  (" + workHours + ")");
                lblDay.setFont(new Font("Arial", Font.BOLD, 12));

                if (onLeave) {
                    lblDay.setText(lblDay.getText() + "  ⛔ TRAINER ON LEAVE");
                    lblDay.setForeground(Color.RED);
                    dayHeader.setBackground(new Color(255, 220, 220));
                } else {
                    dayHeader.setBackground(new Color(220, 235, 255));
                }
                dayHeader.add(lblDay);
                dayHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
                slotGrid.add(dayHeader);

                if (onLeave) {
                    slotGrid.add(Box.createVerticalStrut(3));
                    continue;
                }

                List<TrainerLessonSlot> slots = ptService.getLessonSlotsByDay(trainerId, dayName);
                if (slots.isEmpty()) {
                    JLabel noSlot = new JLabel("      No lesson slots defined for this day.");
                    noSlot.setForeground(Color.GRAY);
                    noSlot.setFont(new Font("Arial", Font.ITALIC, 11));
                    slotGrid.add(noSlot);
                } else {
                    JPanel slotsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
                    slotsRow.setBackground(Color.WHITE);

                    for (TrainerLessonSlot slot : slots) {
                        boolean taken = ptService.isSlotTaken(trainerId, date, slot.getStartTime(), slot.getEndTime());

                        JButton btnSlot = new JButton();
                        btnSlot.setPreferredSize(new Dimension(160, 50));
                        btnSlot.setFont(new Font("Arial", Font.BOLD, 11));

                        if (taken) {
                            btnSlot.setText("<html><center>" + slot.getStartTime()
                                    + " — " + slot.getEndTime()
                                    + "<br><font color='gray'>BOOKED</font></center></html>");
                            btnSlot.setBackground(new Color(220, 220, 220));
                            btnSlot.setEnabled(false);
                            btnSlot.setOpaque(true);
                            btnSlot.setBorderPainted(false);
                        } else {
                            btnSlot.setText("<html><center>" + slot.getStartTime()
                                    + " — " + slot.getEndTime()
                                    + "<br><font color='green'>✓ AVAILABLE</font></center></html>");
                            btnSlot.setBackground(new Color(200, 240, 200));
                            btnSlot.setOpaque(true);
                            btnSlot.setBorderPainted(false);

                            final LocalDate bookDate  = date;
                            final LocalTime bookStart = slot.getStartTime();
                            final LocalTime bookEnd   = slot.getEndTime();
                            final int       tId       = trainerId;
                            final String    tName     = trainers.get(cbTrainer.getSelectedIndex()).getFullName();

                            btnSlot.addActionListener(e -> {
                                int confirm = JOptionPane.showConfirmDialog(main,
                                        "<html><b>Confirm Booking</b><br><br>"
                                                + "Trainer: <b>" + tName + "</b><br>"
                                                + "Date: <b>" + bookDate + "</b> (" + dayName + ")<br>"
                                                + "Time: <b>" + bookStart + " — " + bookEnd + "</b></html>",
                                        "Book Appointment", JOptionPane.YES_NO_OPTION);
                                if (confirm == JOptionPane.YES_OPTION) {
                                    try {
                                        ptService.bookAppointment(
                                                member.getMemberId(), tier, tId,
                                                bookDate, bookStart, bookEnd);
                                        JOptionPane.showMessageDialog(main,
                                                "Appointment booked successfully!");
                                        refreshAppointments();
                                        refreshRef[0].run();
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(main, ex.getMessage(),
                                                "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                        }
                        slotsRow.add(btnSlot);
                    }
                    slotGrid.add(slotsRow);
                }
                slotGrid.add(Box.createVerticalStrut(5));
            }

            slotGrid.revalidate();
            slotGrid.repaint();
        };

        btnPrev.addActionListener(e -> { currentWeekStart = currentWeekStart.minusWeeks(1); refreshRef[0].run(); });
        btnNext.addActionListener(e -> { currentWeekStart = currentWeekStart.plusWeeks(1); refreshRef[0].run(); });
        cbTrainer.addActionListener(e -> refreshRef[0].run());

        refreshRef[0].run();
        return main;
    }

    private JPanel buildMyAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnCancel  = new JButton("Cancel Appointment");
        btnCancel.setBackground(new Color(150, 50, 50));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setOpaque(true);
        btnCancel.setBorderPainted(false);
        toolbar.add(btnRefresh);
        toolbar.add(btnCancel);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Trainer", "Date", "Start", "End", "Status"};
        aptModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(aptModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> refreshAppointments());
        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(panel, "Please select an appointment.");
                return;
            }
            int aptId = (int) aptModel.getValueAt(row, 0);
            Optional<Membership> msOpt = memberService.getActiveMembership(member.getMemberId());
            String tier = msOpt.map(Membership::getTier).orElse("CLASSIC");
            try {
                ptService.cancelAppointment(aptId, tier);
                JOptionPane.showMessageDialog(panel, "Appointment cancelled.");
                refreshAppointments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshAppointments();
        return panel;
    }

    private void refreshAppointments() {
        if (aptModel == null) return;
        aptModel.setRowCount(0);
        List<PersonalTrainingAppointment> apts = ptService.getMemberAppointments(member.getMemberId());
        List<Trainer> allTrainers = ptService.getAllTrainers();
        for (PersonalTrainingAppointment apt : apts) {
            String trainerName = allTrainers.stream()
                    .filter(t -> t.getTrainerId() == apt.getTrainerId())
                    .map(Trainer::getFullName)
                    .findFirst().orElse("Unknown");
            aptModel.addRow(new Object[]{
                    apt.getAppointmentId(), trainerName,
                    apt.getAppointmentDate(), apt.getStartTime(),
                    apt.getEndTime(), apt.getStatus()
            });
        }
    }
}