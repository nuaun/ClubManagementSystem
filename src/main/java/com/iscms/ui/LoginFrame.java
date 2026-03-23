package com.iscms.ui;

import com.iscms.model.Manager;
import com.iscms.model.Member;
import com.iscms.model.Trainer;
import com.iscms.service.AuthService;
import com.iscms.service.LoginResult;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();

    private JRadioButton   rbMember;
    private JRadioButton   rbManager;
    private JRadioButton   rbTrainer;
    private JLabel         lblIdentifier;
    private JTextField     txtIdentifier;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JButton        btnRegister;
    private JButton        btnForgot;

    public LoginFrame() {
        setTitle("ISC-MS — Istanbul Sports Club");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 340);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill   = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("ISC Membership System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        main.add(title, c);

        rbMember  = new JRadioButton("Member",  true);
        rbManager = new JRadioButton("Manager", false);
        rbTrainer = new JRadioButton("Trainer", false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbMember); bg.add(rbManager); bg.add(rbTrainer);
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        rolePanel.add(rbMember);
        rolePanel.add(rbManager);
        rolePanel.add(rbTrainer);
        c.gridy = 1;
        main.add(rolePanel, c);

        lblIdentifier = new JLabel("Phone Number:");
        c.gridy = 2; c.gridwidth = 1;
        main.add(lblIdentifier, c);

        txtIdentifier = new JTextField();
        c.gridx = 1;
        main.add(txtIdentifier, c);

        c.gridx = 0; c.gridy = 3;
        main.add(new JLabel("Password:"), c);

        txtPassword = new JPasswordField();
        c.gridx = 1;
        main.add(txtPassword, c);

        btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(33, 87, 141));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        main.add(btnLogin, c);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnRegister = new JButton("Register");
        btnForgot   = new JButton("Forgot Password");
        btnRegister.setBorderPainted(false);
        btnForgot.setBorderPainted(false);
        btnRegister.setContentAreaFilled(false);
        btnForgot.setContentAreaFilled(false);
        btnRegister.setForeground(Color.BLUE);
        btnForgot.setForeground(Color.BLUE);
        bottomPanel.add(btnRegister);
        bottomPanel.add(btnForgot);
        c.gridy = 5;
        main.add(bottomPanel, c);

        add(main);

        rbMember.addActionListener(e  -> lblIdentifier.setText("Phone Number:"));
        rbManager.addActionListener(e -> lblIdentifier.setText("Email:"));
        rbTrainer.addActionListener(e -> lblIdentifier.setText("Username:"));

        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> openRegister());
        btnForgot.addActionListener(e -> openForgotPassword());
    }

    private void handleLogin() {
        String identifier = txtIdentifier.getText().trim();
        String password   = new String(txtPassword.getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LoginResult result;
        if (rbMember.isSelected()) {
            result = authService.loginMember(identifier, password);
        } else if (rbManager.isSelected()) {
            result = authService.loginManager(identifier, password);
        } else {
            result = authService.loginTrainer(identifier, password);
        }

        switch (result.getStatus()) {
            case SUCCESS -> {
                dispose();
                if (rbMember.isSelected()) {
                    new MemberDashboard((Member) result.getUser()).setVisible(true);
                } else if (rbTrainer.isSelected()) {
                    new TrainerDashboard((Trainer) result.getUser()).setVisible(true);
                } else {
                    Manager mgr = (Manager) result.getUser();
                    if ("ADMIN".equals(mgr.getRole())) {
                        new AdminDashboard(mgr).setVisible(true);
                    } else {
                        new ManagerDashboard(mgr).setVisible(true);
                    }
                }
            }
            case NOT_FOUND ->
                    JOptionPane.showMessageDialog(this, "Account not found.", "Error", JOptionPane.ERROR_MESSAGE);
            case WRONG_PASSWORD -> {
                int rem = result.getRemainingTries();
                String wrongMsg;
                if (rbManager.isSelected()) {
                    wrongMsg = "Wrong password. " + rem + " attempt(s) remaining before this manager account is locked.\n"
                             + "If your account gets locked, contact the system administrator (Admin).";
                } else if (rbTrainer.isSelected()) {
                    wrongMsg = "Wrong password. " + rem + " attempt(s) remaining before this trainer account is locked.\n"
                             + "If your account gets locked, contact the club manager.";
                } else {
                    wrongMsg = "Wrong password. " + rem + " attempt(s) remaining.";
                }
                JOptionPane.showMessageDialog(this, wrongMsg, "Wrong Password", JOptionPane.ERROR_MESSAGE);
            }
            case SUGGEST_RESET -> {
                int choice = JOptionPane.showConfirmDialog(this,
                        "You have entered the wrong password 3 times.\nWould you like to reset your password?\n\n" +
                        "If you skip, you have 3 more attempts before your account is locked.",
                        "Reset Password?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) new ForgotPasswordFrame().setVisible(true);
            }
            case LOCKED -> {
                String lockedMsg;
                if (rbMember.isSelected()) {
                    lockedMsg = "Your account has been locked due to too many failed login attempts.\n"
                              + "Please contact the club manager to unlock your account.";
                } else if (rbManager.isSelected()) {
                    lockedMsg = "This manager account has been locked due to too many failed login attempts.\n"
                              + "Please contact the system administrator (Admin) to unlock your account.";
                } else {
                    lockedMsg = "This trainer account has been locked due to too many failed login attempts.\n"
                              + "Please contact the club manager to unlock your account.";
                }
                JOptionPane.showMessageDialog(this, lockedMsg, "Account Locked", JOptionPane.ERROR_MESSAGE);
            }
            case SUSPENDED ->
                    JOptionPane.showMessageDialog(this,
                            "Your account has been suspended. Please contact the club.",
                            "Suspended", JOptionPane.ERROR_MESSAGE);
            case ARCHIVED ->
                    JOptionPane.showMessageDialog(this,
                            "Your account has been archived.", "Archived", JOptionPane.ERROR_MESSAGE);
            case PENDING ->
                    JOptionPane.showMessageDialog(this,
                            "<html><b>Registration Pending</b><br><br>" +
                                    "Your registration is awaiting manager approval.<br>" +
                                    "Please ensure payment has been made.<br>" +
                                    "Approval may take up to 3 days.</html>",
                            "Pending Approval", JOptionPane.INFORMATION_MESSAGE);
            case REGISTRATION_FAILED ->
                    JOptionPane.showMessageDialog(this,
                            "<html><b>Registration Expired</b><br><br>" +
                                    "Your registration request has expired.<br>" +
                                    "Payment was not received within 3 days.<br><br>" +
                                    "Please register again.</html>",
                            "Registration Expired", JOptionPane.ERROR_MESSAGE);
            case FROZEN ->
                    JOptionPane.showMessageDialog(this,
                            "<html><b>Membership Frozen</b><br><br>" +
                                    "Your membership is currently frozen.<br>" +
                                    "You can log in again after the freeze period ends.</html>",
                            "Account Frozen", JOptionPane.WARNING_MESSAGE);
            case PASSIVE ->
                    JOptionPane.showMessageDialog(this,
                            "<html><b>Membership Expired</b><br><br>" +
                                    "Your membership has expired.<br>" +
                                    "Please contact the club to renew your membership.</html>",
                            "Membership Expired", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openRegister() {
        dispose();
        new RegisterFrame().setVisible(true);
    }

    private void openForgotPassword() {
        new ForgotPasswordFrame().setVisible(true);
    }
}