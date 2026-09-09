package com.library.ui.admin;

import com.library.model.Member;
import com.library.store.DataStore;
import com.library.ui.AdminDashboard;
import com.library.ui.UIHelper;
import com.library.ui.UITheme;
import com.library.util.PasswordUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;

/**
 * Panel managing member accounts, registration, profile modifications, and
 * member status.
 */
public class AdminMembersPanel extends JPanel {

    private final AdminDashboard dashboard;
    private final DataStore store = DataStore.getInstance();

    private DefaultTableModel membersTableModel;
    private JTable membersTable;
    private JTextField searchField;

    public AdminMembersPanel(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.CARD_BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        // Responsive Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 8));
        toolbar.setOpaque(false);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBox.setOpaque(false);
        JLabel searchLbl = new JLabel("Search Members:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        searchLbl.setForeground(UITheme.TEXT_DARK);
        searchField = new JTextField();
        UITheme.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(240, 30));
        searchBox.add(searchLbl);
        searchBox.add(searchField);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionsBox.setOpaque(false);

        JButton addMemberBtn = UITheme.primaryButton("+ Add Member");
        addMemberBtn.addActionListener(e -> showAddMemberDialog());

        JButton updateMemberBtn = UITheme.secondaryButton("Update Member");
        updateMemberBtn.addActionListener(e -> onUpdateMemberClicked());

        JButton memberFinesBtn = UITheme.accentButton("View Member Fines");
        memberFinesBtn.addActionListener(e -> {
            Member m = getSelectedMember();
            if (dashboard.getFinesPanel() != null) {
                dashboard.getFinesPanel().showMemberFinesDialog(m);
            }
        });

        actionsBox.add(addMemberBtn);
        actionsBox.add(updateMemberBtn);
        actionsBox.add(memberFinesBtn);

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(actionsBox, BorderLayout.EAST);

        String[] cols = { "Member ID", "Full Name", "Username", "Email", "Phone", "Address", "Join Date", "Status" };
        membersTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        membersTable = new JTable(membersTableModel);
        membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UITheme.styleTable(membersTable);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(membersTableModel);
        membersTable.setRowSorter(sorter);

        membersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && membersTable.getSelectedRow() != -1) {
                    onUpdateMemberClicked();
                }
            }
        });

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        UIHelper.centerAlignColumns(membersTable, 0, 2, 4, 6, 7);
        UIHelper.setColumnWidths(membersTable, 50, 130, 100, 140, 100, 160, 85, 75);

        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        add(toolbar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        if (membersTableModel == null)
            return;
        membersTableModel.setRowCount(0);
        for (Member m : store.members()) {
            membersTableModel.addRow(new Object[] {
                    m.getMemberId(),
                    m.getName(),
                    m.getUsername(),
                    m.getEmail(),
                    m.getPhone(),
                    m.getAddress(),
                    m.getMembershipDate(),
                    m.getStatus()
            });
        }
        if (dashboard.getFinesPanel() != null) {
            dashboard.getFinesPanel().refreshMemberFilterCombo();
        }
    }

    public Member getSelectedMember() {
        if (membersTable == null)
            return null;
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1)
            return null;
        int modelRow = membersTable.convertRowIndexToModel(selectedRow);
        int memberId = (Integer) membersTableModel.getValueAt(modelRow, 0);
        return store.findMemberById(memberId);
    }

    private void onUpdateMemberClicked() {
        Member member = getSelectedMember();
        if (member == null) {
            JOptionPane.showMessageDialog(dashboard, "Please select a member from the table to update.",
                    "No Member Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showUpdateMemberDialog(member);
    }

    private void showAddMemberDialog() {
        JDialog dialog = new JDialog(dashboard, "Add New Member", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(dashboard);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(UITheme.CARD_BG);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        JLabel heading = new JLabel("Add Member to Directory");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Register a new library member account");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField();
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField addressField = new JTextField();
        JPasswordField passwordField = new JPasswordField("123456");
        JComboBox<Member.Status> statusCombo = new JComboBox<>(Member.Status.values());
        statusCombo.setFont(UITheme.FONT_BODY);

        UIHelper.addFormField(form, gbc, 0, "Full Name:", nameField);
        UIHelper.addFormField(form, gbc, 1, "Username:", usernameField);
        UIHelper.addFormField(form, gbc, 2, "Email:", emailField);
        UIHelper.addFormField(form, gbc, 3, "Phone:", phoneField);
        UIHelper.addFormField(form, gbc, 4, "Address:", addressField);
        UIHelper.addFormField(form, gbc, 5, "Password (Initial):", passwordField);
        UIHelper.addFormField(form, gbc, 6, "Status:", statusCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UITheme.primaryButton("Save Member");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            Member.Status status = (Member.Status) statusCombo.getSelectedItem();

            if (name.isEmpty() || username.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in Name, Username, and Email.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter an initial password.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (store.isUsernameOrEmailTaken(username, -1)) {
                JOptionPane.showMessageDialog(dialog, "Username \"" + username + "\" is already taken!",
                        "Duplicate User", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (store.isUsernameOrEmailTaken(email, -1)) {
                JOptionPane.showMessageDialog(dialog, "Email \"" + email + "\" is already registered!",
                        "Duplicate User", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String passwordHash = PasswordUtil.hash(password);
            Member newMember = new Member(
                    store.nextMemberId(),
                    store.nextUserId(),
                    username,
                    passwordHash,
                    status == Member.Status.ACTIVE,
                    name,
                    email,
                    phone.isEmpty() ? "-" : phone,
                    address.isEmpty() ? "-" : address,
                    LocalDate.now(),
                    status != null ? status : Member.Status.ACTIVE);

            store.addMember(newMember);
            refresh();
            dashboard.refreshKpis();

            dialog.dispose();
            JOptionPane.showMessageDialog(dashboard, "Member \"" + newMember.getName() + "\" registered successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void showUpdateMemberDialog(Member member) {
        JDialog dialog = new JDialog(dashboard, "Update Member Details", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(dashboard);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(UITheme.CARD_BG);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        JLabel heading = new JLabel("Update Member (ID: " + member.getMemberId() + ")");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Modify member profile and account status");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(member.getName());
        JTextField usernameField = new JTextField(member.getUsername());
        JTextField emailField = new JTextField(member.getEmail());
        JTextField phoneField = new JTextField(member.getPhone());
        JTextField addressField = new JTextField(member.getAddress());
        JPasswordField passwordField = new JPasswordField();
        JComboBox<Member.Status> statusCombo = new JComboBox<>(Member.Status.values());
        statusCombo.setFont(UITheme.FONT_BODY);
        statusCombo.setSelectedItem(member.getStatus());

        UIHelper.addFormField(form, gbc, 0, "Full Name:", nameField);
        UIHelper.addFormField(form, gbc, 1, "Username:", usernameField);
        UIHelper.addFormField(form, gbc, 2, "Email:", emailField);
        UIHelper.addFormField(form, gbc, 3, "Phone:", phoneField);
        UIHelper.addFormField(form, gbc, 4, "Address:", addressField);
        UIHelper.addFormField(form, gbc, 5, "New Password (Optional):", passwordField);
        UIHelper.addFormField(form, gbc, 6, "Status:", statusCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UITheme.primaryButton("Save Changes");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            String newPassword = new String(passwordField.getPassword()).trim();
            Member.Status status = (Member.Status) statusCombo.getSelectedItem();

            if (name.isEmpty() || username.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in Name, Username, and Email.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (store.isUsernameOrEmailTaken(username, member.getMemberId())) {
                JOptionPane.showMessageDialog(dialog, "Username \"" + username + "\" is already taken!",
                        "Duplicate User", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (store.isUsernameOrEmailTaken(email, member.getMemberId())) {
                JOptionPane.showMessageDialog(dialog, "Email \"" + email + "\" is already registered!",
                        "Duplicate User", JOptionPane.WARNING_MESSAGE);
                return;
            }

            member.setName(name);
            member.setUsername(username);
            member.setEmail(email);
            member.setPhone(phone.isEmpty() ? "-" : phone);
            member.setAddress(address.isEmpty() ? "-" : address);
            if (status != null) {
                member.setStatus(status);
                member.setActive(status == Member.Status.ACTIVE);
            }
            if (!newPassword.isEmpty()) {
                member.setPassword(PasswordUtil.hash(newPassword));
            }

            store.updateMember(member);
            refresh();
            if (dashboard.getBorrowingsPanel() != null) {
                dashboard.getBorrowingsPanel().refresh();
            }
            if (dashboard.getFinesPanel() != null) {
                dashboard.getFinesPanel().refresh();
            }

            dialog.dispose();
            JOptionPane.showMessageDialog(dashboard, "Member \"" + member.getName() + "\" updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }
}
