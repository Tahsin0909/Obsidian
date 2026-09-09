package com.library.ui;

import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Fine;
import com.library.model.Member;
import com.library.store.DataStore;
import com.library.util.PasswordUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modern Member/Student Dashboard for the Library Management System.
 * Demonstrates: Polymorphism (receives Member subtype of User), encapsulation,
 * personalized member views, fine tracking & settlement, profile management, and clean integration with DataStore.
 */
public class MemberDashboard extends JFrame {

    private final Member member;
    private final DataStore store = DataStore.getInstance();

    // Table Models
    private DefaultTableModel myBorrowingsModel;
    private DefaultTableModel catalogModel;
    private DefaultTableModel finesTableModel;

    // Components for Header
    private JLabel headerTitleLabel;
    private JLabel headerSubtitleLabel;
    private JLabel userBadgeNameLabel;

    // Components for KPI Cards
    private JLabel kpiBorrowedVal;
    private JLabel kpiAvailableVal;
    private JLabel kpiUnpaidFinesVal;
    private JLabel kpiStatusVal;
    private JLabel kpiMemberSinceVal;

    // Fines Tab Components
    private JTable finesTable;
    private JComboBox<String> fineFilterCombo;
    private JTextField fineSearchField;
    private JLabel totalFinesAssessedLabel;
    private JLabel totalFinesUnpaidLabel;
    private JLabel totalFinesPaidLabel;

    // Profile Tab Components
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

    // Main Tabs
    private JTabbedPane mainTabs;

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

        mainTabs = createTabbedContentPanel();
        centerPanel.add(mainTabs, BorderLayout.CENTER);

        root.add(centerPanel, BorderLayout.CENTER);

        setContentPane(root);
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
        kpiStatusVal = new JLabel(member.getStatus().name());
        kpiMemberSinceVal = new JLabel(member.getMembershipDate() != null ? member.getMembershipDate().toString() : "-");

        container.add(createKpiCard("My Borrowed Books", kpiBorrowedVal, "📚", UITheme.PRIMARY));
        container.add(createKpiCard("Available in Library", kpiAvailableVal, "📖", UITheme.ACCENT));
        container.add(createKpiCard("Unpaid Fines", kpiUnpaidFinesVal, "💰", UITheme.DANGER));
        container.add(createKpiCard("Membership Status", kpiStatusVal, "✨", UITheme.PRIMARY_LIGHT));
        container.add(createKpiCard("Member Since", kpiMemberSinceVal, "📅", UITheme.WARNING));

        refreshKpiCards();
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

    private void refreshKpiCards() {
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

    // ============================================================================
    // 3. TABBED CONTENT CONTAINER
    // ============================================================================

    private JTabbedPane createTabbedContentPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("My Borrowed Books", createMyBorrowingsPanel());
        tabs.addTab("Browse Catalog", createCatalogPanel());
        tabs.addTab("My Fines & Fees", createFinesPanel());
        tabs.addTab("My Profile", createProfilePanel());

        return tabs;
    }

    // ============================================================================
    // TAB 1: MY BORROWED BOOKS
    // ============================================================================

    private JPanel createMyBorrowingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Borrow ID", "Book Title", "Author", "Borrow Date", "Due Date", "Days Remaining / Status", "Fine Status"};
        myBorrowingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(myBorrowingsModel);
        table.setFillsViewportHeight(true);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(28);
        UITheme.styleTableHeader(table.getTableHeader());

        centerAlignColumns(table, 0, 3, 4, 5, 6);
        setColumnWidths(table, 70, 180, 130, 90, 90, 140, 110);

        // Highlight Fine Status Column
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String text = value != null ? value.toString() : "";
                if (text.startsWith("UNPAID")) {
                    l.setForeground(UITheme.DANGER);
                    l.setFont(UITheme.FONT_BODY_BOLD);
                } else if (text.startsWith("PAID")) {
                    l.setForeground(new Color(0x1B, 0x7A, 0x4B));
                    l.setFont(UITheme.FONT_BODY_BOLD);
                } else {
                    l.setForeground(UITheme.TEXT_MUTED);
                    l.setFont(UITheme.FONT_BODY);
                }
                return l;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        JButton viewFinesTabBtn = UITheme.accentButton("View My Fines Tab →");
        viewFinesTabBtn.addActionListener(e -> {
            if (mainTabs != null) {
                mainTabs.setSelectedIndex(2); // Jump to Fines Tab
            }
        });

        bottomBar.add(viewFinesTabBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomBar, BorderLayout.SOUTH);

        refreshMyBorrowingsTable();
        return panel;
    }

    private void refreshMyBorrowingsTable() {
        if (myBorrowingsModel == null) return;
        myBorrowingsModel.setRowCount(0);
        List<Borrowing> list = store.borrowings().stream()
                .filter(b -> b.getMemberId() == member.getMemberId())
                .collect(Collectors.toList());

        for (Borrowing b : list) {
            Book book = store.findBookById(b.getBookId());

            String bookTitle = book != null ? book.getTitle() : "Book #" + b.getBookId();
            String bookAuthor = book != null ? book.getAuthor() : "-";

            String statusStr;
            if (b.isOverdue()) {
                statusStr = "OVERDUE (Due: " + b.getDueDate() + ")";
            } else if (b.getStatus() == Borrowing.Status.ACTIVE) {
                long days = ChronoUnit.DAYS.between(LocalDate.now(), b.getDueDate());
                statusStr = days + " day(s) left";
            } else {
                statusStr = b.getStatus().name();
            }

            Fine fine = store.findFineByBorrowingId(b.getBorrowingId());
            String fineStr = "No Fine";
            if (fine != null) {
                fineStr = (fine.isPaid() ? "PAID ($" : "UNPAID ($") + String.format("%.2f", fine.getAmount()) + ")";
            }

            myBorrowingsModel.addRow(new Object[]{
                    b.getBorrowingId(),
                    bookTitle,
                    bookAuthor,
                    b.getBorrowDate(),
                    b.getDueDate(),
                    statusStr,
                    fineStr
            });
        }
    }

    // ============================================================================
    // TAB 2: BROWSE CATALOG
    // ============================================================================

    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Search Bar Top
        JPanel filterRow = new JPanel(new BorderLayout(8, 0));
        filterRow.setOpaque(false);

        JLabel searchLbl = new JLabel("Search Books:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(280, 32));

        filterRow.add(searchLbl, BorderLayout.WEST);
        filterRow.add(searchField, BorderLayout.CENTER);

        String[] cols = {"ISBN", "Title", "Author", "Category", "Publisher", "Year", "Available Copies"};
        catalogModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Book b : store.books()) {
            catalogModel.addRow(new Object[]{
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategoryName(),
                    b.getPublisher(),
                    b.getPublicationYear(),
                    b.getAvailableQuantity() > 0 ? (b.getAvailableQuantity() + " available") : "Out of Stock"
            });
        }

        JTable table = new JTable(catalogModel);
        table.setFillsViewportHeight(true);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(28);
        UITheme.styleTableHeader(table.getTableHeader());

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(catalogModel);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        centerAlignColumns(table, 0, 5, 6);
        setColumnWidths(table, 100, 200, 150, 100, 100, 50, 100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        panel.add(filterRow, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ============================================================================
    // TAB 3: MY FINES & FEES (REFERENCING ADMIN FINE MANAGEMENT)
    // ============================================================================

    private JPanel createFinesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // 1. Top Summary Metric Strip
        JPanel summaryStrip = createFineSummaryStrip();

        // 2. Responsive Toolbar: Search, Status Filter & Action Buttons
        JPanel toolbarPanel = new JPanel(new BorderLayout(8, 0));
        toolbarPanel.setOpaque(false);
        toolbarPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterControls.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        fineSearchField = new JTextField();
        fineSearchField.setPreferredSize(new Dimension(160, 28));

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(UITheme.FONT_BODY_BOLD);
        String[] statusFilters = {"All Fines", "Unpaid Fines Only", "Paid Fines Only"};
        fineFilterCombo = new JComboBox<>(statusFilters);
        fineFilterCombo.setFont(UITheme.FONT_BODY);
        fineFilterCombo.setPreferredSize(new Dimension(145, 28));
        fineFilterCombo.addActionListener(e -> refreshFinesTable());

        filterControls.add(searchLbl);
        filterControls.add(fineSearchField);
        filterControls.add(Box.createHorizontalStrut(6));
        filterControls.add(statusLbl);
        filterControls.add(fineFilterCombo);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionButtons.setOpaque(false);

        JButton payFineBtn = UITheme.accentButton("Pay Selected Fine");
        payFineBtn.addActionListener(e -> onPaySelectedFineClicked());

        JButton payAllBtn = UITheme.primaryButton("Pay All Unpaid Fines");
        payAllBtn.addActionListener(e -> onPayAllFinesClicked());

        JButton viewDetailsBtn = UITheme.secondaryButton("View Details / Receipt");
        viewDetailsBtn.addActionListener(e -> onViewFineDetailsClicked());

        actionButtons.add(payFineBtn);
        actionButtons.add(payAllBtn);
        actionButtons.add(viewDetailsBtn);

        toolbarPanel.add(filterControls, BorderLayout.WEST);
        toolbarPanel.add(actionButtons, BorderLayout.EAST);

        // 3. Fines Table
        String[] cols = {"Fine ID", "Borrow ID", "Book Title", "Overdue Days", "Fine Amount ($)", "Assessed Date", "Paid Date", "Payment Method", "Status"};
        finesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        finesTable = new JTable(finesTableModel);
        finesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        finesTable.setFillsViewportHeight(true);
        finesTable.setFont(UITheme.FONT_BODY);
        finesTable.setRowHeight(28);
        finesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UITheme.styleTableHeader(finesTable.getTableHeader());

        // Custom Renderer for Status (PAID green, UNPAID red)
        finesTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String valStr = value != null ? value.toString() : "";
                if ("PAID".equalsIgnoreCase(valStr)) {
                    l.setForeground(new Color(0x1B, 0x7A, 0x4B));
                    l.setFont(UITheme.FONT_BODY_BOLD);
                } else if ("UNPAID".equalsIgnoreCase(valStr)) {
                    l.setForeground(UITheme.DANGER);
                    l.setFont(UITheme.FONT_BODY_BOLD);
                } else {
                    l.setForeground(UITheme.TEXT_DARK);
                }
                return l;
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(finesTableModel);
        finesTable.setRowSorter(sorter);

        // Double click listener to pay or view details
        finesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && finesTable.getSelectedRow() != -1) {
                    Fine selected = getSelectedFine();
                    if (selected != null && !selected.isPaid()) {
                        showProcessPaymentDialog(selected);
                    } else if (selected != null) {
                        showFineDetailsDialog(selected);
                    }
                }
            }
        });

        fineSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = fineSearchField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        centerAlignColumns(finesTable, 0, 1, 3, 4, 5, 6, 7, 8);
        setColumnWidths(finesTable, 60, 65, 200, 90, 95, 90, 90, 110, 80);

        JScrollPane scrollPane = new JScrollPane(finesTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(toolbarPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(summaryStrip, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        refreshFinesTable();
        return panel;
    }

    private JPanel createFineSummaryStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 14, 0));
        strip.setOpaque(false);
        strip.setBorder(new EmptyBorder(0, 0, 4, 0));

        totalFinesAssessedLabel = new JLabel("$0.00");
        totalFinesUnpaidLabel = new JLabel("$0.00 (0 unpaid)");
        totalFinesPaidLabel = new JLabel("$0.00 (0 paid)");

        strip.add(createMiniSummaryCard("Total Fines Assessed", totalFinesAssessedLabel, UITheme.PRIMARY));
        strip.add(createMiniSummaryCard("Outstanding Balance / Unpaid", totalFinesUnpaidLabel, UITheme.DANGER));
        strip.add(createMiniSummaryCard("Total Settled / Paid", totalFinesPaidLabel, UITheme.ACCENT));

        return strip;
    }

    private JPanel createMiniSummaryCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(4, 2));
        card.setBackground(new Color(0xFA, 0xFB, 0xFC));
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_HEADING);
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void refreshFinesTable() {
        if (finesTableModel == null) return;
        finesTableModel.setRowCount(0);

        String statusFilter = fineFilterCombo != null ? (String) fineFilterCombo.getSelectedItem() : "All Fines";

        List<Fine> myFines = store.findFinesByMemberId(member.getMemberId());
        double totalAssessed = 0.0;
        double totalUnpaid = 0.0;
        double totalPaid = 0.0;
        int unpaidCount = 0;
        int paidCount = 0;

        for (Fine f : myFines) {
            totalAssessed += f.getAmount();
            if (f.isPaid()) {
                totalPaid += f.getAmount();
                paidCount++;
            } else {
                totalUnpaid += f.getAmount();
                unpaidCount++;
            }

            // Apply status filter
            if ("Unpaid Fines Only".equals(statusFilter) && f.isPaid()) continue;
            if ("Paid Fines Only".equals(statusFilter) && !f.isPaid()) continue;

            Borrowing borrowing = null;
            for (Borrowing b : store.borrowings()) {
                if (b.getBorrowingId() == f.getBorrowingId()) {
                    borrowing = b;
                    break;
                }
            }

            String bookTitle = "-";
            long overdueDays = 0;
            if (borrowing != null) {
                Book book = store.findBookById(borrowing.getBookId());
                bookTitle = book != null ? book.getTitle() : "Book #" + borrowing.getBookId();
                overdueDays = store.calculateOverdueDays(borrowing);
            }

            String statusStr = f.isPaid() ? "PAID" : "UNPAID";
            String paidDateStr = f.getPaidDate() != null ? f.getPaidDate().toString() : "-";
            String methodStr = f.getPaymentMethod() != null && !f.getPaymentMethod().isEmpty() ? f.getPaymentMethod() : (f.isPaid() ? "Cash" : "-");

            finesTableModel.addRow(new Object[]{
                    f.getFineId(),
                    f.getBorrowingId(),
                    bookTitle,
                    overdueDays > 0 ? overdueDays + " days" : "-",
                    String.format("$%.2f", f.getAmount()),
                    f.getFineDate() != null ? f.getFineDate().toString() : "-",
                    paidDateStr,
                    methodStr,
                    statusStr
            });
        }

        if (totalFinesAssessedLabel != null) {
            totalFinesAssessedLabel.setText(String.format("$%.2f (%d total)", totalAssessed, myFines.size()));
        }
        if (totalFinesUnpaidLabel != null) {
            totalFinesUnpaidLabel.setText(String.format("$%.2f (%d unpaid)", totalUnpaid, unpaidCount));
        }
        if (totalFinesPaidLabel != null) {
            totalFinesPaidLabel.setText(String.format("$%.2f (%d paid)", totalPaid, paidCount));
        }

        refreshKpiCards();
    }

    private Fine getSelectedFine() {
        if (finesTable == null) return null;
        int selectedRow = finesTable.getSelectedRow();
        if (selectedRow == -1) return null;
        int modelRow = finesTable.convertRowIndexToModel(selectedRow);
        int fineId = (Integer) finesTableModel.getValueAt(modelRow, 0);
        return store.findFineById(fineId);
    }

    private void onPaySelectedFineClicked() {
        Fine fine = getSelectedFine();
        if (fine == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a fine from the table to pay.",
                    "No Fine Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (fine.isPaid()) {
            JOptionPane.showMessageDialog(this,
                    "Fine #" + fine.getFineId() + " has already been settled and paid on " + fine.getPaidDate() + ".\n" +
                    "Payment Method: " + (fine.getPaymentMethod() != null ? fine.getPaymentMethod() : "Cash"),
                    "Fine Already Paid",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        showProcessPaymentDialog(fine);
    }

    private void showProcessPaymentDialog(Fine fine) {
        JDialog dialog = new JDialog(this, "Pay Fine (Fine #" + fine.getFineId() + ")", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        JLabel heading = new JLabel("Pay Outstanding Library Fine");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Select your preferred payment method to clear this overdue penalty");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);

        Borrowing borrowing = null;
        for (Borrowing b : store.borrowings()) {
            if (b.getBorrowingId() == fine.getBorrowingId()) {
                borrowing = b;
                break;
            }
        }
        Book book = borrowing != null ? store.findBookById(borrowing.getBookId()) : null;
        String bookTitle = book != null ? book.getTitle() : (borrowing != null ? "Book #" + borrowing.getBookId() : "-");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel fineIdVal = new JLabel("#" + fine.getFineId() + " (Borrow ID: #" + fine.getBorrowingId() + ")");
        fineIdVal.setFont(UITheme.FONT_BODY_BOLD);

        JLabel bookVal = new JLabel(bookTitle);
        bookVal.setFont(UITheme.FONT_BODY);

        JLabel amountVal = new JLabel(String.format("$%.2f", fine.getAmount()));
        amountVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountVal.setForeground(new Color(0x1B, 0x7A, 0x4B));

        JLabel assessedDateVal = new JLabel(fine.getFineDate() != null ? fine.getFineDate().toString() : "-");
        assessedDateVal.setFont(UITheme.FONT_BODY);

        String[] methods = {"Credit/Debit Card", "Online Banking / Transfer", "Mobile Payment (bKash/Nagad)", "Cash at Front Desk", "Library Credit"};
        JComboBox<String> methodCombo = new JComboBox<>(methods);
        methodCombo.setFont(UITheme.FONT_BODY);

        JTextField notesField = new JTextField();

        addFormField(form, gbc, 0, "Fine Reference:", fineIdVal);
        addFormField(form, gbc, 1, "Book Title:", bookVal);
        addFormField(form, gbc, 2, "Assessed Date:", assessedDateVal);
        addFormField(form, gbc, 3, "Amount Payable:", amountVal);
        addFormField(form, gbc, 4, "Payment Method:", methodCombo);
        addFormField(form, gbc, 5, "Payment Notes / Ref:", notesField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton confirmPayBtn = UITheme.accentButton("Confirm & Pay Now");
        confirmPayBtn.addActionListener(e -> {
            String selectedMethod = (String) methodCombo.getSelectedItem();
            String notes = notesField.getText().trim();

            fine.setPaid(true);
            fine.setPaidDate(LocalDate.now());
            fine.setPaymentMethod(selectedMethod);
            if (!notes.isEmpty()) {
                fine.setNotes(notes);
            }

            store.updateFine(fine);
            refreshFinesTable();
            refreshMyBorrowingsTable();
            dialog.dispose();

            JOptionPane.showMessageDialog(this,
                    "Payment of $" + String.format("%.2f", fine.getAmount()) + " completed successfully!\n\n" +
                    "Transaction Receipt:\n" +
                    "• Fine ID: #" + fine.getFineId() + "\n" +
                    "• Member: " + member.getName() + "\n" +
                    "• Payment Method: " + selectedMethod + "\n" +
                    "• Date: " + LocalDate.now() + "\n" +
                    "• Status: PAID IN FULL",
                    "Payment Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(confirmPayBtn);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void onPayAllFinesClicked() {
        List<Fine> myFines = store.findFinesByMemberId(member.getMemberId());
        long unpaidCount = myFines.stream().filter(f -> !f.isPaid()).count();
        if (unpaidCount == 0) {
            JOptionPane.showMessageDialog(this,
                    "You have no unpaid fines! Your library account is in good standing.",
                    "Zero Outstanding Balance",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double unpaidSum = myFines.stream().filter(f -> !f.isPaid()).mapToDouble(Fine::getAmount).sum();
        String[] methods = {"Credit/Debit Card", "Online Banking / Transfer", "Mobile Payment (bKash/Nagad)", "Cash at Front Desk", "Library Credit"};
        String method = (String) JOptionPane.showInputDialog(this,
                "Settle all " + unpaidCount + " unpaid fine(s).\n" +
                "Total Amount Due: $" + String.format("%.2f", unpaidSum) + "\n\n" +
                "Select Payment Method:",
                "Pay All Outstanding Fines",
                JOptionPane.QUESTION_MESSAGE,
                null,
                methods,
                methods[0]);

        if (method != null) {
            int settled = store.payAllFinesForMember(member.getMemberId(), method);
            refreshFinesTable();
            refreshMyBorrowingsTable();
            JOptionPane.showMessageDialog(this,
                    "Successfully settled " + settled + " fine(s)!\n" +
                    "Total Paid: $" + String.format("%.2f", unpaidSum) + "\n" +
                    "Payment Method: " + method + "\n" +
                    "All fines are now marked as PAID.",
                    "All Fines Settled",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onViewFineDetailsClicked() {
        Fine fine = getSelectedFine();
        if (fine == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a fine from the table to view details.",
                    "No Fine Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        showFineDetailsDialog(fine);
    }

    private void showFineDetailsDialog(Fine fine) {
        JDialog dialog = new JDialog(this, "Fine Details & Receipt (Fine #" + fine.getFineId() + ")", true);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        JLabel heading = new JLabel("Library Fine Assessment Receipt");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Detailed statement and transaction history for Fine #" + fine.getFineId());
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);

        Borrowing borrowing = null;
        for (Borrowing b : store.borrowings()) {
            if (b.getBorrowingId() == fine.getBorrowingId()) {
                borrowing = b;
                break;
            }
        }
        Book book = borrowing != null ? store.findBookById(borrowing.getBookId()) : null;
        String bookTitle = book != null ? book.getTitle() : (borrowing != null ? "Book #" + borrowing.getBookId() : "-");
        long overdueDays = borrowing != null ? store.calculateOverdueDays(borrowing) : 0;

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel fineIdVal = new JLabel("#" + fine.getFineId());
        fineIdVal.setFont(UITheme.FONT_BODY_BOLD);

        JLabel memberVal = new JLabel(member.getName() + " (#" + member.getMemberId() + ")");
        memberVal.setFont(UITheme.FONT_BODY);

        JLabel loanVal = new JLabel("Borrowing #" + fine.getBorrowingId() + " (" + bookTitle + ")");
        loanVal.setFont(UITheme.FONT_BODY);

        JLabel overdueVal = new JLabel(overdueDays > 0 ? overdueDays + " day(s)" : "-");
        overdueVal.setFont(UITheme.FONT_BODY);

        JLabel amountVal = new JLabel(String.format("$%.2f", fine.getAmount()));
        amountVal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        amountVal.setForeground(fine.isPaid() ? new Color(0x1B, 0x7A, 0x4B) : UITheme.DANGER);

        JLabel statusVal = new JLabel(fine.isPaid() ? "PAID" : "UNPAID");
        statusVal.setFont(UITheme.FONT_BODY_BOLD);
        statusVal.setForeground(fine.isPaid() ? new Color(0x1B, 0x7A, 0x4B) : UITheme.DANGER);

        JLabel fineDateVal = new JLabel(fine.getFineDate() != null ? fine.getFineDate().toString() : "-");
        fineDateVal.setFont(UITheme.FONT_BODY);

        JLabel paidDateVal = new JLabel(fine.getPaidDate() != null ? fine.getPaidDate().toString() : "-");
        paidDateVal.setFont(UITheme.FONT_BODY);

        JLabel methodVal = new JLabel(fine.getPaymentMethod() != null && !fine.getPaymentMethod().isEmpty() ? fine.getPaymentMethod() : "-");
        methodVal.setFont(UITheme.FONT_BODY);

        JLabel notesVal = new JLabel(fine.getNotes() != null && !fine.getNotes().isEmpty() ? fine.getNotes() : "None");
        notesVal.setFont(UITheme.FONT_BODY);

        addFormField(form, gbc, 0, "Fine ID:", fineIdVal);
        addFormField(form, gbc, 1, "Member:", memberVal);
        addFormField(form, gbc, 2, "Loan / Book:", loanVal);
        addFormField(form, gbc, 3, "Overdue Duration:", overdueVal);
        addFormField(form, gbc, 4, "Fine Amount:", amountVal);
        addFormField(form, gbc, 5, "Status:", statusVal);
        addFormField(form, gbc, 6, "Assessed Date:", fineDateVal);
        addFormField(form, gbc, 7, "Paid Date:", paidDateVal);
        addFormField(form, gbc, 8, "Payment Method:", methodVal);
        addFormField(form, gbc, 9, "Notes:", notesVal);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton closeBtn = UITheme.secondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        if (!fine.isPaid()) {
            JButton payBtn = UITheme.accentButton("Pay Now");
            payBtn.addActionListener(e -> {
                dialog.dispose();
                showProcessPaymentDialog(fine);
            });
            btnPanel.add(payBtn);
        }

        btnPanel.add(closeBtn);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // ============================================================================
    // TAB 4: MY PROFILE (WITH PROFILE & PASSWORD UPDATE FEATURES)
    // ============================================================================

    private JComponent createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

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

        panel.add(bannerCard, BorderLayout.NORTH);
        panel.add(formsContainer, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
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

        addFormField(form, gbc, 0, "Member ID:", memberIdField);
        addFormField(form, gbc, 1, "Full Name *:", profileNameField);
        addFormField(form, gbc, 2, "Username *:", profileUsernameField);
        addFormField(form, gbc, 3, "Email Address *:", profileEmailField);
        addFormField(form, gbc, 4, "Phone Number:", profilePhoneField);
        addFormField(form, gbc, 5, "Address:", profileAddressField);
        addFormField(form, gbc, 6, "Member Since:", membershipDateField);
        addFormField(form, gbc, 7, "Account Status:", statusField);

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

        addFormField(form, gbc, 0, "Current Password *:", currentPasswordField);
        addFormField(form, gbc, 1, "New Password *:", newPasswordField);
        addFormField(form, gbc, 2, "Confirm New Password *:", confirmPasswordField);
        addFormField(form, gbc, 3, "", infoLabel);

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

    private void resetProfileFields() {
        if (profileNameField != null) profileNameField.setText(member.getName());
        if (profileUsernameField != null) profileUsernameField.setText(member.getUsername());
        if (profileEmailField != null) profileEmailField.setText(member.getEmail());
        if (profilePhoneField != null) profilePhoneField.setText(member.getPhone() != null ? member.getPhone() : "");
        if (profileAddressField != null) profileAddressField.setText(member.getAddress() != null ? member.getAddress() : "");
    }

    private void saveProfileChanges() {
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

        // Update Header and Badge
        if (headerSubtitleLabel != null) {
            headerSubtitleLabel.setText("Welcome back, " + member.getName());
        }
        if (userBadgeNameLabel != null) {
            userBadgeNameLabel.setText("Member ID #" + member.getMemberId() + " (" + member.getEmail() + ")");
        }
        if (profileHeaderNameLabel != null) {
            profileHeaderNameLabel.setText(member.getName());
        }
        if (profileHeaderSubLabel != null) {
            profileHeaderSubLabel.setText("Member ID #" + member.getMemberId() + "  •  " + member.getEmail() + "  •  Status: " + member.getStatus());
        }

        // Refresh borrowings and fines tables in case member info is displayed
        refreshMyBorrowingsTable();
        refreshFinesTable();

        JOptionPane.showMessageDialog(this,
                "Profile updated successfully!\nYour new details have been saved.",
                "Profile Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updatePassword() {
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

    // ============================================================================
    // UI HELPERS
    // ============================================================================

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.32;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.68;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        field.setFont(UITheme.FONT_BODY);
        if (field instanceof JTextField || field instanceof JComboBox) {
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
            field.setMinimumSize(new Dimension(50, 30));
        }
        panel.add(field, gbc);
    }

    private void centerAlignColumns(JTable table, int... columnIndices) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int idx : columnIndices) {
            if (idx < table.getColumnCount()) {
                table.getColumnModel().getColumn(idx).setCellRenderer(centerRenderer);
            }
        }
    }

    private void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
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
