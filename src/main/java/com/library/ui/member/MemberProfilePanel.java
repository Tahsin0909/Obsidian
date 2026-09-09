package com.library.ui.member;

import com.library.model.Member;
import com.library.store.DataStore;
import com.library.ui.MemberDashboard;
import com.library.ui.UIHelper;
import com.library.ui.UITheme;
import com.library.util.PasswordUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel managing member personal profile details and password security.
 * Allows updating contact details, email, and password hashing verification.
 */
public class MemberProfilePanel extends JPanel {

    private final MemberDashboard dashboard;
    private final Member member;
    private final DataStore store = DataStore.getInstance();

    private JLabel profileHeaderNameLabel;
    private JLabel profileHeaderSubLabel;

    private JTextField profileNameField;
    private JTextField profileUsernameField;
    private JTextField profileEmailField;
    private JTextField profilePhoneField;
    private JTextField profileAddressField;

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public MemberProfilePanel(MemberDashboard dashboard, Member member) {
        this.dashboard = dashboard;
        this.member = member;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        buildUI();
    }

    private void buildUI() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 14));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Profile Banner Header
        JPanel bannerCard = UITheme.card();
        bannerCard.setLayout(new BorderLayout(16, 0));

        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel bannerText = new JPanel();
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.setOpaque(false);

        profileHeaderNameLabel = new JLabel(member.getName());
        profileHeaderNameLabel.setFont(UITheme.FONT_TITLE);
        profileHeaderNameLabel.setForeground(UITheme.PRIMARY);

        profileHeaderSubLabel = new JLabel("Member ID #" + member.getMemberId() + "  •  " + member.getEmail() + "  •  Status: " + member.getStatus());
        profileHeaderSubLabel.setFont(UITheme.FONT_BODY);
        profileHeaderSubLabel.setForeground(UITheme.TEXT_MUTED);

        bannerText.add(profileHeaderNameLabel);
        bannerText.add(Box.createVerticalStrut(4));
        bannerText.add(profileHeaderSubLabel);

        bannerCard.add(avatarLabel, BorderLayout.WEST);
        bannerCard.add(bannerText, BorderLayout.CENTER);

        // 2. Center forms: Two side-by-side cards
        JPanel formsContainer = new JPanel(new GridLayout(1, 2, 16, 0));
        formsContainer.setOpaque(false);

        // Form Card 1: Personal Details
        JPanel personalDetailsCard = createPersonalDetailsCard();

        // Form Card 2: Security & Password Update
        JPanel securityCard = createSecurityCard();

        formsContainer.add(personalDetailsCard);
        formsContainer.add(securityCard);

        contentPanel.add(bannerCard, BorderLayout.NORTH);
        contentPanel.add(formsContainer, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createPersonalDetailsCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Personal Information");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.PRIMARY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField memberIdField = new JTextField("#" + member.getMemberId());
        memberIdField.setEditable(false);
        memberIdField.setBackground(new Color(0xF4, 0xF6, 0xF8));
        memberIdField.setFont(UITheme.FONT_BODY_BOLD);
        memberIdField.setForeground(UITheme.PRIMARY);

        profileNameField = new JTextField(member.getName());
        profileNameField.setFont(UITheme.FONT_BODY);

        profileUsernameField = new JTextField(member.getUsername());
        profileUsernameField.setFont(UITheme.FONT_BODY);

        profileEmailField = new JTextField(member.getEmail());
        profileEmailField.setFont(UITheme.FONT_BODY);

        profilePhoneField = new JTextField(member.getPhone() != null ? member.getPhone() : "");
        profilePhoneField.setFont(UITheme.FONT_BODY);

        profileAddressField = new JTextField(member.getAddress() != null ? member.getAddress() : "");
        profileAddressField.setFont(UITheme.FONT_BODY);

        JTextField membershipDateField = new JTextField(member.getMembershipDate() != null ? member.getMembershipDate().toString() : "-");
        membershipDateField.setEditable(false);
        membershipDateField.setBackground(new Color(0xF4, 0xF6, 0xF8));
        membershipDateField.setFont(UITheme.FONT_BODY);

        JTextField statusField = new JTextField(member.getStatus() != null ? member.getStatus().name() : "ACTIVE");
        statusField.setEditable(false);
        statusField.setBackground(new Color(0xF4, 0xF6, 0xF8));
        statusField.setFont(UITheme.FONT_BODY_BOLD);
        statusField.setForeground(member.getStatus() == Member.Status.ACTIVE ? new Color(0x1B, 0x7A, 0x4B) : UITheme.DANGER);

        UIHelper.addFormField(form, gbc, 0, "Member ID:", memberIdField);
        UIHelper.addFormField(form, gbc, 1, "Full Name *:", profileNameField);
        UIHelper.addFormField(form, gbc, 2, "Username *:", profileUsernameField);
        UIHelper.addFormField(form, gbc, 3, "Email Address *:", profileEmailField);
        UIHelper.addFormField(form, gbc, 4, "Phone Number:", profilePhoneField);
        UIHelper.addFormField(form, gbc, 5, "Address:", profileAddressField);
        UIHelper.addFormField(form, gbc, 6, "Member Since:", membershipDateField);
        UIHelper.addFormField(form, gbc, 7, "Account Status:", statusField);

        // Trailing glue anchors fields to the top to prevent clipping
        GridBagConstraints glueGbc = new GridBagConstraints();
        glueGbc.gridx = 0;
        glueGbc.gridy = 8;
        glueGbc.gridwidth = 2;
        glueGbc.weighty = 1.0;
        glueGbc.fill = GridBagConstraints.VERTICAL;
        form.add(Box.createVerticalGlue(), glueGbc);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        JButton resetBtn = UITheme.secondaryButton("Reset");
        resetBtn.addActionListener(e -> resetProfileFields());

        JButton saveBtn = UITheme.primaryButton("Save Profile Changes");
        saveBtn.addActionListener(e -> saveProfileChanges());

        btnBar.add(resetBtn);
        btnBar.add(saveBtn);

        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(btnBar, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createSecurityCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Security & Password");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.PRIMARY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        currentPasswordField = new JPasswordField();
        currentPasswordField.setFont(UITheme.FONT_BODY);

        newPasswordField = new JPasswordField();
        newPasswordField.setFont(UITheme.FONT_BODY);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(UITheme.FONT_BODY);

        JLabel infoLabel = new JLabel("<html><span style='color:#6B7684; font-size:11px;'>Password must be at least 4 characters long and verified against your current password.</span></html>");

        UIHelper.addFormField(form, gbc, 0, "Current Password *:", currentPasswordField);
        UIHelper.addFormField(form, gbc, 1, "New Password *:", newPasswordField);
        UIHelper.addFormField(form, gbc, 2, "Confirm New Password *:", confirmPasswordField);
        UIHelper.addFormField(form, gbc, 3, "", infoLabel);

        // Trailing glue anchors fields to the top
        GridBagConstraints glueGbc = new GridBagConstraints();
        glueGbc.gridx = 0;
        glueGbc.gridy = 4;
        glueGbc.gridwidth = 2;
        glueGbc.weighty = 1.0;
        glueGbc.fill = GridBagConstraints.VERTICAL;
        form.add(Box.createVerticalGlue(), glueGbc);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        JButton updatePasswordBtn = UITheme.accentButton("Update Password");
        updatePasswordBtn.addActionListener(e -> updatePassword());

        btnBar.add(updatePasswordBtn);

        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(btnBar, BorderLayout.SOUTH);

        return card;
    }

    public void resetProfileFields() {
        if (profileNameField != null) profileNameField.setText(member.getName());
        if (profileUsernameField != null) profileUsernameField.setText(member.getUsername());
        if (profileEmailField != null) profileEmailField.setText(member.getEmail());
        if (profilePhoneField != null) profilePhoneField.setText(member.getPhone() != null ? member.getPhone() : "");
        if (profileAddressField != null) profileAddressField.setText(member.getAddress() != null ? member.getAddress() : "");
    }

    public void saveProfileChanges() {
        String name = profileNameField.getText().trim();
        String username = profileUsernameField.getText().trim();
        String email = profileEmailField.getText().trim();
        String phone = profilePhoneField.getText().trim();
        String address = profileAddressField.getText().trim();

        if (name.isEmpty() || username.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all mandatory fields: Full Name, Username, and Email.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this,
                    "Please provide a valid email address (e.g., student@university.edu).",
                    "Invalid Email",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if username or email is already taken by another user
        if (store.isUsernameOrEmailTaken(username, member.getMemberId())) {
            JOptionPane.showMessageDialog(this,
                    "The username \"" + username + "\" is already taken by another account!",
                    "Duplicate Username",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (store.isUsernameOrEmailTaken(email, member.getMemberId())) {
            JOptionPane.showMessageDialog(this,
                    "The email address \"" + email + "\" is already registered to another account!",
                    "Duplicate Email",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Apply changes to member
        member.setName(name);
        member.setUsername(username);
        member.setEmail(email);
        member.setPhone(phone.isEmpty() ? "-" : phone);
        member.setAddress(address.isEmpty() ? "-" : address);

        // Update in DataStore
        store.updateMember(member);

        // Update Profile Header
        if (profileHeaderNameLabel != null) {
            profileHeaderNameLabel.setText(member.getName());
        }
        if (profileHeaderSubLabel != null) {
            profileHeaderSubLabel.setText("Member ID #" + member.getMemberId() + "  •  " + member.getEmail() + "  •  Status: " + member.getStatus());
        }

        // Notify dashboard to update header greeting and badges
        if (dashboard != null) {
            dashboard.updateHeaderInfo();
            dashboard.refreshAll();
        }

        JOptionPane.showMessageDialog(this,
                "Profile updated successfully!\nYour new details have been saved.",
                "Profile Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void updatePassword() {
        String currentPass = new String(currentPasswordField.getPassword()).trim();
        String newPass = new String(newPasswordField.getPassword()).trim();
        String confirmPass = new String(confirmPasswordField.getPassword()).trim();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all password fields.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!PasswordUtil.verify(currentPass, member.getPassword())) {
            JOptionPane.showMessageDialog(this,
                    "The current password you entered is incorrect. Please try again.",
                    "Authentication Failed",
                    JOptionPane.ERROR_MESSAGE);
            currentPasswordField.setText("");
            currentPasswordField.requestFocus();
            return;
        }

        if (newPass.length() < 4) {
            JOptionPane.showMessageDialog(this,
                    "New password must be at least 4 characters in length.",
                    "Weak Password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this,
                    "The new password and confirmation password do not match. Please re-enter.",
                    "Password Mismatch",
                    JOptionPane.WARNING_MESSAGE);
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocus();
            return;
        }

        if (newPass.equals(currentPass)) {
            JOptionPane.showMessageDialog(this,
                    "The new password cannot be the same as your current password.",
                    "Same Password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hash and save new password
        member.setPassword(PasswordUtil.hash(newPass));
        store.updateMember(member);

        // Clear password fields
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");

        JOptionPane.showMessageDialog(this,
                "Password updated successfully!\nPlease use your new password next time you log in.",
                "Password Changed",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
