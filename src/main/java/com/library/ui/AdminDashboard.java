package com.library.ui;

import com.library.model.Admin;
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

/**
 * Modern Administrator Dashboard for the Library Management System.
 * Demonstrates: Polymorphism (receives Admin subtype of User), Encapsulation,
 * clean MVC/UI separation, and responsive table views.
 */
public class AdminDashboard extends JFrame {

    private final Admin admin;
    private final DataStore store = DataStore.getInstance();

    private DefaultTableModel booksTableModel;
    private DefaultTableModel membersTableModel;
    private DefaultTableModel borrowingsTableModel;

    public AdminDashboard(Admin admin) {
        this.admin = admin;

        setTitle("Library Management System - " + admin.getDashboardTitle());
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

        // 1. Header with Title, Admin Badge, and Logout
        root.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center content: KPI Cards + Tabbed Tables
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
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitleLabel);

        header.add(titleBox, BorderLayout.WEST);

        // Admin badge & Logout button
        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsBox.setOpaque(false);

        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        userBadge.setBackground(Color.WHITE);
        userBadge.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(2, 10, 2, 10)));

        JLabel userIcon = new JLabel("👤");
        JLabel userName = new JLabel(admin.getFullName() + " (" + admin.getEmail() + ")");
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
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiPanel.setOpaque(false);

        int totalBooks = store.books().size();
        int totalMembers = store.members().size();
        int activeBorrowings = store.borrowings().size();
        int totalCategories = store.categories().size();

        kpiPanel.add(createKpiCard("Total Books", String.valueOf(totalBooks), "📚", UITheme.PRIMARY));
        kpiPanel.add(createKpiCard("Registered Members", String.valueOf(totalMembers), "👥", UITheme.ACCENT));
        kpiPanel.add(createKpiCard("Active Borrowings", String.valueOf(activeBorrowings), "📖", UITheme.WARNING));
        kpiPanel.add(createKpiCard("Categories", String.valueOf(totalCategories), "🏷️", UITheme.PRIMARY_LIGHT));

        return kpiPanel;
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
        valComp.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valComp.setForeground(accentColor);

        card.add(topRow, BorderLayout.NORTH);
        card.add(valComp, BorderLayout.SOUTH);

        return card;
    }

    private JTabbedPane createTabbedContentPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("Books Catalog", createBooksPanel());
        tabs.addTab("Members Directory", createMembersPanel());
        tabs.addTab("Borrowing Records", createBorrowingsPanel());

        return tabs;
    }

    private JPanel createBooksPanel() {
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

        // Table
        String[] cols = { "ID", "ISBN", "Title", "Author", "Category", "Publisher", "Year", "Total Qty", "Available" };
        booksTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshBooksTable();

        JTable table = new JTable(booksTableModel);
        table.setFillsViewportHeight(true);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(28);
        UITheme.styleTableHeader(table.getTableHeader());

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(booksTableModel);
        table.setRowSorter(sorter);

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

        centerAlignColumns(table, 0, 1, 6, 7, 8);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        panel.add(filterRow, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void refreshBooksTable() {
        booksTableModel.setRowCount(0);
        for (Book b : store.books()) {
            booksTableModel.addRow(new Object[] {
                    b.getBookId(),
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategoryName(),
                    b.getPublisher(),
                    b.getPublicationYear(),
                    b.getTotalQuantity(),
                    b.getAvailableQuantity()
            });
        }
    }

    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = { "Member ID", "Full Name", "Email / Username", "Phone", "Address", "Join Date", "Status" };
        membersTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Member m : store.members()) {
            membersTableModel.addRow(new Object[] {
                    m.getMemberId(),
                    m.getName(),
                    m.getUsername(),
                    m.getPhone(),
                    m.getAddress(),
                    m.getMembershipDate(),
                    m.getStatus()
            });
        }

        JTable table = new JTable(membersTableModel);
        table.setFillsViewportHeight(true);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(28);
        UITheme.styleTableHeader(table.getTableHeader());

        centerAlignColumns(table, 0, 3, 5, 6);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBorrowingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = { "Borrow ID", "Member Name", "Book Title", "Borrow Date", "Due Date", "Status" };
        borrowingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Borrowing b : store.borrowings()) {
            Member member = store.findMemberById(b.getMemberId());
            String memberName = member != null ? member.getName() : "Member #" + b.getMemberId();

            Book book = store.findBookById(b.getBookId());
            String bookTitle = book != null ? book.getTitle() : "Book #" + b.getBookId();

            borrowingsTableModel.addRow(new Object[] {
                    b.getBorrowingId(),
                    memberName,
                    bookTitle,
                    b.getBorrowDate(),
                    b.getDueDate(),
                    b.isOverdue() ? "OVERDUE" : b.getStatus()
            });
        }

        JTable table = new JTable(borrowingsTableModel);
        table.setFillsViewportHeight(true);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(28);
        UITheme.styleTableHeader(table.getTableHeader());

        centerAlignColumns(table, 0, 3, 4, 5);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
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
