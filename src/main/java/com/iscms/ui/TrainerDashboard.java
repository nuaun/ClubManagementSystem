package com.iscms.ui;

import com.iscms.model.*;
import com.iscms.service.MemberService;
import com.iscms.service.PTService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class TrainerDashboard extends JFrame {

    private final Trainer trainer;
    private final PTService ptService         = new PTService();
    private final MemberService memberService = new MemberService();

    private DefaultTableModel aptModel;

    public TrainerDashboard(Trainer trainer) {
        this.trainer = trainer;
        setTitle("Trainer Panel — " + trainer.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(80, 50, 120));
        topBar.setPreferredSize(new Dimension(0, 50));

        JLabel lblTitle = new JLabel("  ISC-MS  |  Trainer: " + trainer.getFullName()
                + "  (" + (trainer.getSpecialty() != null ? trainer.getSpecialty() : "") + ")");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        topBar.add(lblTitle, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(180, 50, 50));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setOpaque(true);
        btnLogout.setBorderPainted(false);
        btnLogout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(btnLogout);
        topBar.add(rightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Appointments", buildAppointmentsPanel());
        tabs.addTab("My Profile",      buildProfilePanel());
        tabs.addTab("Leave Requests",  buildLeavePanel());
        tabs.addTab("My Schedule",     buildSchedulePanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

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

        String[] cols = {"ID", "Member Name", "Date", "Start", "End", "Status"};
        aptModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(aptModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadAppointments());
        btnComplete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel, "Please select an appointment."); return; }
            ptService.markCompleted((int) aptModel.getValueAt(row, 0));
            loadAppointments();
        });
        btnNoShow.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel, "Please select an appointment."); return; }
            ptService.markNoShow((int) aptModel.getValueAt(row, 0));
            JOptionPane.showMessageDialog(panel, "No-show recorded. 7-day penalty applied to member.");
            loadAppointments();
        });

        loadAppointments();
        return panel;
    }

    private void loadAppointments() {
        aptModel.setRowCount(0);
        List<PersonalTrainingAppointment> apts = ptService.getTrainerAppointments(trainer.getTrainerId());
        for (PersonalTrainingAppointment apt : apts) {
            String memberName = memberService.getMemberById(apt.getMemberId())
                    .map(Member::getFullName).orElse("Unknown");
            aptModel.addRow(new Object[]{
                    apt.getAppointmentId(), memberName,
                    apt.getAppointmentDate(), apt.getStartTime(),
                    apt.getEndTime(), apt.getStatus()
            });
        }
    }

    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill   = GridBagConstraints.HORIZONTAL;

        JTextField txtName = new JTextField(trainer.getFullName());
        txtName.setEditable(false);
        txtName.setBackground(new Color(240, 240, 240));

        JTextField txtUser = new JTextField(trainer.getUsername() != null ? trainer.getUsername() : "");
        txtUser.setEditable(false);
        txtUser.setBackground(new Color(240, 240, 240));

        JPasswordField txtPass    = new JPasswordField(20);
        JPasswordField txtConfirm = new JPasswordField(20);

        String[] labels = {"Full Name (read-only)", "Username (read-only)",
                "New Password", "Confirm Password"};
        Component[] inputs = {txtName, txtUser, txtPass, txtConfirm};

        for (int i = 0; i < inputs.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0.3;
            panel.add(new JLabel(labels[i] + ":"), c);
            c.gridx = 1; c.weightx = 0.7;
            panel.add(inputs[i], c);
        }

        JButton btnSave = new JButton("Save Password");
        btnSave.setBackground(new Color(80, 50, 120));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        c.gridx = 0; c.gridy = inputs.length; c.gridwidth = 2;
        panel.add(btnSave, c);

        btnSave.addActionListener(e -> {
            String pass    = new String(txtPass.getPassword());
            String confirm = new String(txtConfirm.getPassword());
            if (pass.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter a new password.");
                return;
            }
            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(panel, "Passwords do not match.");
                return;
            }
            ptService.updateTrainerProfile(trainer.getTrainerId(),
                    trainer.getUsername(), trainer.getSpecialty(), pass);
            JOptionPane.showMessageDialog(panel, "Password updated successfully!");
            txtPass.setText("");
            txtConfirm.setText("");
        });

        return panel;
    }

    private JPanel buildLeavePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"ID", "Start Date", "End Date", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JTextField txtStartDate = new JTextField(10);
        txtStartDate.setToolTipText("YYYY-MM-DD");
        JTextField txtEndDate   = new JTextField(10);
        txtEndDate.setToolTipText("YYYY-MM-DD");
        JTextField txtReason    = new JTextField(20);
        JButton btnSubmit       = new JButton("Submit Leave Request");
        btnSubmit.setBackground(new Color(80, 50, 120));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setOpaque(true);
        btnSubmit.setBorderPainted(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Start (YYYY-MM-DD):"));
        row1.add(txtStartDate);
        row1.add(new JLabel("End (YYYY-MM-DD):"));
        row1.add(txtEndDate);
        row1.add(new JLabel("Reason:"));
        row1.add(txtReason);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(btnSubmit);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(row1);
        form.add(row2);
        panel.add(form, BorderLayout.NORTH);

        Runnable loadLeaves = () -> {
            model.setRowCount(0);
            for (TrainerLeaveRequest req : ptService.getLeavesByTrainer(trainer.getTrainerId())) {
                model.addRow(new Object[]{
                        req.getRequestId(),
                        req.getLeaveStart() != null ? req.getLeaveStart() : req.getLeaveDate(),
                        req.getLeaveEnd()   != null ? req.getLeaveEnd()   : req.getLeaveDate(),
                        req.getReason(),
                        req.getStatus()
                });
            }
        };
        loadLeaves.run();

        btnSubmit.addActionListener(e -> {
            String startStr = txtStartDate.getText().trim();
            String endStr   = txtEndDate.getText().trim();
            String reason   = txtReason.getText().trim();
            if (startStr.isEmpty() || endStr.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter both start and end dates.");
                return;
            }
            try {
                LocalDate start = LocalDate.parse(startStr);
                LocalDate end   = LocalDate.parse(endStr);
                if (end.isBefore(start)) {
                    JOptionPane.showMessageDialog(panel, "End date cannot be before start date.");
                    return;
                }
                TrainerLeaveRequest req = new TrainerLeaveRequest();
                req.setTrainerId(trainer.getTrainerId());
                req.setLeaveDate(start);
                req.setLeaveStart(start);
                req.setLeaveEnd(end);
                req.setReason(reason);
                ptService.submitLeaveRequest(req);
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                JOptionPane.showMessageDialog(panel,
                        "Leave request submitted! (" + start + " → " + end + ", " + days + " day(s))");
                txtStartDate.setText("");
                txtEndDate.setText("");
                txtReason.setText("");
                loadLeaves.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel buildSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] wdCols = {"Day", "Working Start", "Working End"};
        DefaultTableModel wdModel = new DefaultTableModel(wdCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        List<TrainerWorkingDay> wds = ptService.getWorkingDays(trainer.getTrainerId());
        for (TrainerWorkingDay wd : wds) {
            wdModel.addRow(new Object[]{wd.getDayOfWeek(), wd.getStartTime(), wd.getEndTime()});
        }

        String[] slotCols = {"Day", "Lesson Start", "Lesson End"};
        DefaultTableModel slotModel = new DefaultTableModel(slotCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        List<TrainerLessonSlot> slots = ptService.getLessonSlots(trainer.getTrainerId());
        for (TrainerLessonSlot s : slots) {
            slotModel.addRow(new Object[]{s.getDayOfWeek(), s.getStartTime(), s.getEndTime()});
        }

        JTable wdTable   = new JTable(wdModel);
        JTable slotTable = new JTable(slotModel);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(wdTable), new JScrollPane(slotTable));
        split.setDividerLocation(150);
        split.setBorder(null);

        panel.add(new JLabel("  Working Days", SwingConstants.LEFT), BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        if (wds.isEmpty()) {
            JLabel lbl = new JLabel("No working days defined yet. Please contact your manager.",
                    SwingConstants.CENTER);
            lbl.setForeground(Color.GRAY);
            panel.add(lbl, BorderLayout.NORTH);
        }

        return panel;
    }
}