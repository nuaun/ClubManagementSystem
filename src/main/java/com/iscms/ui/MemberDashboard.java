package com.iscms.ui;

import com.iscms.model.*;
import com.iscms.service.MemberService;
import com.iscms.service.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class MemberDashboard extends JFrame {

    private final Member member;
    private final MemberService memberService = new MemberService();
    private final ReportService reportService = new ReportService();

    public MemberDashboard(Member member) {
        this.member = member;
        setTitle("Member Panel — " + member.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(33, 120, 80));
        topBar.setPreferredSize(new Dimension(0, 50));

        JLabel lblTitle = new JLabel("  ISC-MS  |  Welcome, " + member.getFullName());
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
        tabs.addTab("My Profile",      buildProfilePanel());
        tabs.addTab("My Membership",   buildMembershipPanel());
        tabs.addTab("Payment History", buildPaymentPanel());
        tabs.addTab("BMI & Health",    buildBmiPanel());
        tabs.addTab("Events",          new MemberEventPanel(member));
        tabs.addTab("PT Appointments", new PTPanel(member));
        add(tabs, BorderLayout.CENTER);
    }

    // ── PROFILE ───────────────────────────────────────────────────
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill   = GridBagConstraints.HORIZONTAL;

        // Read-only
        JTextField txtName = new JTextField(member.getFullName());
        txtName.setEditable(false);
        txtName.setBackground(new Color(240, 240, 240));

        JTextField txtDob = new JTextField(member.getDateOfBirth().toString()
                + "  (Age: " + ChronoUnit.YEARS.between(member.getDateOfBirth(), LocalDate.now()) + ")");
        txtDob.setEditable(false);
        txtDob.setBackground(new Color(240, 240, 240));

        JTextField txtGender = new JTextField(member.getGender());
        txtGender.setEditable(false);
        txtGender.setBackground(new Color(240, 240, 240));

        JTextField txtStatus = new JTextField(member.getStatus());
        txtStatus.setEditable(false);
        txtStatus.setBackground(new Color(240, 240, 240));

        // Editable — telefon artık düzenlenebilir
        JTextField txtPhone   = new JTextField(member.getPhone() != null ? member.getPhone() : "");
        JTextField txtEmail   = new JTextField(member.getEmail() != null ? member.getEmail() : "");
        JTextField txtWeight  = new JTextField(member.getWeight()  != null ? String.valueOf(member.getWeight())  : "");
        JTextField txtHeight  = new JTextField(member.getHeight()  != null ? String.valueOf(member.getHeight())  : "");
        JTextField txtEcName  = new JTextField(member.getEmergencyContactName()  != null ? member.getEmergencyContactName()  : "");
        JTextField txtEcPhone = new JTextField(member.getEmergencyContactPhone() != null ? member.getEmergencyContactPhone() : "");

        String[] labels = {"Full Name", "Date of Birth", "Gender", "Phone", "Email",
                "Weight (kg)", "Height (cm)", "Emergency Contact", "Emergency Phone", "Status"};
        Component[] inputs = {txtName, txtDob, txtGender, txtPhone, txtEmail,
                txtWeight, txtHeight, txtEcName, txtEcPhone, txtStatus};

        for (int i = 0; i < inputs.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0.3;
            panel.add(new JLabel(labels[i] + ":"), c);
            c.gridx = 1; c.weightx = 0.7;
            panel.add(inputs[i], c);
        }

        JButton btnSave = new JButton("Save Changes");
        btnSave.setBackground(new Color(33, 120, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        c.gridx = 0; c.gridy = inputs.length; c.gridwidth = 2;
        panel.add(btnSave, c);

        btnSave.addActionListener(e -> {
            try {
                // Telefon — duplicate kontrolü
                String newPhone = txtPhone.getText().trim();
                if (!newPhone.equals(member.getPhone())) {
                    boolean duplicate = memberService.getAllMembers().stream()
                            .anyMatch(m -> m.getPhone().equals(newPhone)
                                    && m.getMemberId() != member.getMemberId());
                    if (duplicate) {
                        JOptionPane.showMessageDialog(panel,
                                "Bu telefon numarası başka bir üyeye ait.",
                                "Hata", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    member.setPhone(newPhone);
                }

                member.setEmail(txtEmail.getText().trim());
                String wStr = txtWeight.getText().trim();
                String hStr = txtHeight.getText().trim();
                if (!wStr.isBlank()) {
                    double w = Double.parseDouble(wStr);
                    if (member.getWeight() == null || Double.compare(w, member.getWeight()) != 0)
                        member.setWeight(w);
                }
                if (!hStr.isBlank()) {
                    double h = Double.parseDouble(hStr);
                    if (member.getHeight() == null || Double.compare(h, member.getHeight()) != 0)
                        member.setHeight(h);
                }
                member.setEmergencyContactName(txtEcName.getText().trim());
                member.setEmergencyContactPhone(txtEcPhone.getText().trim());
                memberService.updateMember(member);
                memberService.recalculateBmi(member.getMemberId());
                JOptionPane.showMessageDialog(panel, "Profile updated successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(new JScrollPane(panel), BorderLayout.CENTER);
        return wrapper;
    }

    // ── MEMBERSHIP ────────────────────────────────────────────────
    private JPanel buildMembershipPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        Optional<Membership> msOpt = memberService.getActiveMembership(member.getMemberId());
        boolean isPassive = "PASSIVE".equals(member.getStatus());

        if (msOpt.isEmpty()) {
            JPanel centerPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(10, 10, 10, 10);
            c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
            centerPanel.add(new JLabel("No active membership found.", SwingConstants.CENTER), c);

            if (isPassive) {
                JButton btnRenew = new JButton("Renew / New Membership Request");
                btnRenew.setBackground(new Color(33, 120, 80));
                btnRenew.setForeground(Color.WHITE);
                btnRenew.setOpaque(true);
                btnRenew.setBorderPainted(false);
                c.gridy = 1;
                centerPanel.add(btnRenew, c);
                btnRenew.addActionListener(e -> showRenewDialog());
            }

            panel.add(centerPanel, BorderLayout.CENTER);
            return panel;
        }

        Membership ms = msOpt.get();
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), ms.getEndDate());

        JPanel info = new JPanel(new GridLayout(0, 2, 10, 10));
        info.setBorder(BorderFactory.createTitledBorder("Membership Details"));
        info.add(new JLabel("Membership ID:")); info.add(new JLabel(String.valueOf(ms.getMembershipId())));
        info.add(new JLabel("Tier:"));          info.add(makeTierLabel(ms.getTier()));
        info.add(new JLabel("Package:"));       info.add(new JLabel(ms.getPackageType()));
        info.add(new JLabel("Start Date:"));    info.add(new JLabel(ms.getStartDate().toString()));
        info.add(new JLabel("End Date:"));      info.add(new JLabel(ms.getEndDate().toString()));
        info.add(new JLabel("Days Left:"));     info.add(new JLabel(daysLeft + " days"));
        info.add(new JLabel("Status:"));        info.add(new JLabel(ms.getStatus()));
        info.add(new JLabel("Freeze Used:"));   info.add(new JLabel(ms.getFreezeCount() + " time(s)"));
        panel.add(info, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnFreeze  = new JButton("Freeze Membership");
        btnFreeze.setBackground(new Color(33, 87, 141));
        btnFreeze.setForeground(Color.WHITE);
        btnFreeze.setOpaque(true);
        btnFreeze.setBorderPainted(false);

        JButton btnUpgrade = new JButton("Upgrade Tier");
        btnUpgrade.setBackground(new Color(150, 0, 150));
        btnUpgrade.setForeground(Color.WHITE);
        btnUpgrade.setOpaque(true);
        btnUpgrade.setBorderPainted(false);

        if ("VIP".equals(ms.getTier())) btnUpgrade.setEnabled(false);
        if ("FROZEN".equals(ms.getStatus()) || "SUSPENDED".equals(ms.getStatus())) {
            btnFreeze.setEnabled(false);
            btnUpgrade.setEnabled(false);
        }

        btnPanel.add(btnFreeze);
        btnPanel.add(btnUpgrade);

        // Renew sadece PASSIVE üyelere
        if (isPassive) {
            JButton btnRenew = new JButton("Renew Membership");
            btnRenew.setBackground(new Color(33, 120, 80));
            btnRenew.setForeground(Color.WHITE);
            btnRenew.setOpaque(true);
            btnRenew.setBorderPainted(false);
            btnPanel.add(btnRenew);
            btnRenew.addActionListener(e -> showRenewDialog());
        }

        panel.add(btnPanel, BorderLayout.CENTER);

        // Freeze
        btnFreeze.addActionListener(e -> {
            String[] options = {"7 days", "14 days", "30 days"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Select freeze duration:", "Freeze Membership",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (choice >= 0) {
                int days = new int[]{7, 14, 30}[choice];
                try {
                    memberService.freezeMembership(ms.getMembershipId(), days);
                    JOptionPane.showMessageDialog(this, "Membership frozen for " + days + " days.");
                    refreshMembershipTab();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Upgrade
        btnUpgrade.addActionListener(e -> {
            String newTier = "CLASSIC".equals(ms.getTier()) ? "GOLD" : "VIP";
            double currentDaily = switch (ms.getTier()) {
                case "CLASSIC" -> 750.0 / 30;
                case "GOLD"    -> 1250.0 / 30;
                default        -> 0;
            };
            double newDaily = switch (newTier) {
                case "GOLD" -> 1250.0 / 30;
                case "VIP"  -> 2000.0 / 30;
                default     -> 0;
            };
            double upgradeFee = (newDaily - currentDaily) * daysLeft;

            int confirm = JOptionPane.showConfirmDialog(this,
                    "<html><b>Tier Upgrade Request</b><br><br>"
                            + "Current Tier: <b>" + ms.getTier() + "</b><br>"
                            + "New Tier: <b>" + newTier + "</b><br>"
                            + "Days Remaining: <b>" + daysLeft + "</b><br>"
                            + "Upgrade Fee: <b>" + String.format("%.2f TL", upgradeFee) + "</b><br><br>"
                            + "Request will be sent to manager.<br>"
                            + "Please pay the fee within <b>3 days</b>.<br>"
                            + "Submit request?</html>",
                    "Upgrade Tier", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    memberService.createTierUpgradeRequest(
                            member.getMemberId(), ms.getMembershipId(),
                            ms.getTier(), newTier, upgradeFee);
                    JOptionPane.showMessageDialog(this,
                            "<html>Upgrade request submitted!<br>" +
                                    "Please pay <b>" + String.format("%.2f TL", upgradeFee) +
                                    "</b> to the manager within 3 days.</html>",
                            "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
                    refreshMembershipTab();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private void showRenewDialog() {
        JDialog dialog = new JDialog(this, "Renew Membership", true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill   = GridBagConstraints.HORIZONTAL;

        // Mevcut tier'ı bul — tier düşürme engeli
        List<Membership> allMs = memberService.getAllMemberships(member.getMemberId());
        String currentTier = allMs.isEmpty() ? "CLASSIC" : allMs.get(0).getTier();

        String[] tiers = switch (currentTier) {
            case "GOLD" -> new String[]{"GOLD", "VIP"};
            case "VIP"  -> new String[]{"VIP"};
            default     -> new String[]{"CLASSIC", "GOLD", "VIP"};
        };

        JComboBox<String> cbTier    = new JComboBox<>(tiers);
        JComboBox<String> cbPackage = new JComboBox<>(new String[]{"MONTHLY", "ANNUAL_INSTALLMENT", "ANNUAL_PREPAID"});
        JLabel lblAmount = new JLabel("Amount: 750.00 TL /month");
        lblAmount.setFont(new Font("Arial", Font.BOLD, 12));
        lblAmount.setForeground(new Color(33, 87, 141));

        if (!"CLASSIC".equals(currentTier)) {
            JLabel lblNote = new JLabel("Note: Cannot downgrade from " + currentTier);
            lblNote.setForeground(Color.RED);
            lblNote.setFont(new Font("Arial", Font.ITALIC, 11));
            c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
            panel.add(lblNote, c);
            c.gridwidth = 1;
            c.gridy = 1;
        } else {
            c.gridy = 0;
        }

        c.gridx = 0; c.weightx = 0.3; panel.add(new JLabel("Tier *:"), c);
        c.gridx = 1; c.weightx = 0.7; panel.add(cbTier, c);
        c.gridx = 0; c.gridy++; c.weightx = 0.3; panel.add(new JLabel("Package *:"), c);
        c.gridx = 1; c.weightx = 0.7; panel.add(cbPackage, c);
        c.gridx = 0; c.gridy++; c.gridwidth = 2; panel.add(lblAmount, c);

        JButton btnSubmit = new JButton("Submit Renewal Request");
        btnSubmit.setBackground(new Color(33, 120, 80));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setOpaque(true);
        btnSubmit.setBorderPainted(false);
        c.gridy++; panel.add(btnSubmit, c);

        dialog.add(panel);

        Runnable updateAmount = () -> {
            String tier = (String) cbTier.getSelectedItem();
            String pkg  = (String) cbPackage.getSelectedItem();
            double monthly = switch (tier) {
                case "GOLD" -> 1250.0;
                case "VIP"  -> 2000.0;
                default     -> 750.0;
            };
            double amount = switch (pkg) {
                case "ANNUAL_PREPAID"     -> monthly * 12 * 0.85;
                case "ANNUAL_INSTALLMENT" -> monthly * 1.07;
                default                   -> monthly;
            };
            String note = switch (pkg) {
                case "ANNUAL_PREPAID"     -> " (15% discount)";
                case "ANNUAL_INSTALLMENT" -> " /month (+7%)";
                default                   -> " /month";
            };
            lblAmount.setText(String.format("Amount: %.2f TL%s", amount, note));
        };

        cbTier.addActionListener(e -> updateAmount.run());
        cbPackage.addActionListener(e -> updateAmount.run());
        updateAmount.run();

        btnSubmit.addActionListener(e -> {
            String tier = (String) cbTier.getSelectedItem();
            String pkg  = (String) cbPackage.getSelectedItem();
            try {
                RegistrationRequest req = buildRenewalRequest(tier, pkg);
                memberService.submitRegistrationRequest(req);
                JOptionPane.showMessageDialog(dialog,
                        "<html><b>Renewal request submitted!</b><br><br>" +
                                "Please pay <b>" + lblAmount.getText().replace("Amount: ", "") +
                                "</b> to the manager.<br>" +
                                "Manager will approve within <b>3 days</b>.</html>",
                        "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private RegistrationRequest buildRenewalRequest(String tier, String pkg) {
        double monthly = switch (tier) {
            case "GOLD" -> 1250.0;
            case "VIP"  -> 2000.0;
            default     -> 750.0;
        };
        double amount = switch (pkg) {
            case "ANNUAL_PREPAID"     -> monthly * 12 * 0.85;
            case "ANNUAL_INSTALLMENT" -> monthly * 1.07;
            default                   -> monthly;
        };
        RegistrationRequest req = new RegistrationRequest();
        req.setMemberId(member.getMemberId());
        req.setTier(tier);
        req.setPackageType(pkg);
        req.setAmount(amount);
        req.setExpiresAt(LocalDateTime.now().plusDays(3));
        return req;
    }

    private void refreshMembershipTab() {
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JTabbedPane tabs) {
                tabs.setComponentAt(1, buildMembershipPanel());
                tabs.revalidate();
                tabs.repaint();
                break;
            }
        }
    }

    // ── PAYMENT HISTORY ───────────────────────────────────────────
    private JPanel buildPaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"ID", "Date", "Amount", "Type", "Description", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Payment> payments = reportService.getPaymentsForMember(member.getMemberId());
        for (Payment p : payments) {
            model.addRow(new Object[]{
                    p.getPaymentId(),
                    p.getPaymentDate().toLocalDate(),
                    String.format("%.2f TL", p.getAmount()),
                    p.getPaymentType(),
                    p.getDescription(),
                    p.getStatus()
            });
        }
        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ── BMI & HEALTH ──────────────────────────────────────────────
    private JPanel buildBmiPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;

        if (member.getWeight() == null || member.getHeight() == null) {
            c.gridx = 0; c.gridy = 0;
            panel.add(new JLabel("Please complete your weight and height in My Profile to see BMI.",
                    SwingConstants.CENTER), c);
            return panel;
        }

        double bmi = member.getBmiValue() != null ? member.getBmiValue() : 0.0;
        String cat = member.getBmiCategory() != null ? member.getBmiCategory() : "N/A";

        JLabel lblBmi = new JLabel("BMI: " + bmi, SwingConstants.CENTER);
        lblBmi.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel lblCat = new JLabel(cat, SwingConstants.CENTER);
        lblCat.setFont(new Font("Arial", Font.BOLD, 18));
        lblCat.setForeground(bmiColor(cat));

        double calories = calcCalories();
        JLabel lblCal = new JLabel("Estimated Daily Calories: " + (int) calories + " kcal",
                SwingConstants.CENTER);

        Optional<Membership> msOpt = memberService.getActiveMembership(member.getMemberId());
        String tier = msOpt.map(Membership::getTier).orElse("CLASSIC");

        JTextArea txtRec = new JTextArea(getRecommendation(cat, tier));
        txtRec.setEditable(false);
        txtRec.setLineWrap(true);
        txtRec.setWrapStyleWord(true);
        txtRec.setBackground(panel.getBackground());
        txtRec.setBorder(BorderFactory.createTitledBorder("Recommendation"));

        c.gridx = 0; c.gridy = 0; panel.add(lblBmi, c);
        c.gridy = 1; panel.add(lblCat, c);
        c.gridy = 2; panel.add(lblCal, c);
        c.gridy = 3; panel.add(txtRec, c);

        return panel;
    }

    // ── HELPERS ──────────────────────────────────────────────────
    private JLabel makeTierLabel(String tier) {
        JLabel lbl = new JLabel(tier);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(switch (tier) {
            case "GOLD" -> new Color(180, 130, 0);
            case "VIP"  -> new Color(150, 0, 150);
            default     -> new Color(60, 60, 60);
        });
        return lbl;
    }

    private Color bmiColor(String cat) {
        return switch (cat) {
            case "UNDERWEIGHT" -> Color.BLUE;
            case "NORMAL"      -> new Color(0, 150, 0);
            case "OVERWEIGHT"  -> new Color(200, 130, 0);
            case "OBESE"       -> Color.RED;
            default            -> Color.BLACK;
        };
    }

    private double calcCalories() {
        if (member.getWeight() == null || member.getHeight() == null) return 0;
        double bmr;
        if ("MALE".equals(member.getGender())) {
            bmr = 88.362 + (13.397 * member.getWeight())
                    + (4.799 * member.getHeight())
                    - (5.677 * getAge());
        } else {
            bmr = 447.593 + (9.247 * member.getWeight())
                    + (3.098 * member.getHeight())
                    - (4.330 * getAge());
        }
        return Math.round(bmr * 1.55);
    }

    private int getAge() {
        return (int) ChronoUnit.YEARS.between(member.getDateOfBirth(), LocalDate.now());
    }

    private String getRecommendation(String bmiCat, String tier) {
        String base = switch (bmiCat) {
            case "UNDERWEIGHT" -> "Focus on strength training and a calorie-surplus diet.";
            case "NORMAL"      -> "Great shape! Maintain with regular cardio and balanced nutrition.";
            case "OVERWEIGHT"  -> "Consider increasing cardio sessions and reducing processed food intake.";
            case "OBESE"       -> "Start with low-impact exercises and consult a nutritionist.";
            default            -> "Please update your profile to get personalized recommendations.";
        };
        String tierNote = switch (tier) {
            case "GOLD" -> " With your Gold membership, you can book PT sessions (2/month).";
            case "VIP"  -> " With your VIP membership, you have full access to all facilities.";
            default     -> " Upgrade to Gold or VIP for personal trainer access.";
        };
        return base + tierNote;
    }
}