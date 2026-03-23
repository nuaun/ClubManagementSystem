package com.iscms.ui;

import com.iscms.model.*;
import com.iscms.service.AuthService;
import com.iscms.service.PTService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TrainerManagementPanel extends JPanel {

    private final PTService ptService = new PTService();

    private JTable trainerTable;
    private DefaultTableModel trainerModel;

    public TrainerManagementPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Trainers",       buildTrainersPanel());
        tabs.addTab("Add Trainer",    buildAddTrainerPanel());
        tabs.addTab("Appointments",   buildAppointmentsPanel());
        tabs.addTab("Leave Requests", buildLeaveRequestsPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ── TRAINERS ─────────────────────────────────────────────────
    private JPanel buildTrainersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnToggle  = new JButton("Activate / Deactivate");
        JButton btnEdit    = new JButton("Edit Trainer");
        JButton btnUnlock  = new JButton("Unlock Account");

        btnToggle.setBackground(new Color(200, 130, 0));
        btnToggle.setForeground(Color.WHITE);
        btnToggle.setOpaque(true);
        btnToggle.setBorderPainted(false);

        btnEdit.setBackground(new Color(33, 87, 141));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setOpaque(true);
        btnEdit.setBorderPainted(false);

        btnUnlock.setBackground(new Color(0, 130, 130));
        btnUnlock.setForeground(Color.WHITE);
        btnUnlock.setOpaque(true);
        btnUnlock.setBorderPainted(false);

        toolbar.add(btnRefresh);
        toolbar.add(btnToggle);
        toolbar.add(btnEdit);
        toolbar.add(btnUnlock);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Username", "Specialty", "Active", "Locked"};
        trainerModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        trainerTable = new JTable(trainerModel);
        trainerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(trainerTable), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadTrainers());
        btnToggle.addActionListener(e -> toggleTrainer());
        btnEdit.addActionListener(e -> editTrainer());
        btnUnlock.addActionListener(e -> unlockTrainer());

        loadTrainers();
        return panel;
    }

    private void loadTrainers() {
        trainerModel.setRowCount(0);
        for (Trainer t : ptService.getAllTrainers()) {
            trainerModel.addRow(new Object[]{
                    t.getTrainerId(), t.getFullName(), t.getUsername(),
                    t.getSpecialty(),
                    t.isActive() ? "Active" : "Inactive",
                    t.isLocked() ? "LOCKED" : "OK"
            });
        }
    }

    private void unlockTrainer() {
        int row = trainerTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a trainer."); return; }
        int trainerId = (int) trainerModel.getValueAt(row, 0);
        String name   = (String) trainerModel.getValueAt(row, 1);
        String locked = (String) trainerModel.getValueAt(row, 5);
        if (!"LOCKED".equals(locked)) {
            JOptionPane.showMessageDialog(this, "This trainer account is not locked.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Unlock account of trainer '" + name + "'?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ptService.unlockTrainer(trainerId);
            loadTrainers();
            JOptionPane.showMessageDialog(this, "Trainer account unlocked.");
        }
    }

    private void toggleTrainer() {
        int row = trainerTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a trainer."); return; }
        int trainerId  = (int) trainerModel.getValueAt(row, 0);
        String current = (String) trainerModel.getValueAt(row, 4);
        boolean newStatus = "Inactive".equals(current);
        ptService.setTrainerActive(trainerId, newStatus);
        loadTrainers();
    }

    private void editTrainer() {
        int row = trainerTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a trainer."); return; }

        int trainerId    = (int) trainerModel.getValueAt(row, 0);
        String fullName  = (String) trainerModel.getValueAt(row, 1);
        String username  = (String) trainerModel.getValueAt(row, 2);
        String specialty = (String) trainerModel.getValueAt(row, 3);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Trainer", true);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill   = GridBagConstraints.HORIZONTAL;

        JTextField     txtName      = new JTextField(fullName);
        JTextField     txtUsername  = new JTextField(username != null ? username : "");
        JTextField     txtSpecialty = new JTextField(specialty != null ? specialty : "");
        JPasswordField txtPass      = new JPasswordField(20);

        txtName.setEditable(false);
        txtName.setBackground(new Color(240, 240, 240));

        String[] labels = {"Full Name", "Username", "New Password", "Specialty"};
        Component[] inputs = {txtName, txtUsername, txtPass, txtSpecialty};

        for (int i = 0; i < inputs.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0.3;
            panel.add(new JLabel(labels[i] + ":"), c);
            c.gridx = 1; c.weightx = 0.7;
            panel.add(inputs[i], c);
        }

        JButton btnSave = new JButton("Save");
        btnSave.setBackground(new Color(33, 87, 141));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);

        JButton btnWorkingDays = new JButton("Set Working Days & Slots");
        btnWorkingDays.setBackground(new Color(33, 120, 80));
        btnWorkingDays.setForeground(Color.WHITE);
        btnWorkingDays.setOpaque(true);
        btnWorkingDays.setBorderPainted(false);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        btnPanel.add(btnSave);
        btnPanel.add(btnWorkingDays);

        c.gridx = 0; c.gridy = inputs.length; c.gridwidth = 2;
        panel.add(btnPanel, c);

        dialog.add(panel);

        btnSave.addActionListener(e -> {
            String newUsername  = txtUsername.getText().trim();
            String newSpecialty = txtSpecialty.getText().trim();
            String newPass      = new String(txtPass.getPassword());

            if (newUsername.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username cannot be empty.");
                return;
            }
            try {
                String passToSet = newPass.isEmpty() ? null : newPass;
                ptService.updateTrainerProfile(trainerId, newUsername, newSpecialty, passToSet);
                JOptionPane.showMessageDialog(dialog, "Trainer updated successfully!");
                dialog.dispose();
                loadTrainers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnWorkingDays.addActionListener(ev -> {
            dialog.dispose();
            showWorkingDaysDialog(trainerId, fullName);
        });

        dialog.setVisible(true);
    }

    // ── WORKING DAYS DIALOG ───────────────────────────────────────
    private void showWorkingDaysDialog(int trainerId, String trainerName) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Working Days — " + trainerName, true);
        dialog.setSize(520, 440);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        JCheckBox[] checks      = new JCheckBox[days.length];
        JTextField[] startTimes = new JTextField[days.length];
        JTextField[] endTimes   = new JTextField[days.length];

        List<TrainerWorkingDay> existing = ptService.getWorkingDays(trainerId);

        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; grid.add(new JLabel("Day"), c);
        c.gridx = 1; grid.add(new JLabel("Start (HH:MM)"), c);
        c.gridx = 2; grid.add(new JLabel("End (HH:MM)"), c);

        for (int i = 0; i < days.length; i++) {
            final int idx = i;
            checks[i]     = new JCheckBox(days[i]);
            startTimes[i] = new JTextField("09:00", 6);
            endTimes[i]   = new JTextField("18:00", 6);

            for (TrainerWorkingDay wd : existing) {
                if (wd.getDayOfWeek().equals(days[i])) {
                    checks[i].setSelected(true);
                    startTimes[i].setText(wd.getStartTime().toString());
                    endTimes[i].setText(wd.getEndTime().toString());
                }
            }

            startTimes[i].setEnabled(checks[i].isSelected());
            endTimes[i].setEnabled(checks[i].isSelected());
            checks[i].addActionListener(e -> {
                startTimes[idx].setEnabled(checks[idx].isSelected());
                endTimes[idx].setEnabled(checks[idx].isSelected());
            });

            c.gridx = 0; c.gridy = i + 1; grid.add(checks[i], c);
            c.gridx = 1; grid.add(startTimes[i], c);
            c.gridx = 2; grid.add(endTimes[i], c);
        }

        panel.add(new JScrollPane(grid), BorderLayout.CENTER);

        JButton btnSave = new JButton("Save Working Days");
        btnSave.setBackground(new Color(33, 87, 141));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);

        JButton btnSlots = new JButton("Set Lesson Slots");
        btnSlots.setBackground(new Color(80, 50, 120));
        btnSlots.setForeground(Color.WHITE);
        btnSlots.setOpaque(true);
        btnSlots.setBorderPainted(false);

        JPanel southPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        southPanel.add(btnSave);
        southPanel.add(btnSlots);
        panel.add(southPanel, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> {
            try {
                List<TrainerWorkingDay> list = new ArrayList<>();
                for (int i = 0; i < days.length; i++) {
                    if (checks[i].isSelected()) {
                        TrainerWorkingDay wd = new TrainerWorkingDay();
                        wd.setTrainerId(trainerId);
                        wd.setDayOfWeek(days[i]);
                        wd.setStartTime(LocalTime.parse(startTimes[i].getText().trim()));
                        wd.setEndTime(LocalTime.parse(endTimes[i].getText().trim()));
                        list.add(wd);
                    }
                }
                ptService.saveWorkingDays(trainerId, list);
                JOptionPane.showMessageDialog(dialog, "Working days saved!");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnSlots.addActionListener(e -> {
            dialog.dispose();
            showLessonSlotsDialog(trainerId, trainerName);
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    // ── LESSON SLOTS DIALOG ───────────────────────────────────────
    private void showLessonSlotsDialog(int trainerId, String trainerName) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Lesson Slots — " + trainerName, true);
        dialog.setSize(580, 520);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        List<TrainerWorkingDay> wds = ptService.getWorkingDays(trainerId);

        if (wds.isEmpty()) {
            JLabel lbl = new JLabel("Please set working days first!", SwingConstants.CENTER);
            lbl.setForeground(Color.RED);
            lbl.setFont(new Font("Arial", Font.BOLD, 13));
            panel.add(lbl, BorderLayout.CENTER);
            dialog.add(panel);
            dialog.setVisible(true);
            return;
        }

        // ── Existing slots table ──
        String[] cols = {"ID", "Day", "Start", "End"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);

        Runnable loadSlots = () -> {
            model.setRowCount(0);
            for (TrainerLessonSlot s : ptService.getLessonSlots(trainerId)) {
                model.addRow(new Object[]{
                        s.getSlotId(), s.getDayOfWeek(), s.getStartTime(), s.getEndTime()
                });
            }
        };
        loadSlots.run();

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Current Lesson Slots"));
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.setPreferredSize(new Dimension(0, 250));
        panel.add(tablePanel, BorderLayout.CENTER);

        // ── Add slot form ──
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Slot"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Day combo — only working days
        JComboBox<String> cbDay = new JComboBox<>();
        for (TrainerWorkingDay wd : wds) {
            cbDay.addItem(wd.getDayOfWeek()
                    + "  (" + wd.getStartTime() + " - " + wd.getEndTime() + ")");
        }

        JTextField txtStart = new JTextField("09:30", 8);
        JTextField txtEnd   = new JTextField("10:30", 8);

        JButton btnAdd    = new JButton("Add Slot");
        JButton btnDelete = new JButton("Delete Selected");

        btnAdd.setBackground(new Color(33, 120, 80));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setOpaque(true);
        btnAdd.setBorderPainted(false);

        btnDelete.setBackground(new Color(150, 50, 50));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setOpaque(true);
        btnDelete.setBorderPainted(false);

        c.gridx = 0; c.gridy = 0; c.weightx = 0.2;
        formPanel.add(new JLabel("Day:"), c);
        c.gridx = 1; c.weightx = 0.5;
        formPanel.add(cbDay, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0.2;
        formPanel.add(new JLabel("Start (HH:MM):"), c);
        c.gridx = 1; c.weightx = 0.3;
        formPanel.add(txtStart, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0.2;
        formPanel.add(new JLabel("End (HH:MM):"), c);
        c.gridx = 1; c.weightx = 0.3;
        formPanel.add(txtEnd, c);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.add(btnAdd);
        btnRow.add(btnDelete);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        formPanel.add(btnRow, c);

        panel.add(formPanel, BorderLayout.SOUTH);

        // ── Listeners ──
        btnAdd.addActionListener(e -> {
            try {
                String dayVal   = wds.get(cbDay.getSelectedIndex()).getDayOfWeek();
                LocalTime start = LocalTime.parse(txtStart.getText().trim());
                LocalTime end   = LocalTime.parse(txtEnd.getText().trim());

                if (!end.isAfter(start)) {
                    JOptionPane.showMessageDialog(dialog, "End time must be after start time.");
                    return;
                }

                // Working hours check
                TrainerWorkingDay wd = wds.get(cbDay.getSelectedIndex());
                if (start.isBefore(wd.getStartTime()) || end.isAfter(wd.getEndTime())) {
                    JOptionPane.showMessageDialog(dialog,
                            "Slot must be within working hours: "
                                    + wd.getStartTime() + " - " + wd.getEndTime());
                    return;
                }

                TrainerLessonSlot slot = new TrainerLessonSlot();
                slot.setTrainerId(trainerId);
                slot.setDayOfWeek(dayVal);
                slot.setStartTime(start);
                slot.setEndTime(end);
                ptService.addLessonSlot(slot);
                loadSlots.run();
                JOptionPane.showMessageDialog(dialog, "Slot added!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a slot.");
                return;
            }
            int slotId = (int) model.getValueAt(row, 0);
            ptService.deleteLessonSlot(slotId);
            loadSlots.run();
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    // ── ADD TRAINER ───────────────────────────────────────────────
    private JPanel buildAddTrainerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill   = GridBagConstraints.HORIZONTAL;

        JTextField     txtName      = new JTextField(20);
        JTextField     txtUsername  = new JTextField(20);
        JPasswordField txtPassword  = new JPasswordField(20);
        JTextField     txtSpecialty = new JTextField(20);

        String[] labels = {"Full Name *", "Username *", "Password *", "Specialty"};
        Component[] inputs = {txtName, txtUsername, txtPassword, txtSpecialty};

        for (int i = 0; i < inputs.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0.3;
            panel.add(new JLabel(labels[i] + ":"), c);
            c.gridx = 1; c.weightx = 0.7;
            panel.add(inputs[i], c);
        }

        JButton btnSave = new JButton("Add Trainer");
        btnSave.setBackground(new Color(33, 87, 141));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        c.gridx = 0; c.gridy = inputs.length; c.gridwidth = 2;
        panel.add(btnSave, c);

        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword());

            if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Name, username and password are required.");
                return;
            }
            try {
                Trainer t = new Trainer();
                t.setFullName(name);
                t.setUsername(user);
                t.setPassword(AuthService.hashPassword(pass));
                t.setSpecialty(txtSpecialty.getText().trim());
                t.setActive(true);
                ptService.addTrainer(t);
                JOptionPane.showMessageDialog(panel, "Trainer added successfully!");
                txtName.setText(""); txtUsername.setText("");
                txtPassword.setText(""); txtSpecialty.setText("");
                loadTrainers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // ── APPOINTMENTS ──────────────────────────────────────────────
    private JPanel buildAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"ID", "Member ID", "Trainer ID", "Date", "Start", "End", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh  = new JButton("Refresh");
        JButton btnComplete = new JButton("Mark Completed");
        JButton btnNoShow   = new JButton("Mark No-Show");

        btnComplete.setBackground(new Color(50, 150, 50));
        btnComplete.setForeground(Color.WHITE);
        btnComplete.setOpaque(true);
        btnComplete.setBorderPainted(false);

        btnNoShow.setBackground(new Color(150, 50, 50));
        btnNoShow.setForeground(Color.WHITE);
        btnNoShow.setOpaque(true);
        btnNoShow.setBorderPainted(false);

        toolbar.add(btnRefresh);
        toolbar.add(btnComplete);
        toolbar.add(btnNoShow);
        panel.add(toolbar, BorderLayout.NORTH);

        Runnable loadApts = () -> {
            model.setRowCount(0);
            for (Trainer t : ptService.getAllTrainers()) {
                for (PersonalTrainingAppointment apt : ptService.getTrainerAppointments(t.getTrainerId())) {
                    model.addRow(new Object[]{
                            apt.getAppointmentId(), apt.getMemberId(), apt.getTrainerId(),
                            apt.getAppointmentDate(), apt.getStartTime(),
                            apt.getEndTime(), apt.getStatus()
                    });
                }
            }
        };

        loadApts.run();
        btnRefresh.addActionListener(e -> loadApts.run());
        btnComplete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel, "Please select an appointment."); return; }
            ptService.markCompleted((int) model.getValueAt(row, 0));
            loadApts.run();
        });
        btnNoShow.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel, "Please select an appointment."); return; }
            ptService.markNoShow((int) model.getValueAt(row, 0));
            JOptionPane.showMessageDialog(panel, "No-show recorded. 7-day penalty applied.");
            loadApts.run();
        });

        return panel;
    }

    // ── LEAVE REQUESTS ────────────────────────────────────────────
    private JPanel buildLeaveRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"ID", "Trainer", "Date", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnApprove = new JButton("Approve");
        JButton btnReject  = new JButton("Reject");

        btnApprove.setBackground(new Color(50, 150, 50));
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setOpaque(true);
        btnApprove.setBorderPainted(false);

        btnReject.setBackground(new Color(150, 50, 50));
        btnReject.setForeground(Color.WHITE);
        btnReject.setOpaque(true);
        btnReject.setBorderPainted(false);

        toolbar.add(btnRefresh);
        toolbar.add(btnApprove);
        toolbar.add(btnReject);
        panel.add(toolbar, BorderLayout.NORTH);

        Runnable loadLeaves = () -> {
            model.setRowCount(0);
            for (TrainerLeaveRequest req : ptService.getPendingLeaves()) {
                String trainerName = ptService.getAllTrainers().stream()
                        .filter(t -> t.getTrainerId() == req.getTrainerId())
                        .map(Trainer::getFullName)
                        .findFirst().orElse("Unknown");
                model.addRow(new Object[]{
                        req.getRequestId(), trainerName, req.getLeaveDate(),
                        req.getReason(), req.getStatus()
                });
            }
        };

        loadLeaves.run();
        btnRefresh.addActionListener(e -> loadLeaves.run());
        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel, "Please select a request."); return; }
            ptService.approveLeave((int) model.getValueAt(row, 0));
            JOptionPane.showMessageDialog(panel, "Leave approved.");
            loadLeaves.run();
        });
        btnReject.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel, "Please select a request."); return; }
            ptService.rejectLeave((int) model.getValueAt(row, 0));
            JOptionPane.showMessageDialog(panel, "Leave rejected.");
            loadLeaves.run();
        });

        return panel;
    }
}