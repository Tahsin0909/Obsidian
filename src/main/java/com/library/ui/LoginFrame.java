package com.library.ui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

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

                // ==================== LOGIN BUTTON ====================

                JButton loginButton = UITheme.primaryButton("Login");
                loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);

                loginButton.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                35));

                loginButton.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                // ==================== STATUS ====================

                statusLabel = new JLabel(" ");

                statusLabel.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                // ==================== LOGIN ACTION ====================

                loginButton.addActionListener(
                                e -> login());

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

                // ==================== ADD PANELS ====================

                mainPanel.add(imagePanel);
                mainPanel.add(loginPanel);

                add(mainPanel);
        }

        // ==================== LOGIN METHOD ====================

        private void login() {

                String email = emailField.getText();

                String password = new String(
                                passwordField.getPassword());

                if (email.isEmpty() || password.isEmpty()) {

                        statusLabel.setText(
                                        "Please enter email and password.");

                        return;
                }

                statusLabel.setText(
                                "Login successful!");
        }

        // ==================== MAIN METHOD ====================

        public static void main(String[] args) {

                SwingUtilities.invokeLater(() -> {

                        new LoginFrame().setVisible(true);

                });
        }
}