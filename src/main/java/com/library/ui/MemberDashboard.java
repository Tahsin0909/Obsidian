package com.library.ui;

import com.library.model.Borrowing;
import com.library.model.Fine;
import com.library.model.Member;
import com.library.store.DataStore;
import com.library.ui.member.MemberBorrowingsPanel;
import com.library.ui.member.MemberCatalogPanel;
import com.library.ui.member.MemberFinesPanel;
import com.library.ui.member.MemberProfilePanel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Modern Member/Student Dashboard coordinator for the Library Management System.
 * Coordinates personalized views, KPI metrics, tab switching, and modular sub-panels.
 */
public class MemberDashboard extends JFrame {

    public static final int TAB_BORROWINGS = 0;
    public static final int TAB_CATALOG = 1;
    public static final int TAB_FINES = 2;
    public static final int TAB_PROFILE = 3;

    private final Member member;
    private final DataStore store = DataStore.getInstance();

    // Header Components
    private JLabel headerTitleLabel;
    private JLabel headerSubtitleLabel;
    private JLabel userBadgeNameLabel;

    // KPI Metric Labels
    private JLabel kpiBorrowedVal;
    private JLabel kpiAvailableVal;
    private JLabel kpiUnpaidFinesVal;
    private JLabel kpiStatusVal;
    private JLabel kpiMemberSinceVal;

    // Main Tab Container & Modular Panels
    private JTabbedPane mainTabs;
    private MemberBorrowingsPanel borrowingsPanel;
    private MemberCatalogPanel catalogPanel;
    private MemberFinesPanel finesPanel;
    private MemberProfilePanel profilePanel;

    public MemberDashboard(Member member) {
        this.member = member;

        setTitle("Library Management System - " + member.getDashboardTitle());
        setSize(1100, 760);
        setMinimumSize(new Dimension(900, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(16, 24, 24, 24));

        // 1. Header with Member Greeting, Badge, and Logout
        root.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center content: KPI Cards + Tabbed Panels
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(createKpiPanel(), BorderLayout.NORTH);

        mainTabs = new JTabbedPane();
        mainTabs.setFont(UITheme.FONT_BODY_BOLD);
        mainTabs.setBackground(Color.WHITE);

        borrowingsPanel = new MemberBorrowingsPanel(this, member);
        catalogPanel = new MemberCatalogPanel();
        finesPanel = new MemberFinesPanel(this, member);
        profilePanel = new MemberProfilePanel(this, member);

        mainTabs.addTab("My Borrowed Books", borrowingsPanel);
        mainTabs.addTab("Browse Catalog", catalogPanel);
        mainTabs.addTab("My Fines & Fees", finesPanel);
        mainTabs.addTab("My Profile", profilePanel);

        centerPanel.add(mainTabs, BorderLayout.CENTER);
        root.add(centerPanel, BorderLayout.CENTER);

        setContentPane(root);
        refreshKpiCards();
    }

    // ============================================================================
    // 1. HEADER PANEL
    // ============================================================================

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Title and greeting
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        headerTitleLabel = new JLabel(member.getDashboardTitle());
        headerTitleLabel.setFont(UITheme.FONT_TITLE);
        headerTitleLabel.setForeground(UITheme.TEXT_DARK);

        headerSubtitleLabel = new JLabel("Welcome back, " + member.getName());
        headerSubtitleLabel.setFont(UITheme.FONT_BODY);
        headerSubtitleLabel.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(headerTitleLabel);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(headerSubtitleLabel);

        header.add(titleBox, BorderLayout.WEST);

        // Member Badge & Logout
        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsBox.setOpaque(false);

        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        userBadge.setBackground(Color.WHITE);
        userBadge.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(2, 10, 2, 10)
        ));

        JLabel userIcon = new JLabel("📖");
        userBadgeNameLabel = new JLabel("Member ID #" + member.getMemberId() + " (" + member.getEmail() + ")");
        userBadgeNameLabel.setFont(UITheme.FONT_BODY_BOLD);
        userBadgeNameLabel.setForeground(UITheme.PRIMARY);

        userBadge.add(userIcon);
        userBadge.add(userBadgeNameLabel);

        JButton logoutBtn = UITheme.secondaryButton("Logout");
        logoutBtn.addActionListener(e -> logout());

        actionsBox.add(userBadge);
        actionsBox.add(logoutBtn);

        header.add(actionsBox, BorderLayout.EAST);
        return header;
    }

    // ============================================================================
    // 2. KPI METRICS PANEL
    // ============================================================================

    private JPanel createKpiPanel() {
        JPanel container = new JPanel(new GridLayout(1, 5, 14, 0));
        container.setOpaque(false);

        kpiBorrowedVal = new JLabel("0");
        kpiAvailableVal = new JLabel("0");
        kpiUnpaidFinesVal = new JLabel("$0.00");
        kpiStatusVal = new JLabel(member.getStatus() != null ? member.getStatus().name() : "ACTIVE");
        kpiMemberSinceVal = new JLabel(member.getMembershipDate() != null ? member.getMembershipDate().toString() : "-");

        container.add(createKpiCard("My Borrowed Books", kpiBorrowedVal, "📚", UITheme.PRIMARY));
        container.add(createKpiCard("Available in Library", kpiAvailableVal, "📖", UITheme.ACCENT));
        container.add(createKpiCard("Unpaid Fines", kpiUnpaidFinesVal, "💰", UITheme.DANGER));
        container.add(createKpiCard("Membership Status", kpiStatusVal, "✨", UITheme.PRIMARY_LIGHT));
        container.add(createKpiCard("Member Since", kpiMemberSinceVal, "📅", UITheme.WARNING));

        return container;
    }

    private JPanel createKpiCard(String label, JLabel valComp, String icon, Color accentColor) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UITheme.FONT_BODY);
        labelComp.setForeground(UITheme.TEXT_MUTED);

        JLabel iconComp = new JLabel(icon);
        iconComp.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        topRow.add(labelComp, BorderLayout.WEST);
        topRow.add(iconComp, BorderLayout.EAST);

        valComp.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valComp.setForeground(accentColor);

        card.add(topRow, BorderLayout.NORTH);
        card.add(valComp, BorderLayout.SOUTH);

        return card;
    }

    public void refreshKpiCards() {
        // Active borrowings
        long myActiveCount = store.borrowings().stream()
                .filter(b -> b.getMemberId() == member.getMemberId() && b.getStatus() == Borrowing.Status.ACTIVE)
                .count();

        // Available library books
        long availableBooksCount = store.books().stream()
                .filter(b -> b.getAvailableQuantity() > 0)
                .count();

        // Unpaid fines for member
        List<Fine> myFines = store.findFinesByMemberId(member.getMemberId());
        double unpaidSum = 0.0;
        int unpaidCount = 0;
        for (Fine f : myFines) {
            if (!f.isPaid()) {
                unpaidSum += f.getAmount();
                unpaidCount++;
            }
        }

        if (kpiBorrowedVal != null) kpiBorrowedVal.setText(String.valueOf(myActiveCount));
        if (kpiAvailableVal != null) kpiAvailableVal.setText(String.valueOf(availableBooksCount));
        if (kpiUnpaidFinesVal != null) {
            kpiUnpaidFinesVal.setText(String.format("$%.2f (%d)", unpaidSum, unpaidCount));
            kpiUnpaidFinesVal.setForeground(unpaidCount > 0 ? UITheme.DANGER : UITheme.ACCENT);
        }
        if (kpiStatusVal != null) kpiStatusVal.setText(member.getStatus() != null ? member.getStatus().name() : "ACTIVE");
        if (kpiMemberSinceVal != null) kpiMemberSinceVal.setText(member.getMembershipDate() != null ? member.getMembershipDate().toString() : "-");
    }

    public void updateHeaderInfo() {
        setTitle("Library Management System - " + member.getDashboardTitle());
        if (headerTitleLabel != null) {
            headerTitleLabel.setText(member.getDashboardTitle());
        }
        if (headerSubtitleLabel != null) {
            headerSubtitleLabel.setText("Welcome back, " + member.getName());
        }
        if (userBadgeNameLabel != null) {
            userBadgeNameLabel.setText("Member ID #" + member.getMemberId() + " (" + member.getEmail() + ")");
        }
    }

    public void switchTab(int index) {
        if (mainTabs != null && index >= 0 && index < mainTabs.getTabCount()) {
            mainTabs.setSelectedIndex(index);
        }
    }

    public void refreshAll() {
        refreshKpiCards();
        if (borrowingsPanel != null) borrowingsPanel.refreshTable();
        if (catalogPanel != null) catalogPanel.refreshTable();
        if (finesPanel != null) finesPanel.refreshTable();
    }

    public Member getMember() {
        return member;
    }

    public JTabbedPane getMainTabs() {
        return mainTabs;
    }

    public MemberBorrowingsPanel getBorrowingsPanel() {
        return borrowingsPanel;
    }

    public MemberCatalogPanel getCatalogPanel() {
        return catalogPanel;
    }

    public MemberFinesPanel getFinesPanel() {
        return finesPanel;
    }

    public MemberProfilePanel getProfilePanel() {
        return profilePanel;
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
