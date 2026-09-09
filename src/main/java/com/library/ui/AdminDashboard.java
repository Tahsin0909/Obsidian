package com.library.ui;

import com.library.model.Admin;
import com.library.model.Borrowing;
import com.library.store.DataStore;
import com.library.ui.admin.AdminBooksPanel;
import com.library.ui.admin.AdminBorrowingsPanel;
import com.library.ui.admin.AdminFinesPanel;
import com.library.ui.admin.AdminMembersPanel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern, Responsive Administrator Dashboard for the Library Management System.
 * Coordinates system-wide administration, KPI tracking, and modular management panels.
 */
public class AdminDashboard extends JFrame {

    private final Admin admin;
    private final DataStore store = DataStore.getInstance();

    // Modular Admin Panels
    private AdminBooksPanel booksPanel;
    private AdminMembersPanel membersPanel;
    private AdminBorrowingsPanel borrowingsPanel;
    private AdminFinesPanel finesPanel;

    // KPI Labels
    private JLabel totalBooksValueLabel;
    private JLabel totalMembersValueLabel;
    private JLabel activeBorrowingsValueLabel;
    private JLabel outstandingFinesKpiLabel;

    // Navigation Tabs
    private JTabbedPane tabs;

    public AdminDashboard(Admin admin) {
        this.admin = admin;

        setTitle("Library Management System - " + admin.getDashboardTitle());
        setSize(1080, 720);
        setMinimumSize(new Dimension(800, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(14, 18, 18, 18));

        // 1. Header with Title, Admin Badge, and Logout
        root.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center content: KPI Cards + Tabbed Panels
        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setOpaque(false);
        centerPanel.add(createKpiPanel(), BorderLayout.NORTH);
        centerPanel.add(createTabbedContentPanel(), BorderLayout.CENTER);

        root.add(centerPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        // Title and subtitle
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel titleLabel = new JLabel(admin.getDashboardTitle());
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_DARK);

        JLabel subtitleLabel = new JLabel("System Administration & Operations Console");
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(titleLabel);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(subtitleLabel);

        header.add(titleBox, BorderLayout.WEST);

        // Admin badge & Logout button
        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsBox.setOpaque(false);

        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        userBadge.setBackground(Color.WHITE);
        userBadge.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(2, 8, 2, 8)));

        JLabel userName = new JLabel(admin.getFullName() + " (" + admin.getEmail() + ")");
        userName.setFont(UITheme.FONT_BODY_BOLD);
        userName.setForeground(UITheme.PRIMARY);

        userBadge.add(userName);

        JButton logoutBtn = UITheme.secondaryButton("Logout");
        logoutBtn.addActionListener(e -> logout());

        actionsBox.add(userBadge);
        actionsBox.add(logoutBtn);

        header.add(actionsBox, BorderLayout.EAST);
        return header;
    }

    private JPanel createKpiPanel() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiPanel.setOpaque(false);

        totalBooksValueLabel = new JLabel("0");
        totalMembersValueLabel = new JLabel("0");
        activeBorrowingsValueLabel = new JLabel("0");
        outstandingFinesKpiLabel = new JLabel("$0.00");

        kpiPanel.add(createKpiCard("Total Books", totalBooksValueLabel, UITheme.PRIMARY));
        kpiPanel.add(createKpiCard("Registered Members", totalMembersValueLabel, UITheme.ACCENT));
        kpiPanel.add(createKpiCard("Active Borrowings", activeBorrowingsValueLabel, UITheme.WARNING));
        kpiPanel.add(createKpiCard("Unpaid Fines", outstandingFinesKpiLabel, UITheme.DANGER));

        refreshKpis();
        return kpiPanel;
    }

    private JPanel createKpiCard(String label, JLabel valComp, Color accentColor) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UITheme.FONT_SMALL);
        labelComp.setForeground(UITheme.TEXT_MUTED);

        valComp.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valComp.setForeground(accentColor);

        card.add(labelComp, BorderLayout.NORTH);
        card.add(valComp, BorderLayout.SOUTH);

        return card;
    }

    private JTabbedPane createTabbedContentPanel() {
        tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.setBackground(Color.WHITE);

        booksPanel = new AdminBooksPanel(this);
        membersPanel = new AdminMembersPanel(this);
        borrowingsPanel = new AdminBorrowingsPanel(this);
        finesPanel = new AdminFinesPanel(this);

        tabs.addTab("Books Catalog", booksPanel);
        tabs.addTab("Members Directory", membersPanel);
        tabs.addTab("Borrowing Records", borrowingsPanel);
        tabs.addTab("Fine Management", finesPanel);

        return tabs;
    }

    /**
     * Recalculates and refreshes all top KPI metric counters.
     */
    public void refreshKpis() {
        int totalBooks = store.books().size();
        int totalMembers = store.members().size();
        int activeBorrowings = (int) store.borrowings().stream().filter(b -> b.getStatus() == Borrowing.Status.ACTIVE).count();
        double outstandingFines = store.getTotalOutstandingFines();

        if (totalBooksValueLabel != null) {
            totalBooksValueLabel.setText(String.valueOf(totalBooks));
        }
        if (totalMembersValueLabel != null) {
            totalMembersValueLabel.setText(String.valueOf(totalMembers));
        }
        if (activeBorrowingsValueLabel != null) {
            activeBorrowingsValueLabel.setText(String.valueOf(activeBorrowings));
        }
        if (outstandingFinesKpiLabel != null) {
            outstandingFinesKpiLabel.setText(String.format("$%.2f", outstandingFines));
        }
    }

    /**
     * Refreshes all tabs and KPIs across the entire dashboard.
     */
    public void refreshAll() {
        refreshKpis();
        if (booksPanel != null) booksPanel.refresh();
        if (membersPanel != null) membersPanel.refresh();
        if (borrowingsPanel != null) borrowingsPanel.refresh();
        if (finesPanel != null) finesPanel.refresh();
    }

    public AdminBooksPanel getBooksPanel() {
        return booksPanel;
    }

    public AdminMembersPanel getMembersPanel() {
        return membersPanel;
    }

    public AdminBorrowingsPanel getBorrowingsPanel() {
        return borrowingsPanel;
    }

    public AdminFinesPanel getFinesPanel() {
        return finesPanel;
    }

    public void showFinesTab() {
        if (tabs != null) {
            tabs.setSelectedIndex(3);
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
