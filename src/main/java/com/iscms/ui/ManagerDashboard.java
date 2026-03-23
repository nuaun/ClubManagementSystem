package com.iscms.ui;

import com.iscms.model.*;
import com.iscms.service.AuthService;
import com.iscms.service.MemberService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class ManagerDashboard extends JFrame {

    private final Manager manager;
    private final MemberService memberService = new MemberService();

    private JTable memberTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    public ManagerDashboard(Manager manager) {
        this.manager = manager;
        setTitle("Manager Panel — " + manager.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        initUI();
        loadMembers();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(33, 87, 141));
        topBar.setPreferredSize(new Dimension(0, 50));

        JLabel lblTitle = new JLabel("  ISC-MS  |  Manager: " + manager.getFullName());
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
        tabs.addTab("Members",       buildMembersPanel());
        tabs.addTab("Add Member",    buildAddMemberPanel());
        tabs.addTab("Requests",      buildRequestsPanel());
        tabs.addTab("Events",        new EventManagementPanel(manager));
        tabs.addTab("Trainers & PT", new TrainerManagementPanel());
        tabs.addTab("My Profile",    buildProfilePanel());
        tabs.addTab("Reports", new ReportsPanel());
        add(tabs, BorderLayout.CENTER);

        // BR-28/29: Run auto-archive and anonymize on startup
        SwingUtilities.invokeLater(() -> {
            int archived = memberService.archivePassiveMembers();
            int anonymized = memberService.anonymizeArchivedMembers();
            if (archived > 0) {
                JOptionPane.showMessageDialog(this,
                        archived + " passive member(s) have been automatically archived (BR-28).",
                        "Auto-Archive", JOptionPane.INFORMATION_MESSAGE);
            }
            if (anonymized > 0) {
                JOptionPane.showMessageDialog(this,
                        anonymized + " member(s) personal data has been anonymized per KVKK (BR-29).",
                        "KVKK Compliance", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    // ── MEMBERS ───────────────────────────────────────────────────
    private JPanel buildMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(20);
        JButton btnSearch  = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");
        searchPanel.add(new JLabel("Search (name/phone):"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Full Name", "Phone", "Tier", "Package", "Status", "End Date", "Locked"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        memberTable = new JTable(tableModel);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(memberTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnDetail   = new JButton("View Detail");
        JButton btnActivate = new JButton("Activate");
        JButton btnSuspend  = new JButton("Suspend");
        JButton btnArchive  = new JButton("Archive");
        JButton btnUnlock   = new JButton("Unlock");

        btnActivate.setBackground(new Color(50, 150, 50));   btnActivate.setForeground(Color.WHITE); btnActivate.setOpaque(true); btnActivate.setBorderPainted(false);
        btnSuspend.setBackground(new Color(200, 130, 0));    btnSuspend.setForeground(Color.WHITE);  btnSuspend.setOpaque(true);  btnSuspend.setBorderPainted(false);
        btnArchive.setBackground(new Color(150, 50, 50));    btnArchive.setForeground(Color.WHITE);  btnArchive.setOpaque(true);  btnArchive.setBorderPainted(false);
        btnUnlock.setBackground(new Color(0, 130, 130));     btnUnlock.setForeground(Color.WHITE);   btnUnlock.setOpaque(true);   btnUnlock.setBorderPainted(false);

        actionPanel.add(btnDetail); actionPanel.add(btnActivate); actionPanel.add(btnSuspend);
        actionPanel.add(btnArchive); actionPanel.add(btnUnlock);
        panel.add(actionPanel, BorderLayout.SOUTH);

        JButton btnArchiveCheck = new JButton("Run Archive Check");
        btnArchiveCheck.setBackground(new Color(100, 100, 100));
        btnArchiveCheck.setForeground(Color.WHITE);
        btnArchiveCheck.setOpaque(true);
        btnArchiveCheck.setBorderPainted(false);
        actionPanel.add(btnArchiveCheck);

        btnArchiveCheck.addActionListener(e -> {
            int archived   = memberService.archivePassiveMembers();
            int anonymized = memberService.anonymizeArchivedMembers();
            JOptionPane.showMessageDialog(this,
                    "Archive check complete.\n" +
                            "Archived: " + archived + " member(s)\n" +
                            "Anonymized (KVKK): " + anonymized + " member(s)");
            loadMembers();
        });


        btnRefresh.addActionListener(e -> loadMembers());
        btnSearch.addActionListener(e -> searchMembers());
        btnActivate.addActionListener(e -> changeStatus("ACTIVE"));
        btnSuspend.addActionListener(e -> changeStatus("SUSPENDED"));
        btnArchive.addActionListener(e -> changeStatus("ARCHIVED"));
        btnDetail.addActionListener(e -> showMemberDetail());
        btnUnlock.addActionListener(e -> unlockMember());

        return panel;
    }

    void loadMembers() {
        tableModel.setRowCount(0);
        for (Member m : memberService.getAllMembers()) {
            Optional<Membership> ms = memberService.getActiveMembership(m.getMemberId());
            tableModel.addRow(new Object[]{
                    m.getMemberId(), m.getFullName(), m.getPhone(),
                    ms.map(Membership::getTier).orElse("-"),
                    ms.map(Membership::getPackageType).orElse("-"),
                    m.getStatus(),
                    ms.map(x -> x.getEndDate().toString()).orElse("-"),
                    m.isLocked() ? "LOCKED" : "OK"
            });
        }
    }

    private void searchMembers() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        memberService.getAllMembers().stream()
                .filter(m -> m.getFullName().toLowerCase().contains(keyword)
                        || m.getPhone().contains(keyword))
                .forEach(m -> {
                    Optional<Membership> ms = memberService.getActiveMembership(m.getMemberId());
                    tableModel.addRow(new Object[]{
                            m.getMemberId(), m.getFullName(), m.getPhone(),
                            ms.map(Membership::getTier).orElse("-"),
                            ms.map(Membership::getPackageType).orElse("-"),
                            m.getStatus(),
                            ms.map(x -> x.getEndDate().toString()).orElse("-"),
                            m.isLocked() ? "LOCKED" : "OK"
                    });
                });
    }

    private void changeStatus(String newStatus) {
        int row = memberTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a member first."); return; }
        int memberId = (int) tableModel.getValueAt(row, 0);
        String name  = (String) tableModel.getValueAt(row, 1);
        int confirm  = JOptionPane.showConfirmDialog(this,
                "Set status of '" + name + "' to " + newStatus + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) { memberService.updateMemberStatus(memberId, newStatus); loadMembers(); }
    }

    private void unlockMember() {
        int row = memberTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a member first."); return; }
        int memberId = (int) tableModel.getValueAt(row, 0);
        String name  = (String) tableModel.getValueAt(row, 1);
        Member m = memberService.getMemberById(memberId).orElse(null);
        if (m == null) return;
        if (!m.isLocked()) { JOptionPane.showMessageDialog(this, "This account is not locked."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Unlock account of '" + name + "'?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            memberService.unlockMember(memberId);
            loadMembers();
            JOptionPane.showMessageDialog(this, "Account unlocked successfully.");
        }
    }

    private void showMemberDetail() {
        int row = memberTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a member first."); return; }
        int memberId = (int) tableModel.getValueAt(row, 0);
        memberService.getMemberById(memberId).ifPresent(m -> {
            String info = String.format(
                    "ID: %d%nName: %s%nPhone: %s%nEmail: %s%nDOB: %s%nGender: %s%n" +
                            "Weight: %s kg  Height: %s cm%nBMI: %s (%s)%n" +
                            "Emergency: %s — %s%nStatus: %s%nLocked: %s%nFailed Login Attempts: %d",
                    m.getMemberId(), m.getFullName(), m.getPhone(), m.getEmail(),
                    m.getDateOfBirth(), m.getGender(), m.getWeight(), m.getHeight(),
                    m.getBmiValue(), m.getBmiCategory(),
                    m.getEmergencyContactName(), m.getEmergencyContactPhone(),
                    m.getStatus(),
                    m.isLocked() ? "YES — account locked" : "No",
                    m.getFailedAttempts());
            JOptionPane.showMessageDialog(this, info, "Member Detail", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // ── ADD MEMBER ────────────────────────────────────────────────
    private JPanel buildAddMemberPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill   = GridBagConstraints.HORIZONTAL;

        JTextField     txtName    = new JTextField(20);
        JTextField     txtDob     = new JTextField(20);
        JComboBox<String> cbGender    = new JComboBox<>(new String[]{"MALE","FEMALE","OTHER"});
        JTextField     txtPhone   = new JTextField(20);
        JTextField     txtEmail   = new JTextField(20);
        JPasswordField txtPass    = new JPasswordField(20);
        JTextField     txtWeight  = new JTextField(20);
        JTextField     txtHeight  = new JTextField(20);
        JTextField     txtEcName  = new JTextField(20);
        JTextField     txtEcPhone = new JTextField(20);
        JComboBox<String> cbTier      = new JComboBox<>(new String[]{"CLASSIC","GOLD","VIP"});
        JComboBox<String> cbPackage   = new JComboBox<>(new String[]{"MONTHLY","ANNUAL_INSTALLMENT","ANNUAL_PREPAID"});

        String[] labels = {
                "Full Name *","Date of Birth * (YYYY-MM-DD)","Gender *",
                "Phone * (10 digits)","Email","Password *",
                "Weight (kg)","Height (cm)","Emergency Contact Name",
                "Emergency Contact Phone","Tier *","Package *"
        };
        Component[] inputs = {
                txtName, txtDob, cbGender, txtPhone, txtEmail, txtPass,
                txtWeight, txtHeight, txtEcName, txtEcPhone, cbTier, cbPackage
        };

        for (int i = 0; i < inputs.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0.3;
            panel.add(new JLabel(labels[i] + ":"), c);
            c.gridx = 1; c.weightx = 0.7;
            panel.add(inputs[i], c);
        }

        JButton btnSave = new JButton("Add Member");
        btnSave.setBackground(new Color(33, 87, 141));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        c.gridx = 0; c.gridy = inputs.length; c.gridwidth = 2;
        panel.add(btnSave, c);

        btnSave.addActionListener(e -> {
            try {
                String selectedTier = (String) cbTier.getSelectedItem();

                // Builder pattern
                Member m = new com.iscms.model.MemberBuilder()
                        .fullName(txtName.getText().trim())
                        .dateOfBirth(java.time.LocalDate.parse(txtDob.getText().trim()))
                        .gender((String) cbGender.getSelectedItem())
                        .phone(txtPhone.getText().trim())
                        .email(txtEmail.getText().trim())
                        .password(new String(txtPass.getPassword()))
                        .status("ACTIVE")
                        .build();

                if (!txtWeight.getText().isBlank()) m.setWeight(Double.parseDouble(txtWeight.getText().trim()));
                if (!txtHeight.getText().isBlank()) m.setHeight(Double.parseDouble(txtHeight.getText().trim()));
                m.setEmergencyContactName(txtEcName.getText().trim());
                m.setEmergencyContactPhone(txtEcPhone.getText().trim());

                memberService.registerMember(m, selectedTier, (String) cbPackage.getSelectedItem(), manager.getManagerId());

                JOptionPane.showMessageDialog(panel, "Member added successfully!");
                loadMembers();
                txtName.setText(""); txtDob.setText(""); txtPhone.setText("");
                txtEmail.setText(""); txtPass.setText(""); txtWeight.setText("");
                txtHeight.setText(""); txtEcName.setText(""); txtEcPhone.setText("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // ── REQUESTS ──────────────────────────────────────────────────
    private JPanel buildRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane subTabs = new JTabbedPane();
        subTabs.addTab("Registrations", buildRegistrationRequestsPanel());
        subTabs.addTab("Tier Upgrades", buildTierUpgradeRequestsPanel());
        panel.add(subTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRegistrationRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"Req ID","Member ID","Name","Tier","Package","Amount","Expires","Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnApprove = new JButton("Approve (Payment Received)");
        JButton btnFail    = new JButton("Fail (No Payment)");

        btnApprove.setBackground(new Color(50,150,50)); btnApprove.setForeground(Color.WHITE); btnApprove.setOpaque(true); btnApprove.setBorderPainted(false);
        btnFail.setBackground(new Color(150,50,50));    btnFail.setForeground(Color.WHITE);    btnFail.setOpaque(true);    btnFail.setBorderPainted(false);

        toolbar.add(btnRefresh); toolbar.add(btnApprove); toolbar.add(btnFail);
        panel.add(toolbar, BorderLayout.NORTH);

        Runnable load = () -> {
            memberService.expireOldRequests();
            model.setRowCount(0);
            for (RegistrationRequest req : memberService.getPendingRegistrations()) {
                memberService.getMemberById(req.getMemberId()).ifPresent(m -> model.addRow(new Object[]{
                        req.getRequestId(), req.getMemberId(), m.getFullName(),
                        req.getTier(), req.getPackageType(),
                        String.format("%.2f TL", req.getAmount()),
                        req.getExpiresAt().toLocalDate(), req.getStatus()
                }));
            }
        };
        load.run();

        btnRefresh.addActionListener(e -> load.run());
        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel,"Please select a request."); return; }
            try {
                memberService.approveRegistration((int)model.getValueAt(row,0), manager.getManagerId());
                JOptionPane.showMessageDialog(panel,"Registration approved! Member is now ACTIVE.");
                load.run(); loadMembers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        });
        btnFail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel,"Please select a request."); return; }
            memberService.failRegistration((int)model.getValueAt(row,0));
            JOptionPane.showMessageDialog(panel,"Registration marked as failed.");
            load.run();
        });

        return panel;
    }

    private JPanel buildTierUpgradeRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"Req ID","Member ID","Name","From","To","Fee","Expires","Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnApprove = new JButton("Approve (Payment Received)");
        JButton btnFail    = new JButton("Fail (No Payment)");

        btnApprove.setBackground(new Color(50,150,50)); btnApprove.setForeground(Color.WHITE); btnApprove.setOpaque(true); btnApprove.setBorderPainted(false);
        btnFail.setBackground(new Color(150,50,50));    btnFail.setForeground(Color.WHITE);    btnFail.setOpaque(true);    btnFail.setBorderPainted(false);

        toolbar.add(btnRefresh); toolbar.add(btnApprove); toolbar.add(btnFail);
        panel.add(toolbar, BorderLayout.NORTH);

        Runnable load = () -> {
            memberService.expireOldRequests();
            model.setRowCount(0);
            for (TierUpgradeRequest req : memberService.getPendingTierUpgrades()) {
                memberService.getMemberById(req.getMemberId()).ifPresent(m -> model.addRow(new Object[]{
                        req.getRequestId(), req.getMemberId(), m.getFullName(),
                        req.getCurrentTier(), req.getRequestedTier(),
                        String.format("%.2f TL", req.getUpgradeFee()),
                        req.getExpiresAt().toLocalDate(), req.getStatus()
                }));
            }
        };
        load.run();

        btnRefresh.addActionListener(e -> load.run());
        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel,"Please select a request."); return; }
            try {
                memberService.approveTierUpgrade((int)model.getValueAt(row,0), manager.getManagerId());
                JOptionPane.showMessageDialog(panel,"Tier upgrade approved!");
                load.run(); loadMembers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        });
        btnFail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel,"Please select a request."); return; }
            memberService.failTierUpgrade((int)model.getValueAt(row,0));
            JOptionPane.showMessageDialog(panel,"Tier upgrade request failed.");
            load.run();
        });

        return panel;
    }

    // ── PROFILE ───────────────────────────────────────────────────
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill   = GridBagConstraints.HORIZONTAL;

        // Read-only
        JTextField txtName  = new JTextField(manager.getFullName());
        txtName.setEditable(false);
        txtName.setBackground(new Color(240, 240, 240));

        JTextField txtEmail = new JTextField(manager.getEmail());
        txtEmail.setEditable(false);
        txtEmail.setBackground(new Color(240, 240, 240));

        JTextField txtUser  = new JTextField(manager.getUsername());
        txtUser.setEditable(false);
        txtUser.setBackground(new Color(240, 240, 240));

        JPasswordField txtPass    = new JPasswordField(20);
        JPasswordField txtConfirm = new JPasswordField(20);

        String[] labels = {"Full Name (read-only)", "Username (read-only)",
                "Email (read-only)", "New Password", "Confirm Password"};
        Component[] inputs = {txtName, txtUser, txtEmail, txtPass, txtConfirm};

        for (int i = 0; i < inputs.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0.3;
            panel.add(new JLabel(labels[i] + ":"), c);
            c.gridx = 1; c.weightx = 0.7;
            panel.add(inputs[i], c);
        }

        JButton btnSave = new JButton("Save Password");
        btnSave.setBackground(new Color(33, 87, 141));
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
            try {
                new AuthService().resetManagerPassword(manager.getManagerId(), pass);
                JOptionPane.showMessageDialog(panel, "Password updated successfully!");
                txtPass.setText("");
                txtConfirm.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }
}
