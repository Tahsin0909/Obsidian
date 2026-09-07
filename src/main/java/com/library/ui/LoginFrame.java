package com.library.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.library.model.Admin;
import com.library.model.Member;
import com.library.model.User;
import com.library.services.AuthService;

public class LoginFrame extends JFrame {

        private final AuthService authService = new AuthService();
        private JTextField emailField;
        private JPasswordField passwordField;
        private JLabel statusLabel;

        public LoginFrame() {
                setTitle("Library Management System - Login");
                setSize(800, 500);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setLocationRelativeTo(null);

                buildUI();
        }

        private void buildUI() {

                // ==================== MAIN PANEL ====================

                JPanel mainPanel = new JPanel(new GridLayout(1, 2));

                // ==================== LEFT IMAGE SECTION ====================
                JPanel imagePanel = new JPanel(new BorderLayout());

                ImageIcon originalIcon = new ImageIcon(
                                getClass().getResource("/sideImage.jpg"));

                JLabel imageLabel = new JLabel();
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                imageLabel.setVerticalAlignment(SwingConstants.CENTER);

                imagePanel.add(imageLabel, BorderLayout.CENTER);

                imagePanel.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {

                                Image image = originalIcon.getImage().getScaledInstance(
                                                imagePanel.getWidth(),
                                                imagePanel.getHeight(),
                                                Image.SCALE_SMOOTH);

                                imageLabel.setIcon(new ImageIcon(image));
                        }
                });

                // ==================== RIGHT LOGIN SECTION ====================

                JPanel loginPanel = new JPanel();

                loginPanel.setLayout(
                                new BoxLayout(loginPanel, BoxLayout.Y_AXIS));

                loginPanel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                60, 50, 60, 50));

                // ==================== LOGIN TITLE ====================

                JLabel title = new JLabel("Welcome Back");

                title.setFont(
                                new Font("Arial", Font.BOLD, 26));

                title.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel subtitle = new JLabel("Sign in to continue");

                subtitle.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                // ==================== EMAIL ====================

                JLabel emailLabel = new JLabel("Email", SwingConstants.LEFT);

                emailLabel.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                emailLabel.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                emailLabel.getPreferredSize().height));

                emailField = new JTextField();

                emailField.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                35));

                emailField.setAlignmentX(
                                Component.CENTER_ALIGNMENT);
                emailField.addActionListener(e -> onLogin());

                // ==================== PASSWORD ====================

                JLabel passwordLabel = new JLabel("Password", SwingConstants.LEFT);

                passwordLabel.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                passwordLabel.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                passwordLabel.getPreferredSize().height));

                passwordField = new JPasswordField();

                passwordField.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                35));

                passwordField.setAlignmentX(
                                Component.CENTER_ALIGNMENT);
                passwordField.addActionListener(e -> onLogin());

                // ==================== LOGIN BUTTON ====================

                JButton loginButton = UITheme.primaryButton("Login");

                loginButton.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                loginButton.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                35));
                loginButton.addActionListener(e -> onLogin());

                // ==================== STATUS ====================

                statusLabel = new JLabel(" ");

                statusLabel.setAlignmentX(
                                Component.CENTER_ALIGNMENT);
                statusLabel.setForeground(UITheme.DANGER);

                // ==================== Hint ====================

                JLabel hint = new JLabel("<html><i>Demo logins (all passwords: 123456):<br>"
                                + "Admin &nbsp;&nbsp;admin@mail.com<br>"
                                + "Member  member@mail.com</i></html>");
                hint.setFont(UITheme.FONT_SMALL);
                hint.setForeground(UITheme.TEXT_MUTED);
                hint.setAlignmentX(Component.CENTER_ALIGNMENT);
                hint.setHorizontalAlignment(SwingConstants.LEFT);
                hint.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                hint.getPreferredSize().height));
                // ==================== ADD COMPONENTS ====================

                loginPanel.add(title);

                loginPanel.add(
                                Box.createVerticalStrut(10));

                loginPanel.add(subtitle);

                loginPanel.add(
                                Box.createVerticalStrut(35));

                loginPanel.add(emailLabel);

                loginPanel.add(
                                Box.createVerticalStrut(5));

                loginPanel.add(emailField);

                loginPanel.add(
                                Box.createVerticalStrut(15));

                loginPanel.add(passwordLabel);

                loginPanel.add(
                                Box.createVerticalStrut(5));

                loginPanel.add(passwordField);

                loginPanel.add(
                                Box.createVerticalStrut(25));

                loginPanel.add(loginButton);

                loginPanel.add(
                                Box.createVerticalStrut(15));

                loginPanel.add(statusLabel);
                loginPanel.add(
                                Box.createVerticalStrut(15));
                loginPanel.add(hint);

                // ==================== ADD PANELS ====================

                mainPanel.add(imagePanel);
                mainPanel.add(loginPanel);

                add(mainPanel);
        }

        // ==================== LOGIN METHOD ====================

        private void onLogin() {
                String email = emailField.getText().trim();
                String password = new String(passwordField.getPassword());

                if (email.isEmpty() || password.isEmpty()) {
                        statusLabel.setForeground(UITheme.DANGER);
                        statusLabel.setText("Please enter both email and password.");
                        return;
                }

                User user = authService.login(email, password);
                if (user == null) {
                        statusLabel.setForeground(UITheme.DANGER);
                        statusLabel.setText("Invalid email or password.");
                        return;
                }

                // Authentication succeeded - Redirect based on Polymorphic User Role
                dispose();

                if (user instanceof Admin) {
                        new AdminDashboard((Admin) user).setVisible(true);
                } else if (user instanceof Member) {
                        new MemberDashboard((Member) user).setVisible(true);
                }
        }

        public static void main(String[] args) {

                SwingUtilities.invokeLater(() -> {

                        new LoginFrame().setVisible(true);

                });
        }
}