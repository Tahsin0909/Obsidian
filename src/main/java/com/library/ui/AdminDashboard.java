package com.library.ui;

import com.library.model.Admin;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Category;
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

/**
 * Modern Administrator Dashboard for the Library Management System.
 * Demonstrates: Polymorphism (receives Admin subtype of User), Encapsulation,
 * clean MVC/UI separation, and responsive table views.
 */
public class AdminDashboard extends JFrame {

    private final Admin admin;
    private final DataStore store = DataStore.getInstance();

    private DefaultTableModel booksTableModel;
    private JTable booksTable;
    private DefaultTableModel membersTableModel;
    private JTable membersTable;
    private DefaultTableModel borrowingsTableModel;
    private JLabel totalBooksValueLabel;
    private JLabel totalMembersValueLabel;

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

        kpiPanel.add(createKpiCard("Total Books", String.valueOf(totalBooks), "📚", UITheme.PRIMARY, true));
        kpiPanel.add(createKpiCard("Registered Members", String.valueOf(totalMembers), "👥", UITheme.ACCENT, false));
        kpiPanel.add(createKpiCard("Active Borrowings", String.valueOf(activeBorrowings), "📖", UITheme.WARNING, false));
        kpiPanel.add(createKpiCard("Categories", String.valueOf(totalCategories), "🏷️", UITheme.PRIMARY_LIGHT, false));

        return kpiPanel;
    }

    private JPanel createKpiCard(String label, String value, String icon, Color accentColor, boolean isTotalBooks) {
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

        if (isTotalBooks) {
            totalBooksValueLabel = valComp;
        } else if ("Registered Members".equals(label)) {
            totalMembersValueLabel = valComp;
        }

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

        // Search Bar & Action Top
        JPanel filterRow = new JPanel(new BorderLayout(12, 0));
        filterRow.setOpaque(false);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setOpaque(false);
        JLabel searchLbl = new JLabel("Search Books:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(280, 32));
        searchBox.add(searchLbl);
        searchBox.add(searchField);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsBox.setOpaque(false);

        JButton addBookBtn = UITheme.primaryButton("+ Add Book");
        addBookBtn.addActionListener(e -> showAddBookDialog());

        JButton updateBookBtn = UITheme.secondaryButton("Update Book");
        updateBookBtn.addActionListener(e -> onUpdateBookClicked());

        JButton deleteBookBtn = UITheme.dangerButton("Delete Book");
        deleteBookBtn.addActionListener(e -> onDeleteBookClicked());

        actionsBox.add(addBookBtn);
        actionsBox.add(updateBookBtn);
        actionsBox.add(deleteBookBtn);

        filterRow.add(searchBox, BorderLayout.WEST);
        filterRow.add(actionsBox, BorderLayout.EAST);

        // Table
        String[] cols = { "ID", "ISBN", "Title", "Author", "Category", "Publisher", "Year", "Total Qty", "Available" };
        booksTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshBooksTable();

        booksTable = new JTable(booksTableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.setFillsViewportHeight(true);
        booksTable.setFont(UITheme.FONT_BODY);
        booksTable.setRowHeight(28);
        UITheme.styleTableHeader(booksTable.getTableHeader());

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(booksTableModel);
        booksTable.setRowSorter(sorter);

        booksTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && booksTable.getSelectedRow() != -1) {
                    onUpdateBookClicked();
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

        centerAlignColumns(booksTable, 0, 1, 6, 7, 8);
        setColumnWidths(booksTable, 50, 115, 170, 130, 130, 110, 55, 75, 75);

        JScrollPane scrollPane = new JScrollPane(booksTable);
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

        // Search Bar & Action Top
        JPanel filterRow = new JPanel(new BorderLayout(12, 0));
        filterRow.setOpaque(false);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setOpaque(false);
        JLabel searchLbl = new JLabel("Search Members:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(280, 32));
        searchBox.add(searchLbl);
        searchBox.add(searchField);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsBox.setOpaque(false);

        JButton addMemberBtn = UITheme.primaryButton("+ Add Member");
        addMemberBtn.addActionListener(e -> showAddMemberDialog());

        JButton updateMemberBtn = UITheme.secondaryButton("Update Member");
        updateMemberBtn.addActionListener(e -> onUpdateMemberClicked());

        actionsBox.add(addMemberBtn);
        actionsBox.add(updateMemberBtn);

        filterRow.add(searchBox, BorderLayout.WEST);
        filterRow.add(actionsBox, BorderLayout.EAST);

        String[] cols = { "Member ID", "Full Name", "Username", "Email", "Phone", "Address", "Join Date", "Status" };
        membersTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshMembersTable();

        membersTable = new JTable(membersTableModel);
        membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersTable.setFillsViewportHeight(true);
        membersTable.setFont(UITheme.FONT_BODY);
        membersTable.setRowHeight(28);
        UITheme.styleTableHeader(membersTable.getTableHeader());

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

        centerAlignColumns(membersTable, 0, 2, 4, 6, 7);
        setColumnWidths(membersTable, 50, 130, 100, 140, 100, 170, 85, 75);

        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        panel.add(filterRow, BorderLayout.NORTH);
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

        refreshBorrowingsTable();

        JTable table = new JTable(borrowingsTableModel);
        table.setFillsViewportHeight(true);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(28);
        UITheme.styleTableHeader(table.getTableHeader());

        centerAlignColumns(table, 0, 3, 4, 5);
        setColumnWidths(table, 70, 140, 180, 95, 95, 85);

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

    private void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        JLabel heading = new JLabel("Add Book to Catalog");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Enter book metadata and stock quantity");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField isbnField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();

        JComboBox<Category> categoryCombo = new JComboBox<>();
        categoryCombo.setFont(UITheme.FONT_BODY);
        for (Category cat : store.categories()) {
            categoryCombo.addItem(cat);
        }

        JTextField publisherField = new JTextField();
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 1800, 2100, 1));
        yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "#"));
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 9999, 1));

        addFormField(form, gbc, 0, "ISBN:", isbnField);
        addFormField(form, gbc, 1, "Book Title:", titleField);
        addFormField(form, gbc, 2, "Author:", authorField);
        addFormField(form, gbc, 3, "Category:", categoryCombo);
        addFormField(form, gbc, 4, "Publisher:", publisherField);
        addFormField(form, gbc, 5, "Publication Year:", yearSpinner);
        addFormField(form, gbc, 6, "Total Copies / Quantity:", qtySpinner);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UITheme.primaryButton("Save Book");
        saveBtn.addActionListener(e -> {
            String isbn = isbnField.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String publisher = publisherField.getText().trim();
            Category category = (Category) categoryCombo.getSelectedItem();
            int year = (Integer) yearSpinner.getValue();
            int qty = (Integer) qtySpinner.getValue();

            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in ISBN, Title, and Author.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (store.findBookByIsbn(isbn) != null) {
                JOptionPane.showMessageDialog(dialog, "A book with ISBN \"" + isbn + "\" already exists!", "Duplicate Book", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Book newBook = new Book(
                    store.nextBookId(),
                    isbn,
                    title,
                    author,
                    category != null ? category.getCategoryId() : 1,
                    category != null ? category.getCategoryName() : "General",
                    publisher.isEmpty() ? "Independent" : publisher,
                    year,
                    qty,
                    qty
            );

            store.addBook(newBook);
            refreshBooksTable();
            if (totalBooksValueLabel != null) {
                totalBooksValueLabel.setText(String.valueOf(store.books().size()));
            }

            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Book \"" + newBook.getTitle() + "\" added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        field.setFont(UITheme.FONT_BODY);
        if (field instanceof JTextField) {
            ((JTextField) field).setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
        }
        panel.add(field, gbc);
    }

    private void refreshBorrowingsTable() {
        if (borrowingsTableModel == null) {
            return;
        }
        borrowingsTableModel.setRowCount(0);
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
    }

    private Book getSelectedBook() {
        if (booksTable == null) {
            return null;
        }
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        int modelRow = booksTable.convertRowIndexToModel(selectedRow);
        int bookId = (Integer) booksTableModel.getValueAt(modelRow, 0);
        return store.findBookById(bookId);
    }

    private void onUpdateBookClicked() {
        Book book = getSelectedBook();
        if (book == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book from the table to update.",
                    "No Book Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        showUpdateBookDialog(book);
    }

    private void showUpdateBookDialog(Book book) {
        JDialog dialog = new JDialog(this, "Update Book Details", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        JLabel heading = new JLabel("Update Book (ID: " + book.getBookId() + ")");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Modify book metadata and stock quantity");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField isbnField = new JTextField(book.getIsbn());
        JTextField titleField = new JTextField(book.getTitle());
        JTextField authorField = new JTextField(book.getAuthor());

        JComboBox<Category> categoryCombo = new JComboBox<>();
        categoryCombo.setFont(UITheme.FONT_BODY);
        Category selectedCategory = null;
        for (Category cat : store.categories()) {
            categoryCombo.addItem(cat);
            if (cat.getCategoryId() == book.getCategoryId()) {
                selectedCategory = cat;
            }
        }
        if (selectedCategory != null) {
            categoryCombo.setSelectedItem(selectedCategory);
        }

        JTextField publisherField = new JTextField(book.getPublisher());
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(book.getPublicationYear(), 1800, 2100, 1));
        yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "#"));
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(book.getTotalQuantity(), 1, 9999, 1));

        addFormField(form, gbc, 0, "ISBN:", isbnField);
        addFormField(form, gbc, 1, "Book Title:", titleField);
        addFormField(form, gbc, 2, "Author:", authorField);
        addFormField(form, gbc, 3, "Category:", categoryCombo);
        addFormField(form, gbc, 4, "Publisher:", publisherField);
        addFormField(form, gbc, 5, "Publication Year:", yearSpinner);
        addFormField(form, gbc, 6, "Total Copies / Quantity:", qtySpinner);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UITheme.primaryButton("Save Changes");
        saveBtn.addActionListener(e -> {
            String isbn = isbnField.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String publisher = publisherField.getText().trim();
            Category category = (Category) categoryCombo.getSelectedItem();
            int year = (Integer) yearSpinner.getValue();
            int newTotalQty = (Integer) qtySpinner.getValue();

            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in ISBN, Title, and Author.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Book existingWithIsbn = store.findBookByIsbn(isbn);
            if (existingWithIsbn != null && existingWithIsbn.getBookId() != book.getBookId()) {
                JOptionPane.showMessageDialog(dialog, "Another book with ISBN \"" + isbn + "\" already exists!", "Duplicate Book", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int currentlyBorrowed = book.getTotalQuantity() - book.getAvailableQuantity();
            if (newTotalQty < currentlyBorrowed) {
                JOptionPane.showMessageDialog(dialog,
                        "Total quantity cannot be less than the number of currently borrowed copies (" + currentlyBorrowed + ").",
                        "Invalid Quantity",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int newAvailableQty = newTotalQty - currentlyBorrowed;

            book.setIsbn(isbn);
            book.setTitle(title);
            book.setAuthor(author);
            if (category != null) {
                book.setCategoryId(category.getCategoryId());
                book.setCategoryName(category.getCategoryName());
            }
            book.setPublisher(publisher.isEmpty() ? "Independent" : publisher);
            book.setPublicationYear(year);
            book.setTotalQuantity(newTotalQty);
            book.setAvailableQuantity(newAvailableQty);

            store.updateBook(book);
            refreshBooksTable();
            refreshBorrowingsTable();

            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Book \"" + book.getTitle() + "\" updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void onDeleteBookClicked() {
        Book book = getSelectedBook();
        if (book == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book from the table to delete.",
                    "No Book Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (store.hasActiveBorrowingsForBook(book.getBookId())) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete \"" + book.getTitle() + "\" because copy/copies are currently borrowed by member(s).\n" +
                    "Please ensure all borrowed copies are returned before deleting this book.",
                    "Active Borrowings Exist",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the following book from the catalog?\n\n" +
                "Title: " + book.getTitle() + "\n" +
                "Author: " + book.getAuthor() + "\n" +
                "ISBN: " + book.getIsbn() + "\n" +
                "Total Copies: " + book.getTotalQuantity() + "\n\n" +
                "This action cannot be undone.",
                "Confirm Book Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            boolean deleted = store.deleteBook(book.getBookId());
            if (deleted) {
                refreshBooksTable();
                refreshBorrowingsTable();
                if (totalBooksValueLabel != null) {
                    totalBooksValueLabel.setText(String.valueOf(store.books().size()));
                }
                JOptionPane.showMessageDialog(this,
                        "Book \"" + book.getTitle() + "\" deleted successfully.",
                        "Book Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete book. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshMembersTable() {
        if (membersTableModel == null) {
            return;
        }
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
    }

    private Member getSelectedMember() {
        if (membersTable == null) {
            return null;
        }
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        int modelRow = membersTable.convertRowIndexToModel(selectedRow);
        int memberId = (Integer) membersTableModel.getValueAt(modelRow, 0);
        return store.findMemberById(memberId);
    }

    private void onUpdateMemberClicked() {
        Member member = getSelectedMember();
        if (member == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a member from the table to update.",
                    "No Member Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        showUpdateMemberDialog(member);
    }

    private void showAddMemberDialog() {
        JDialog dialog = new JDialog(this, "Add New Member", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
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

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField();
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField addressField = new JTextField();
        JPasswordField passwordField = new JPasswordField("123456");
        JComboBox<Member.Status> statusCombo = new JComboBox<>(Member.Status.values());
        statusCombo.setFont(UITheme.FONT_BODY);

        addFormField(form, gbc, 0, "Full Name:", nameField);
        addFormField(form, gbc, 1, "Username:", usernameField);
        addFormField(form, gbc, 2, "Email:", emailField);
        addFormField(form, gbc, 3, "Phone:", phoneField);
        addFormField(form, gbc, 4, "Address:", addressField);
        addFormField(form, gbc, 5, "Password (Initial):", passwordField);
        addFormField(form, gbc, 6, "Status:", statusCombo);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
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
                JOptionPane.showMessageDialog(dialog, "Please fill in Name, Username, and Email.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter an initial password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (store.isUsernameOrEmailTaken(username, -1)) {
                JOptionPane.showMessageDialog(dialog, "Username \"" + username + "\" is already taken!", "Duplicate User", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (store.isUsernameOrEmailTaken(email, -1)) {
                JOptionPane.showMessageDialog(dialog, "Email \"" + email + "\" is already registered!", "Duplicate User", JOptionPane.WARNING_MESSAGE);
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
                    status != null ? status : Member.Status.ACTIVE
            );

            store.addMember(newMember);
            refreshMembersTable();
            if (totalMembersValueLabel != null) {
                totalMembersValueLabel.setText(String.valueOf(store.members().size()));
            }

            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Member \"" + newMember.getName() + "\" registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        JDialog dialog = new JDialog(this, "Update Member Details", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
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

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
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

        addFormField(form, gbc, 0, "Full Name:", nameField);
        addFormField(form, gbc, 1, "Username:", usernameField);
        addFormField(form, gbc, 2, "Email:", emailField);
        addFormField(form, gbc, 3, "Phone:", phoneField);
        addFormField(form, gbc, 4, "Address:", addressField);
        addFormField(form, gbc, 5, "New Password (Optional):", passwordField);
        addFormField(form, gbc, 6, "Status:", statusCombo);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
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
                JOptionPane.showMessageDialog(dialog, "Please fill in Name, Username, and Email.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (store.isUsernameOrEmailTaken(username, member.getMemberId())) {
                JOptionPane.showMessageDialog(dialog, "Username \"" + username + "\" is already taken!", "Duplicate User", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (store.isUsernameOrEmailTaken(email, member.getMemberId())) {
                JOptionPane.showMessageDialog(dialog, "Email \"" + email + "\" is already registered!", "Duplicate User", JOptionPane.WARNING_MESSAGE);
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
            refreshMembersTable();
            refreshBorrowingsTable();

            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Member \"" + member.getName() + "\" updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
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
