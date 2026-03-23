package com.iscms.ui;

import com.iscms.model.Member;
import com.iscms.service.MemberFactory;
import com.iscms.service.MemberService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class RegisterFrame extends JFrame {

    private final MemberService memberService = new MemberService();

    private JTextField     txtName;
    private JTextField     txtDob;
    private JComboBox<String> cbGender;
    private JTextField     txtPhone;
    private JTextField     txtEmail;
    private JPasswordField txtPass;
    private JPasswordField txtPassConfirm;
    private JTextField     txtWeight;
    private JTextField     txtHeight;
    private JTextField     txtEcName;
    private JTextField     txtEcPhone;
    private JComboBox<String> cbTier;
    private JComboBox<String> cbPackage;
    private JLabel         lblAmount;

    public RegisterFrame() {
        setTitle("ISC-MS — Register");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(550, 660);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill   = GridBagConstraints.HORIZONTAL;

        txtName        = new JTextField(20);
        txtDob         = new JTextField("YYYY-MM-DD");
        cbGender       = new JComboBox<>(new String[]{"MALE", "FEMALE", "OTHER"});
        txtPhone       = new JTextField(20);
        txtEmail       = new JTextField(20);
        txtPass        = new JPasswordField(20);
        txtPassConfirm = new JPasswordField(20);
        txtWeight      = new JTextField(20);
        txtHeight      = new JTextField(20);
        txtEcName      = new JTextField(20);
        txtEcPhone     = new JTextField(20);
        cbTier         = new JComboBox<>(new String[]{"CLASSIC", "GOLD", "VIP"});
        cbPackage      = new JComboBox<>(new String[]{"MONTHLY", "ANNUAL_INSTALLMENT", "ANNUAL_PREPAID"});
        lblAmount      = new JLabel("Amount: 750.00 TL /month");
        lblAmount.setFont(new Font("Arial", Font.BOLD, 13));
        lblAmount.setForeground(new Color(33, 87, 141));

        String[] labels = {
                "Full Name *", "Date of Birth * (YYYY-MM-DD)", "Gender *",
                "Phone * (10 digits)", "Email", "Password *", "Confirm Password *",
                "Weight (kg)", "Height (cm)",
                "Emergency Contact Name", "Emergency Contact Phone",
                "Tier *", "Package *", "Estimated Amount"
        };
        Component[] inputs = {
                txtName, txtDob, cbGender, txtPhone, txtEmail,
                txtPass, txtPassConfirm, txtWeight, txtHeight,
                txtEcName, txtEcPhone, cbTier, cbPackage, lblAmount
        };

        for (int i = 0; i < inputs.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0.35;
            panel.add(new JLabel(labels[i] + ":"), c);
            c.gridx = 1; c.weightx = 0.65;
            panel.add(inputs[i], c);
        }

        JButton btnRegister = new JButton("Submit Registration Request");
        btnRegister.setBackground(new Color(33, 87, 141));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setOpaque(true);
        btnRegister.setBorderPainted(false);
        c.gridx = 0; c.gridy = inputs.length; c.gridwidth = 2;
        panel.add(btnRegister, c);

        JButton btnBack = new JButton("Back to Login");
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setForeground(Color.BLUE);
        c.gridy = inputs.length + 1;
        panel.add(btnBack, c);

        add(new JScrollPane(panel));

        cbTier.addActionListener(e -> updateAmount());
        cbPackage.addActionListener(e -> updateAmount());
        btnRegister.addActionListener(e -> handleRegister());
        btnBack.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void updateAmount() {
        // FIX-6: Fiyat mantığı artık Service katmanında — UI burada sadece gösteriyor.
        // Daha önce hem UI'da hem MemberService'de aynı hesap vardı (DRY ihlali).
        String tier = (String) cbTier.getSelectedItem();
        String pkg  = (String) cbPackage.getSelectedItem();
        double amount = memberService.calculateAmount(tier, pkg);
        String note = switch (pkg) {
            case "ANNUAL_PREPAID"     -> " (15% discount, total)";
            case "ANNUAL_INSTALLMENT" -> " /month (+7% vade)";
            default                   -> " /month";
        };
        lblAmount.setText(String.format("Amount: %.2f TL%s", amount, note));
    }

    private void handleRegister() {
        String name    = txtName.getText().trim();
        String dob     = txtDob.getText().trim();
        String phone   = txtPhone.getText().trim();
        String pass    = new String(txtPass.getPassword());
        String confirm = new String(txtPassConfirm.getPassword());

        if (name.isEmpty() || dob.isEmpty() || phone.isEmpty() || pass.isEmpty()
                || txtEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (*).",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (pass.length() < 8) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (phone.length() != 10 || !phone.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Phone must be exactly 10 digits.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String selectedTier = (String) cbTier.getSelectedItem();

            // Factory pattern — creates a PENDING member (tier stored in Membership table)
            Member m = MemberFactory.createPendingMember(
                    name, LocalDate.parse(dob),
                    (String) cbGender.getSelectedItem(),
                    phone, txtEmail.getText().trim(), pass);

            if (!txtWeight.getText().isBlank()) m.setWeight(Double.parseDouble(txtWeight.getText().trim()));
            if (!txtHeight.getText().isBlank()) m.setHeight(Double.parseDouble(txtHeight.getText().trim()));
            m.setEmergencyContactName(txtEcName.getText().trim());
            m.setEmergencyContactPhone(txtEcPhone.getText().trim());

            String pkg = (String) cbPackage.getSelectedItem();
            memberService.createRegistrationRequest(m, selectedTier, pkg);

            JOptionPane.showMessageDialog(this,
                    "<html><b>Registration request submitted!</b><br><br>" +
                            "Please pay <b>" + lblAmount.getText().replace("Amount: ", "") + "</b> to the club.<br>" +
                            "Manager will approve within <b>3 days</b>.<br><br>" +
                            "If payment is not received within 3 days,<br>your request will expire.</html>",
                    "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}