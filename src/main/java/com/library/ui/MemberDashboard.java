package com.library.ui;

import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Member;
import com.library.store.DataStore;

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
 * personalized member views, and clean integration with DataStore.
 */
public class MemberDashboard extends JFrame {

    private final Member member;
    private final DataStore store = DataStore.getInstance();

    private DefaultTableModel myBorrowingsModel;
    private DefaultTableModel catalogModel;

    public MemberDashboard(Member member) {
        this.member = member;

        setTitle("Library Management System - " + member.getDashboardTitle());
        setSize(1060, 720);
        setMinimumSize(new Dimension(850, 600));
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
        centerPanel.add(createTabbedContentPanel(), BorderLayout.CENTER);

        root.add(centerPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Title and greeting
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel titleLabel = new JLabel(member.getDashboardTitle());
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_DARK);

        JLabel subtitleLabel = new JLabel("Welcome back, " + member.getName());
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(titleLabel);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitleLabel);

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
        JLabel userName = new JLabel("Member ID #" + member.getMemberId() + " (" + member.getEmail() + ")");
        userName.setFont(UITheme.FONT_BODY_BOLD);
        userName.setForeground(UITheme.PRIMARY);

        userBadge.add(userIcon);
        userBadge.add(userName);

        JButton logoutBtn = UITheme.secondaryButton("Logout");
        logoutBtn.addActionListener(e -> logout());

        actionsBox.add(userBadge);
        actionsBox.add(logoutBtn);

        header.add(actionsBox, BorderLayout.EAST);
        return header;
    }

    private JPanel createKpiPanel() {
        JPanel container = new JPanel(new GridLayout(1, 4, 16, 0));
        container.setOpaque(false);

        List<Borrowing> myActiveBorrowings = store.borrowings().stream()
                .filter(b -> b.getMemberId() == member.getMemberId() && b.getStatus() == Borrowing.Status.ACTIVE)
                .collect(Collectors.toList());

        long availableBooksCount = store.books().stream()
                .filter(b -> b.getAvailableQuantity() > 0)
                .count();

        container.add(createKpiCard("My Borrowed Books", String.valueOf(myActiveBorrowings.size()), "📚", UITheme.PRIMARY));
        container.add(createKpiCard("Available in Library", String.valueOf(availableBooksCount), "📖", UITheme.ACCENT));
        container.add(createKpiCard("Membership Status", member.getStatus().name(), "✨", UITheme.PRIMARY_LIGHT));
        container.add(createKpiCard("Member Since", member.getMembershipDate().toString(), "📅", UITheme.WARNING));

        return container;
    }

    private JPanel createKpiCard(String label, String value, String icon, Color accentColor) {
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

        JLabel valComp = new JLabel(value);
        valComp.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valComp.setForeground(accentColor);

        card.add(topRow, BorderLayout.NORTH);
        card.add(valComp, BorderLayout.SOUTH);

        return card;
    }

    private JTabbedPane createTabbedContentPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("My Borrowed Books", createMyBorrowingsPanel());
        tabs.addTab("Browse Catalog", createCatalogPanel());
        tabs.addTab("My Profile", createProfilePanel());

        return tabs;
    }

    private JPanel createMyBorrowingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Borrow ID", "Book Title", "Author", "Borrow Date", "Due Date", "Days Remaining / Status"};
        myBorrowingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshMyBorrowingsTable();

        JTable table = new JTable(myBorrowingsModel);
        table.setFillsViewportHeight(true);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(28);
        UITheme.styleTableHeader(table.getTableHeader());

        centerAlignColumns(table, 0, 3, 4, 5);
        setColumnWidths(table, 70, 180, 140, 95, 120, 85);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void refreshMyBorrowingsTable() {
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

            myBorrowingsModel.addRow(new Object[]{
                    b.getBorrowingId(),
                    bookTitle,
                    bookAuthor,
                    b.getBorrowDate(),
                    b.getDueDate(),
                    statusStr
            });
        }
    }

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

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel header = new JLabel("Member Account Information");
        header.setFont(UITheme.FONT_HEADING);
        header.setForeground(UITheme.PRIMARY);
        card.add(header, gbc);

        gbc.gridy = 1;
        card.add(new JSeparator(), gbc);

        gbc.gridy = 2;
        card.add(profileRow("Full Name:", member.getName()), gbc);

        gbc.gridy = 3;
        card.add(profileRow("Email / Username:", member.getEmail()), gbc);

        gbc.gridy = 4;
        card.add(profileRow("Phone:", member.getPhone()), gbc);

        gbc.gridy = 5;
        card.add(profileRow("Address:", member.getAddress()), gbc);

        gbc.gridy = 6;
        card.add(profileRow("Membership Date:", member.getMembershipDate() != null ? member.getMembershipDate().toString() : "-"), gbc);

        gbc.gridy = 7;
        card.add(profileRow("Account Status:", member.getStatus() != null ? member.getStatus().name() : "ACTIVE"), gbc);

        panel.add(card, BorderLayout.NORTH);
        return panel;
    }

    private JPanel profileRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setPreferredSize(new Dimension(150, 24));

        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_BODY);

        row.add(lbl);
        row.add(val);
        return row;
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
