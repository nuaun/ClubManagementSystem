package com.iscms.ui;

import com.iscms.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class ForgotPasswordFrame extends JFrame {

    private final AuthService authService = new AuthService();

    public ForgotPasswordFrame() {
        setTitle("Reset Password");
        setSize(420, 280);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets  = new Insets(8, 8, 8, 8);
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JTextField     txtPhone   = new JTextField();
        JPasswordField txtNewPass = new JPasswordField();
        JPasswordField txtConfirm = new JPasswordField();

        c.gridx = 0; c.gridy = 0; c.weightx = 0.4;
        panel.add(new JLabel("Phone Number:"), c);
        c.gridx = 1; c.weightx = 0.6;
        panel.add(txtPhone, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0.4;
        panel.add(new JLabel("New Password:"), c);
        c.gridx = 1; c.weightx = 0.6;
        panel.add(txtNewPass, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0.4;
        panel.add(new JLabel("Confirm Password:"), c);
        c.gridx = 1; c.weightx = 0.6;
        panel.add(txtConfirm, c);

        JButton btnReset = new JButton("Reset Password");
        btnReset.setBackground(new Color(33, 87, 141));
        btnReset.setForeground(Color.WHITE);
        btnReset.setOpaque(true);
        btnReset.setBorderPainted(false);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        panel.add(btnReset, c);

        add(panel);

        btnReset.addActionListener(e -> {
            String phone   = txtPhone.getText().trim();
            String newPass = new String(txtNewPass.getPassword());
            String confirm = new String(txtConfirm.getPassword());

            if (phone.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }
            // FIX-8: Minimum şifre uzunluğu — 1 karakterlik şifre kabul edilmemeli
            if (newPass.length() < 8) {
                JOptionPane.showMessageDialog(this,
                        "Password must be at least 8 characters.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }
            boolean ok = authService.resetMemberPassword(phone, newPass);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Password reset successful! You can now login.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Phone number not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}