package com.library.ui;

import com.library.model.Admin;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Category;
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
import java.util.List;

/**
 * Modern, Responsive Administrator Dashboard for the Library Management System.
 * Demonstrates: Polymorphism (receives Admin subtype of User), Encapsulation,
 * clean MVC/UI separation, responsive layouts, and complete Fine Management.
 */
public class AdminDashboard extends JFrame {

    private final Admin admin;
    private final DataStore store = DataStore.getInstance();

    // Books components
    private DefaultTableModel booksTableModel;
    private JTable booksTable;

    // Members components
    private DefaultTableModel membersTableModel;
    private JTable membersTable;

    // Borrowings components
    private DefaultTableModel borrowingsTableModel;
    private JTable borrowingsTable;
    private JComboBox<String> borrowingFilterCombo;
    private JTextField borrowingSearchField;

    // Fines components
    private DefaultTableModel finesTableModel;
    private JTable finesTable;
    private JComboBox<String> fineFilterCombo;
    private JComboBox<String> fineMemberFilterCombo;
    private JTextField fineSearchField;
    private JLabel totalFinesAssessedLabel;
    private JLabel totalFinesUnpaidLabel;
    private JLabel totalFinesCollectedLabel;

    // KPI Labels
    private JLabel totalBooksValueLabel;
    private JLabel totalMembersValueLabel;
    private JLabel activeBorrowingsValueLabel;
    private JLabel outstandingFinesKpiLabel;

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

        // 2. Center content: KPI Cards + Tabbed Tables
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

        int totalBooks = store.books().size();
        int totalMembers = store.members().size();
        int activeBorrowings = (int) store.borrowings().stream().filter(b -> b.getStatus() == Borrowing.Status.ACTIVE).count();
        double outstandingFines = store.getTotalOutstandingFines();

        kpiPanel.add(createKpiCard("Total Books", String.valueOf(totalBooks), UITheme.PRIMARY, "books"));
        kpiPanel.add(createKpiCard("Registered Members", String.valueOf(totalMembers), UITheme.ACCENT, "members"));
        kpiPanel.add(createKpiCard("Active Borrowings", String.valueOf(activeBorrowings), UITheme.WARNING, "borrowings"));
        kpiPanel.add(createKpiCard("Unpaid Fines", String.format("$%.2f", outstandingFines), UITheme.DANGER, "fines"));

        return kpiPanel;
    }

    private JPanel createKpiCard(String label, String value, Color accentColor, String type) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UITheme.FONT_SMALL);
        labelComp.setForeground(UITheme.TEXT_MUTED);

        JLabel valComp = new JLabel(value);
        valComp.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valComp.setForeground(accentColor);

        if ("books".equals(type)) {
            totalBooksValueLabel = valComp;
        } else if ("members".equals(type)) {
            totalMembersValueLabel = valComp;
        } else if ("borrowings".equals(type)) {
            activeBorrowingsValueLabel = valComp;
        } else if ("fines".equals(type)) {
            outstandingFinesKpiLabel = valComp;
        }

        card.add(labelComp, BorderLayout.NORTH);
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
        tabs.addTab("Fine Management", createFinesPanel());

        return tabs;
    }

    // ============================================================================
    // 1. BOOKS PANEL
    // ============================================================================

    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Responsive Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 8));
        toolbar.setOpaque(false);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBox.setOpaque(false);
        JLabel searchLbl = new JLabel("Search Books:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(240, 30));
        searchBox.add(searchLbl);
        searchBox.add(searchField);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
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

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(actionsBox, BorderLayout.EAST);

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
        booksTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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

        centerAlignColumns(booksTable, 0, 1, 6, 7, 8);
        setColumnWidths(booksTable, 45, 110, 160, 120, 110, 100, 50, 70, 70);

        JScrollPane scrollPane = new JScrollPane(booksTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void refreshBooksTable() {
        if (booksTableModel == null) return;
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

    private Book getSelectedBook() {
        if (booksTable == null) return null;
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) return null;
        int modelRow = booksTable.convertRowIndexToModel(selectedRow);
        int bookId = (Integer) booksTableModel.getValueAt(modelRow, 0);
        return store.findBookById(bookId);
    }

    private void onUpdateBookClicked() {
        Book book = getSelectedBook();
        if (book == null) {
            JOptionPane.showMessageDialog(this, "Please select a book from the table to update.", "No Book Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showUpdateBookDialog(book);
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

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

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
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

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
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

    private void showUpdateBookDialog(Book book) {
        JDialog dialog = new JDialog(this, "Update Book Details", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

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

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
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

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
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
            JOptionPane.showMessageDialog(this, "Please select a book from the table to delete.", "No Book Selected", JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Book \"" + book.getTitle() + "\" deleted successfully.", "Book Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete book. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ============================================================================
    // 2. MEMBERS PANEL
    // ============================================================================

    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Responsive Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 8));
        toolbar.setOpaque(false);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBox.setOpaque(false);
        JLabel searchLbl = new JLabel("Search Members:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        JTextField searchField = new JTextField();
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
            showMemberFinesDialog(m);
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

        refreshMembersTable();

        membersTable = new JTable(membersTableModel);
        membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersTable.setFillsViewportHeight(true);
        membersTable.setFont(UITheme.FONT_BODY);
        membersTable.setRowHeight(28);
        membersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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

        centerAlignColumns(membersTable, 0, 2, 4, 6, 7);
        setColumnWidths(membersTable, 50, 130, 100, 140, 100, 160, 85, 75);

        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void refreshMembersTable() {
        if (membersTableModel == null) return;
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
        refreshFineMemberFilterCombo();
    }

    private Member getSelectedMember() {
        if (membersTable == null) return null;
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) return null;
        int modelRow = membersTable.convertRowIndexToModel(selectedRow);
        int memberId = (Integer) membersTableModel.getValueAt(modelRow, 0);
        return store.findMemberById(memberId);
    }

    private void onUpdateMemberClicked() {
        Member member = getSelectedMember();
        if (member == null) {
            JOptionPane.showMessageDialog(this, "Please select a member from the table to update.", "No Member Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showUpdateMemberDialog(member);
    }

    private void showAddMemberDialog() {
        JDialog dialog = new JDialog(this, "Add New Member", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
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

        addFormField(form, gbc, 0, "Full Name:", nameField);
        addFormField(form, gbc, 1, "Username:", usernameField);
        addFormField(form, gbc, 2, "Email:", emailField);
        addFormField(form, gbc, 3, "Phone:", phoneField);
        addFormField(form, gbc, 4, "Address:", addressField);
        addFormField(form, gbc, 5, "Password (Initial):", passwordField);
        addFormField(form, gbc, 6, "Status:", statusCombo);

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
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
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

        addFormField(form, gbc, 0, "Full Name:", nameField);
        addFormField(form, gbc, 1, "Username:", usernameField);
        addFormField(form, gbc, 2, "Email:", emailField);
        addFormField(form, gbc, 3, "Phone:", phoneField);
        addFormField(form, gbc, 4, "Address:", addressField);
        addFormField(form, gbc, 5, "New Password (Optional):", passwordField);
        addFormField(form, gbc, 6, "Status:", statusCombo);

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
            refreshFinesTable();

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

    // ============================================================================
    // 3. BORROWINGS PANEL
    // ============================================================================

    private JPanel createBorrowingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Responsive Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 8));
        toolbar.setOpaque(false);

        JPanel searchAndFilterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchAndFilterBox.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        borrowingSearchField = new JTextField();
        borrowingSearchField.setPreferredSize(new Dimension(170, 30));

        JLabel filterLbl = new JLabel("View:");
        filterLbl.setFont(UITheme.FONT_BODY_BOLD);

        String[] filterOptions = { "All Records (History)", "Active Borrowings Only", "Overdue Books Only" };
        borrowingFilterCombo = new JComboBox<>(filterOptions);
        borrowingFilterCombo.setFont(UITheme.FONT_BODY);
        borrowingFilterCombo.setPreferredSize(new Dimension(175, 30));
        borrowingFilterCombo.addActionListener(e -> refreshBorrowingsTable());

        searchAndFilterBox.add(searchLbl);
        searchAndFilterBox.add(borrowingSearchField);
        searchAndFilterBox.add(filterLbl);
        searchAndFilterBox.add(borrowingFilterCombo);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionsBox.setOpaque(false);

        JButton borrowBtn = UITheme.primaryButton("+ Borrow Book");
        borrowBtn.addActionListener(e -> showBorrowBookDialog());

        JButton returnBtn = UITheme.accentButton("Process Return");
        returnBtn.addActionListener(e -> onProcessReturnClicked());

        JButton calculateFineBtn = UITheme.secondaryButton("Calculate Fine");
        calculateFineBtn.addActionListener(e -> showCalculateFineDialog(getSelectedBorrowing()));

        actionsBox.add(borrowBtn);
        actionsBox.add(returnBtn);
        actionsBox.add(calculateFineBtn);

        toolbar.add(searchAndFilterBox, BorderLayout.WEST);
        toolbar.add(actionsBox, BorderLayout.EAST);

        String[] cols = { "Borrow ID", "Member Name", "Book Title", "Borrow Date", "Due Date", "Return Date", "Status" };
        borrowingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        borrowingsTable = new JTable(borrowingsTableModel);
        borrowingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        borrowingsTable.setFillsViewportHeight(true);
        borrowingsTable.setFont(UITheme.FONT_BODY);
        borrowingsTable.setRowHeight(28);
        borrowingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UITheme.styleTableHeader(borrowingsTable.getTableHeader());

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(borrowingsTableModel);
        borrowingsTable.setRowSorter(sorter);

        borrowingsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && borrowingsTable.getSelectedRow() != -1) {
                    onProcessReturnClicked();
                }
            }
        });

        borrowingSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = borrowingSearchField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        refreshBorrowingsTable();

        centerAlignColumns(borrowingsTable, 0, 3, 4, 5, 6);
        setColumnWidths(borrowingsTable, 65, 130, 170, 85, 85, 85, 80);

        JScrollPane scrollPane = new JScrollPane(borrowingsTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private Borrowing getSelectedBorrowing() {
        if (borrowingsTable == null) return null;
        int selectedRow = borrowingsTable.getSelectedRow();
        if (selectedRow == -1) return null;
        int modelRow = borrowingsTable.convertRowIndexToModel(selectedRow);
        int borrowingId = (Integer) borrowingsTableModel.getValueAt(modelRow, 0);
        for (Borrowing b : store.borrowings()) {
            if (b.getBorrowingId() == borrowingId) {
                return b;
            }
        }
        return null;
    }

    private void updateActiveBorrowingsKpi() {
        if (activeBorrowingsValueLabel != null) {
            int count = (int) store.borrowings().stream().filter(b -> b.getStatus() == Borrowing.Status.ACTIVE).count();
            activeBorrowingsValueLabel.setText(String.valueOf(count));
        }
    }

    private void refreshBorrowingsTable() {
        if (borrowingsTableModel == null) return;
        borrowingsTableModel.setRowCount(0);

        String selectedFilter = borrowingFilterCombo != null ? (String) borrowingFilterCombo.getSelectedItem() : "All Records (History)";

        for (Borrowing b : store.borrowings()) {
            boolean include = true;
            if ("Active Borrowings Only".equals(selectedFilter)) {
                include = (b.getStatus() == Borrowing.Status.ACTIVE);
            } else if ("Overdue Books Only".equals(selectedFilter)) {
                include = b.isOverdue();
            }

            if (!include) continue;

            Member member = store.findMemberById(b.getMemberId());
            String memberName = member != null ? member.getName() : "Member #" + b.getMemberId();

            Book book = store.findBookById(b.getBookId());
            String bookTitle = book != null ? book.getTitle() : "Book #" + b.getBookId();

            String statusStr;
            if (b.getStatus() == Borrowing.Status.RETURNED) {
                statusStr = "RETURNED";
            } else if (b.isOverdue()) {
                statusStr = "OVERDUE";
            } else {
                statusStr = "ACTIVE";
            }

            String returnDateStr = b.getReturnDate() != null ? b.getReturnDate().toString() : "-";

            borrowingsTableModel.addRow(new Object[] {
                    b.getBorrowingId(),
                    memberName,
                    bookTitle,
                    b.getBorrowDate(),
                    b.getDueDate(),
                    returnDateStr,
                    statusStr
            });
        }
    }

    private void showBorrowBookDialog() {
        JDialog dialog = new JDialog(this, "Borrow Book for Member", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        JLabel heading = new JLabel("Issue / Borrow Book");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Select member, available book, and loan duration");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Member> memberCombo = new JComboBox<>();
        memberCombo.setFont(UITheme.FONT_BODY);
        for (Member m : store.members()) {
            if (m.isActive()) {
                memberCombo.addItem(m);
            }
        }
        memberCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Member) {
                    Member m = (Member) value;
                    setText(m.getName() + " (ID: #" + m.getMemberId() + ", " + m.getUsername() + ")");
                }
                return this;
            }
        });

        JComboBox<Book> bookCombo = new JComboBox<>();
        bookCombo.setFont(UITheme.FONT_BODY);
        for (Book b : store.books()) {
            if (b.getAvailableQuantity() > 0) {
                bookCombo.addItem(b);
            }
        }
        bookCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Book) {
                    Book b = (Book) value;
                    setText(b.getTitle() + " by " + b.getAuthor() + " [" + b.getAvailableQuantity() + " avail]");
                }
                return this;
            }
        });

        JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(14, 1, 90, 1));
        daysSpinner.setFont(UITheme.FONT_BODY);

        JLabel dueDatePreview = new JLabel("Due Date: " + LocalDate.now().plusDays(14));
        dueDatePreview.setFont(UITheme.FONT_BODY_BOLD);
        dueDatePreview.setForeground(UITheme.PRIMARY);

        daysSpinner.addChangeListener(e -> {
            int days = (Integer) daysSpinner.getValue();
            dueDatePreview.setText("Due Date: " + LocalDate.now().plusDays(days));
        });

        addFormField(form, gbc, 0, "Select Member:", memberCombo);
        addFormField(form, gbc, 1, "Select Book:", bookCombo);
        addFormField(form, gbc, 2, "Loan Duration (Days):", daysSpinner);
        addFormField(form, gbc, 3, "Calculated Due Date:", dueDatePreview);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton issueBtn = UITheme.primaryButton("Issue Book");
        issueBtn.addActionListener(e -> {
            Member member = (Member) memberCombo.getSelectedItem();
            Book book = (Book) bookCombo.getSelectedItem();
            int days = (Integer) daysSpinner.getValue();

            if (member == null) {
                JOptionPane.showMessageDialog(dialog, "Please select an active member.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (book == null) {
                JOptionPane.showMessageDialog(dialog, "No book selected or no copies currently available.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Borrowing borrowing = store.borrowBook(member.getMemberId(), book.getBookId(), days);
            if (borrowing != null) {
                refreshBorrowingsTable();
                refreshBooksTable();
                updateActiveBorrowingsKpi();

                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        "Book \"" + book.getTitle() + "\" successfully issued to " + member.getName() + "!\n" +
                        "Due Date: " + borrowing.getDueDate(),
                        "Book Issued",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to issue book. The book might be out of stock.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(issueBtn);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void onProcessReturnClicked() {
        int selectedRow = borrowingsTable != null ? borrowingsTable.getSelectedRow() : -1;

        if (selectedRow != -1) {
            int modelRow = borrowingsTable.convertRowIndexToModel(selectedRow);
            int borrowingId = (Integer) borrowingsTableModel.getValueAt(modelRow, 0);

            Borrowing target = null;
            for (Borrowing b : store.borrowings()) {
                if (b.getBorrowingId() == borrowingId) {
                    target = b;
                    break;
                }
            }

            if (target == null) {
                JOptionPane.showMessageDialog(this, "Borrowing record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (target.getStatus() == Borrowing.Status.RETURNED) {
                JOptionPane.showMessageDialog(this, "This book has already been returned on " + target.getReturnDate() + ".", "Already Returned", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Member member = store.findMemberById(target.getMemberId());
            String memberName = member != null ? member.getName() : "Member #" + target.getMemberId();
            Book book = store.findBookById(target.getBookId());
            String bookTitle = book != null ? book.getTitle() : "Book #" + target.getBookId();

            int choice = JOptionPane.showConfirmDialog(this,
                    "Process book return for:\n\n" +
                    "Borrow ID: #" + target.getBorrowingId() + "\n" +
                    "Book: " + bookTitle + "\n" +
                    "Member: " + memberName + "\n" +
                    "Due Date: " + target.getDueDate() + (target.isOverdue() ? "  (OVERDUE!)" : "") + "\n\n" +
                    "Mark this book as returned today (" + LocalDate.now() + ")?",
                    "Confirm Book Return",
                    JOptionPane.YES_NO_OPTION,
                    target.isOverdue() ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                boolean returned = store.returnBook(borrowingId);
                if (returned) {
                    refreshBorrowingsTable();
                    refreshBooksTable();
                    updateActiveBorrowingsKpi();

                    if (target.getDueDate() != null && LocalDate.now().isAfter(target.getDueDate())) {
                        long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(target.getDueDate(), LocalDate.now());
                        int fineChoice = JOptionPane.showConfirmDialog(this,
                                "Book returned successfully!\n\n" +
                                "Note: This book was overdue by " + overdueDays + " day(s).\n" +
                                "Would you like to assess/calculate a fine for this return now?",
                                "Overdue Return - Assess Fine?",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);
                        if (fineChoice == JOptionPane.YES_OPTION) {
                            showCalculateFineDialog(target);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Book \"" + bookTitle + "\" returned successfully!\nStock quantity restored.", "Return Processed", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to process return.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            showSelectReturnDialog();
        }
    }

    private void showSelectReturnDialog() {
        java.util.List<Borrowing> activeList = new java.util.ArrayList<>();
        for (Borrowing b : store.borrowings()) {
            if (b.getStatus() == Borrowing.Status.ACTIVE) {
                activeList.add(b);
            }
        }

        if (activeList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are currently no active borrowings to return.", "No Active Borrowings", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Process Book Return", true);
        dialog.setSize(500, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        JLabel heading = new JLabel("Process Book Return");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Select an active borrowed book to mark as returned");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Borrowing> combo = new JComboBox<>(activeList.toArray(new Borrowing[0]));
        combo.setFont(UITheme.FONT_BODY);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Borrowing) {
                    Borrowing b = (Borrowing) value;
                    Book book = store.findBookById(b.getBookId());
                    Member member = store.findMemberById(b.getMemberId());
                    String bTitle = book != null ? book.getTitle() : "Book #" + b.getBookId();
                    String mName = member != null ? member.getName() : "Member #" + b.getMemberId();
                    String overdueTag = b.isOverdue() ? " [OVERDUE]" : "";
                    setText("#" + b.getBorrowingId() + ": " + bTitle + " (" + mName + ")" + overdueTag);
                }
                return this;
            }
        });

        addFormField(form, gbc, 0, "Active Loan:", combo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton returnBtn = UITheme.accentButton("Confirm Return");
        returnBtn.addActionListener(e -> {
            Borrowing selected = (Borrowing) combo.getSelectedItem();
            if (selected == null) return;

            boolean success = store.returnBook(selected.getBorrowingId());
            if (success) {
                refreshBorrowingsTable();
                refreshBooksTable();
                updateActiveBorrowingsKpi();
                dialog.dispose();

                Book book = store.findBookById(selected.getBookId());
                String bTitle = book != null ? book.getTitle() : "Book #" + selected.getBookId();
                JOptionPane.showMessageDialog(this, "Book \"" + bTitle + "\" was returned successfully!\nStock quantity restored.", "Return Processed", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(returnBtn);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // ============================================================================
    // 4. FINE MANAGEMENT PANEL (RESPONSIVE TOOLBAR & TABLES)
    // ============================================================================

    private JPanel createFinesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Top Summary Metric Strip
        JPanel topSummaryPanel = createFineSummaryStrip();

        // Responsive 2-Row Toolbar: Row 1 = Search & Filters, Row 2 = Action Buttons
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
        toolbarPanel.setOpaque(false);
        toolbarPanel.setBorder(new EmptyBorder(2, 0, 8, 0));

        // Row 1: Search & Filter Controls
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        row1.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        fineSearchField = new JTextField();
        fineSearchField.setPreferredSize(new Dimension(150, 28));

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(UITheme.FONT_BODY_BOLD);
        String[] statusFilters = { "All Fines", "Unpaid Fines Only", "Paid Fines Only" };
        fineFilterCombo = new JComboBox<>(statusFilters);
        fineFilterCombo.setFont(UITheme.FONT_BODY);
        fineFilterCombo.setPreferredSize(new Dimension(135, 28));
        fineFilterCombo.addActionListener(e -> refreshFinesTable());

        JLabel memberLbl = new JLabel("Member:");
        memberLbl.setFont(UITheme.FONT_BODY_BOLD);
        fineMemberFilterCombo = new JComboBox<>();
        fineMemberFilterCombo.setFont(UITheme.FONT_BODY);
        fineMemberFilterCombo.setPreferredSize(new Dimension(150, 28));
        refreshFineMemberFilterCombo();
        fineMemberFilterCombo.addActionListener(e -> refreshFinesTable());

        row1.add(searchLbl);
        row1.add(fineSearchField);
        row1.add(Box.createHorizontalStrut(6));
        row1.add(statusLbl);
        row1.add(fineFilterCombo);
        row1.add(Box.createHorizontalStrut(6));
        row1.add(memberLbl);
        row1.add(fineMemberFilterCombo);

        // Row 2: Action Buttons
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        row2.setOpaque(false);

        JButton calcFineBtn = UITheme.primaryButton("+ Calculate Fine");
        calcFineBtn.addActionListener(e -> showCalculateFineDialog(null));

        JButton autoAssessBtn = UITheme.accentButton("Auto-Assess Overdue");
        autoAssessBtn.addActionListener(e -> autoAssessAllOverdueFines());

        JButton payFineBtn = UITheme.accentButton("Settle Payment");
        payFineBtn.addActionListener(e -> onProcessFinePaymentClicked());

        JButton memberFinesBtn = UITheme.secondaryButton("Member Fines");
        memberFinesBtn.addActionListener(e -> showMemberFinesDialog(null));

        JButton waiveBtn = UITheme.dangerButton("Waive Fine");
        waiveBtn.addActionListener(e -> onWaiveFineClicked());

        row2.add(calcFineBtn);
        row2.add(autoAssessBtn);
        row2.add(payFineBtn);
        row2.add(memberFinesBtn);
        row2.add(waiveBtn);

        toolbarPanel.add(row1);
        toolbarPanel.add(row2);

        // Fines Table
        String[] cols = { "Fine ID", "Borrow ID", "Member Name", "Book Title", "Overdue Days", "Fine Amount ($)", "Assessed Date", "Paid Date", "Payment Method", "Status" };
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

        // Custom Status Renderer
        finesTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
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

        finesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && finesTable.getSelectedRow() != -1) {
                    onProcessFinePaymentClicked();
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

        centerAlignColumns(finesTable, 0, 1, 4, 5, 6, 7, 8, 9);
        setColumnWidths(finesTable, 55, 65, 120, 150, 85, 90, 85, 85, 100, 75);

        JScrollPane scrollPane = new JScrollPane(finesTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setOpaque(false);
        centerPanel.add(toolbarPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(topSummaryPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        refreshFinesTable();

        return panel;
    }

    private JPanel createFineSummaryStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 10, 0));
        strip.setOpaque(false);
        strip.setBorder(new EmptyBorder(0, 0, 4, 0));

        totalFinesAssessedLabel = new JLabel("$0.00");
        totalFinesUnpaidLabel = new JLabel("$0.00 (0 unpaid)");
        totalFinesCollectedLabel = new JLabel("$0.00 (0 paid)");

        strip.add(createMiniSummaryCard("Total Fines Assessed", totalFinesAssessedLabel, UITheme.PRIMARY));
        strip.add(createMiniSummaryCard("Outstanding / Unpaid", totalFinesUnpaidLabel, UITheme.DANGER));
        strip.add(createMiniSummaryCard("Collected Revenue", totalFinesCollectedLabel, UITheme.ACCENT));

        return strip;
    }

    private JPanel createMiniSummaryCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(4, 2));
        card.setBackground(new Color(0xFA, 0xFB, 0xFC));
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_HEADING);
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void refreshFineMemberFilterCombo() {
        if (fineMemberFilterCombo == null) return;
        fineMemberFilterCombo.removeAllItems();
        fineMemberFilterCombo.addItem("All Members");
        for (Member m : store.members()) {
            fineMemberFilterCombo.addItem(m.getName() + " (#" + m.getMemberId() + ")");
        }
    }

    private void updateFineKpiLabels() {
        double totalAssessed = 0.0;
        double totalUnpaid = 0.0;
        double totalPaid = 0.0;
        int unpaidCount = 0;
        int paidCount = 0;

        for (Fine f : store.fines()) {
            totalAssessed += f.getAmount();
            if (f.isPaid()) {
                totalPaid += f.getAmount();
                paidCount++;
            } else {
                totalUnpaid += f.getAmount();
                unpaidCount++;
            }
        }

        if (totalFinesAssessedLabel != null) {
            totalFinesAssessedLabel.setText(String.format("$%.2f (%d total)", totalAssessed, store.fines().size()));
        }
        if (totalFinesUnpaidLabel != null) {
            totalFinesUnpaidLabel.setText(String.format("$%.2f (%d unpaid)", totalUnpaid, unpaidCount));
        }
        if (totalFinesCollectedLabel != null) {
            totalFinesCollectedLabel.setText(String.format("$%.2f (%d paid)", totalPaid, paidCount));
        }
        if (outstandingFinesKpiLabel != null) {
            outstandingFinesKpiLabel.setText(String.format("$%.2f", totalUnpaid));
        }
    }

    private void refreshFinesTable() {
        if (finesTableModel == null) return;
        finesTableModel.setRowCount(0);

        String statusFilter = fineFilterCombo != null ? (String) fineFilterCombo.getSelectedItem() : "All Fines";
        String memberFilter = fineMemberFilterCombo != null ? (String) fineMemberFilterCombo.getSelectedItem() : "All Members";

        int targetMemberId = -1;
        if (memberFilter != null && !memberFilter.equals("All Members") && memberFilter.contains("#")) {
            try {
                String idSub = memberFilter.substring(memberFilter.indexOf('#') + 1);
                idSub = idSub.substring(0, idSub.indexOf(')'));
                targetMemberId = Integer.parseInt(idSub);
            } catch (Exception ignored) {}
        }

        for (Fine f : store.fines()) {
            // Apply status filter
            if ("Unpaid Fines Only".equals(statusFilter) && f.isPaid()) continue;
            if ("Paid Fines Only".equals(statusFilter) && !f.isPaid()) continue;

            // Apply member filter
            if (targetMemberId != -1 && f.getMemberId() != targetMemberId) continue;

            Member member = store.findMemberById(f.getMemberId());
            String memberName = member != null ? member.getName() : "Member #" + f.getMemberId();

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

            finesTableModel.addRow(new Object[] {
                    f.getFineId(),
                    f.getBorrowingId(),
                    memberName,
                    bookTitle,
                    overdueDays > 0 ? overdueDays + " days" : "-",
                    String.format("$%.2f", f.getAmount()),
                    f.getFineDate() != null ? f.getFineDate().toString() : "-",
                    paidDateStr,
                    methodStr,
                    statusStr
            });
        }

        updateFineKpiLabels();
    }

    private Fine getSelectedFine() {
        if (finesTable == null) return null;
        int selectedRow = finesTable.getSelectedRow();
        if (selectedRow == -1) return null;
        int modelRow = finesTable.convertRowIndexToModel(selectedRow);
        int fineId = (Integer) finesTableModel.getValueAt(modelRow, 0);
        return store.findFineById(fineId);
    }

    // ============================================================================
    // FINE ACTION 1: CALCULATE FINE DIALOG
    // ============================================================================

    private void showCalculateFineDialog(Borrowing preSelectedBorrowing) {
        JDialog dialog = new JDialog(this, "Calculate & Assess Member Fine", true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        JLabel heading = new JLabel("Calculate & Assess Fine");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Compute late return fees for loan records based on overdue days");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Borrowing> borrowingsList = store.borrowings();
        JComboBox<Borrowing> borrowingCombo = new JComboBox<>(borrowingsList.toArray(new Borrowing[0]));
        borrowingCombo.setFont(UITheme.FONT_BODY);
        borrowingCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Borrowing) {
                    Borrowing b = (Borrowing) value;
                    Book book = store.findBookById(b.getBookId());
                    Member member = store.findMemberById(b.getMemberId());
                    String bTitle = book != null ? book.getTitle() : "Book #" + b.getBookId();
                    String mName = member != null ? member.getName() : "Member #" + b.getMemberId();
                    long days = store.calculateOverdueDays(b);
                    String tag = days > 0 ? " [OVERDUE: " + days + "d]" : (b.getStatus() == Borrowing.Status.RETURNED ? " [RETURNED]" : " [ACTIVE]");
                    setText("#" + b.getBorrowingId() + ": " + bTitle + " (" + mName + ")" + tag);
                }
                return this;
            }
        });

        if (preSelectedBorrowing != null) {
            borrowingCombo.setSelectedItem(preSelectedBorrowing);
        }

        JLabel memberNameVal = new JLabel("-");
        memberNameVal.setFont(UITheme.FONT_BODY_BOLD);

        JLabel bookTitleVal = new JLabel("-");
        bookTitleVal.setFont(UITheme.FONT_BODY);

        JLabel dueDateVal = new JLabel("-");
        dueDateVal.setFont(UITheme.FONT_BODY);

        JLabel overdueDaysVal = new JLabel("0 days");
        overdueDaysVal.setFont(UITheme.FONT_BODY_BOLD);
        overdueDaysVal.setForeground(UITheme.DANGER);

        JSpinner rateSpinner = new JSpinner(new SpinnerNumberModel(1.00, 0.10, 100.00, 0.50));
        rateSpinner.setFont(UITheme.FONT_BODY);

        JLabel calculatedAmountVal = new JLabel("$0.00");
        calculatedAmountVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        calculatedAmountVal.setForeground(UITheme.DANGER);

        JCheckBox customAmountCheck = new JCheckBox("Override with Custom Amount");
        customAmountCheck.setFont(UITheme.FONT_SMALL);
        customAmountCheck.setOpaque(false);

        JSpinner customAmountSpinner = new JSpinner(new SpinnerNumberModel(5.00, 0.50, 1000.00, 1.00));
        customAmountSpinner.setFont(UITheme.FONT_BODY);
        customAmountSpinner.setEnabled(false);

        customAmountCheck.addActionListener(e -> {
            boolean isCustom = customAmountCheck.isSelected();
            customAmountSpinner.setEnabled(isCustom);
            rateSpinner.setEnabled(!isCustom);
            if (isCustom) {
                calculatedAmountVal.setText(String.format("$%.2f", (Double) customAmountSpinner.getValue()));
            } else {
                Borrowing b = (Borrowing) borrowingCombo.getSelectedItem();
                long days = store.calculateOverdueDays(b);
                double rate = (Double) rateSpinner.getValue();
                calculatedAmountVal.setText(String.format("$%.2f", days * rate));
            }
        });

        customAmountSpinner.addChangeListener(e -> {
            if (customAmountCheck.isSelected()) {
                calculatedAmountVal.setText(String.format("$%.2f", (Double) customAmountSpinner.getValue()));
            }
        });

        JTextField notesField = new JTextField();

        Runnable updateCalculations = () -> {
            Borrowing b = (Borrowing) borrowingCombo.getSelectedItem();
            if (b != null) {
                Member m = store.findMemberById(b.getMemberId());
                Book book = store.findBookById(b.getBookId());
                long days = store.calculateOverdueDays(b);

                memberNameVal.setText(m != null ? m.getName() + " (ID: #" + m.getMemberId() + ")" : "Member #" + b.getMemberId());
                bookTitleVal.setText(book != null ? book.getTitle() : "Book #" + b.getBookId());
                dueDateVal.setText(b.getDueDate() != null ? b.getDueDate().toString() : "-");
                overdueDaysVal.setText(days + " day(s) overdue");

                if (customAmountCheck.isSelected()) {
                    calculatedAmountVal.setText(String.format("$%.2f", (Double) customAmountSpinner.getValue()));
                } else {
                    double rate = (Double) rateSpinner.getValue();
                    double total = Math.max(0, days * rate);
                    calculatedAmountVal.setText(String.format("$%.2f", total));
                    if (notesField.getText().isEmpty() && days > 0) {
                        notesField.setText("Late return fee: " + days + " days overdue @ $" + String.format("%.2f", rate) + "/day");
                    }
                }
            }
        };

        borrowingCombo.addActionListener(e -> updateCalculations.run());
        rateSpinner.addChangeListener(e -> updateCalculations.run());
        updateCalculations.run();

        addFormField(form, gbc, 0, "Select Loan:", borrowingCombo);
        addFormField(form, gbc, 1, "Member:", memberNameVal);
        addFormField(form, gbc, 2, "Book:", bookTitleVal);
        addFormField(form, gbc, 3, "Due Date:", dueDateVal);
        addFormField(form, gbc, 4, "Overdue Period:", overdueDaysVal);
        addFormField(form, gbc, 5, "Daily Rate ($/day):", rateSpinner);
        addFormField(form, gbc, 6, "Custom Amount:", customAmountCheck);
        addFormField(form, gbc, 7, "Amount Field:", customAmountSpinner);
        addFormField(form, gbc, 8, "Total Fine Due:", calculatedAmountVal);
        addFormField(form, gbc, 9, "Reason / Notes:", notesField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveFineBtn = UITheme.primaryButton("Assess & Save Fine");
        saveFineBtn.addActionListener(e -> {
            Borrowing selectedBorrowing = (Borrowing) borrowingCombo.getSelectedItem();
            if (selectedBorrowing == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a borrowing record.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double amount;
            if (customAmountCheck.isSelected()) {
                amount = (Double) customAmountSpinner.getValue();
            } else {
                long days = store.calculateOverdueDays(selectedBorrowing);
                double rate = (Double) rateSpinner.getValue();
                amount = days * rate;
            }

            if (amount <= 0) {
                int proceed = JOptionPane.showConfirmDialog(dialog,
                        "The calculated fine amount is $0.00 because this loan has 0 overdue days.\nDo you still wish to assess this fine?",
                        "Confirm Fine Assessment",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (proceed != JOptionPane.YES_OPTION) return;
            }

            Fine existingFine = store.findFineByBorrowingId(selectedBorrowing.getBorrowingId());
            if (existingFine != null && !existingFine.isPaid()) {
                int replaceChoice = JOptionPane.showConfirmDialog(dialog,
                        "An unpaid fine of $" + String.format("%.2f", existingFine.getAmount()) +
                        " already exists for this borrowing (Fine #" + existingFine.getFineId() + ").\n\n" +
                        "Would you like to update the existing fine amount to $" + String.format("%.2f", amount) + "?",
                        "Fine Already Exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (replaceChoice == JOptionPane.YES_OPTION) {
                    existingFine.setAmount(amount);
                    existingFine.setFineDate(LocalDate.now());
                    existingFine.setNotes(notesField.getText().trim());
                    store.updateFine(existingFine);
                    refreshFinesTable();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Fine #" + existingFine.getFineId() + " updated successfully to $" + String.format("%.2f", amount) + "!", "Fine Updated", JOptionPane.INFORMATION_MESSAGE);
                }
                return;
            }

            Fine newFine = new Fine(
                    store.nextFineId(),
                    selectedBorrowing.getBorrowingId(),
                    selectedBorrowing.getMemberId(),
                    amount,
                    false,
                    LocalDate.now(),
                    null,
                    null,
                    notesField.getText().trim()
            );

            store.addFine(newFine);
            refreshFinesTable();
            dialog.dispose();

            Member m = store.findMemberById(selectedBorrowing.getMemberId());
            String mName = m != null ? m.getName() : "Member #" + selectedBorrowing.getMemberId();
            JOptionPane.showMessageDialog(this,
                    "Fine of $" + String.format("%.2f", amount) + " successfully assessed to " + mName + "!\nFine ID: #" + newFine.getFineId(),
                    "Fine Assessed",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveFineBtn);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // ============================================================================
    // FINE ACTION 2: AUTO-ASSESS ALL OVERDUE FINES
    // ============================================================================

    private void autoAssessAllOverdueFines() {
        double defaultRate = 1.00;
        int newlyAssessed = 0;
        int updated = 0;
        double totalNewAmount = 0.0;

        for (Borrowing b : store.borrowings()) {
            long overdueDays = store.calculateOverdueDays(b);
            if (overdueDays > 0) {
                double calculatedAmount = overdueDays * defaultRate;
                Fine existingFine = store.findFineByBorrowingId(b.getBorrowingId());

                if (existingFine == null) {
                    Fine newFine = new Fine(
                            store.nextFineId(),
                            b.getBorrowingId(),
                            b.getMemberId(),
                            calculatedAmount,
                            false,
                            LocalDate.now(),
                            null,
                            null,
                            "Auto-assessed: " + overdueDays + " days overdue @ $" + String.format("%.2f", defaultRate) + "/day"
                    );
                    store.addFine(newFine);
                    newlyAssessed++;
                    totalNewAmount += calculatedAmount;
                } else if (!existingFine.isPaid() && existingFine.getAmount() < calculatedAmount) {
                    existingFine.setAmount(calculatedAmount);
                    existingFine.setNotes("Updated auto-assessment: " + overdueDays + " days overdue @ $" + String.format("%.2f", defaultRate) + "/day");
                    store.updateFine(existingFine);
                    updated++;
                }
            }
        }

        refreshFinesTable();

        if (newlyAssessed == 0 && updated == 0) {
            JOptionPane.showMessageDialog(this,
                    "No new overdue loans requiring assessment found.\nAll overdue borrowings already have up-to-date fine records.",
                    "Auto-Assessment Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Batch Auto-Assessment Complete!\n\n" +
                    "• Newly assessed fines: " + newlyAssessed + " (Total: $" + String.format("%.2f", totalNewAmount) + ")\n" +
                    "• Updated overdue fines: " + updated + "\n" +
                    "Standard Rate Applied: $" + String.format("%.2f", defaultRate) + " / day",
                    "Fines Successfully Calculated",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ============================================================================
    // FINE ACTION 3: VIEW MEMBER FINES DIALOG
    // ============================================================================

    private void showMemberFinesDialog(Member initialMember) {
        JDialog dialog = new JDialog(this, "Member Fine Account Overview", true);
        dialog.setSize(620, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        JLabel heading = new JLabel("Member Fine Overview & Balance");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Inspect individual member fine history, balances, and settle dues");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);

        // Member selector row
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selectPanel.setOpaque(false);
        JLabel selectLbl = new JLabel("Select Member:");
        selectLbl.setFont(UITheme.FONT_BODY_BOLD);

        JComboBox<Member> memberCombo = new JComboBox<>(store.members().toArray(new Member[0]));
        memberCombo.setFont(UITheme.FONT_BODY);
        memberCombo.setPreferredSize(new Dimension(280, 30));
        memberCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Member) {
                    Member m = (Member) value;
                    setText(m.getName() + " (ID: #" + m.getMemberId() + ", " + m.getEmail() + ")");
                }
                return this;
            }
        });

        if (initialMember != null) {
            memberCombo.setSelectedItem(initialMember);
        }

        selectPanel.add(selectLbl);
        selectPanel.add(memberCombo);

        // Member Summary Banner
        JPanel summaryCard = UITheme.card();
        summaryCard.setLayout(new GridLayout(1, 3, 10, 0));
        JLabel totalAssessedVal = new JLabel("$0.00");
        JLabel totalPaidVal = new JLabel("$0.00");
        JLabel totalOutstandingVal = new JLabel("$0.00");

        summaryCard.add(createMiniSummaryCard("Total Assessed", totalAssessedVal, UITheme.PRIMARY));
        summaryCard.add(createMiniSummaryCard("Total Paid", totalPaidVal, UITheme.ACCENT));
        summaryCard.add(createMiniSummaryCard("Balance Due", totalOutstandingVal, UITheme.DANGER));

        // Member fines table
        String[] cols = { "Fine ID", "Borrow ID", "Amount ($)", "Assessed Date", "Paid Date", "Method", "Status" };
        DefaultTableModel memberFinesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable memberFinesTable = new JTable(memberFinesModel);
        memberFinesTable.setFont(UITheme.FONT_BODY);
        memberFinesTable.setRowHeight(26);
        memberFinesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UITheme.styleTableHeader(memberFinesTable.getTableHeader());
        centerAlignColumns(memberFinesTable, 0, 1, 2, 3, 4, 5, 6);

        memberFinesTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String valStr = value != null ? value.toString() : "";
                if ("PAID".equalsIgnoreCase(valStr)) {
                    l.setForeground(new Color(0x1B, 0x7A, 0x4B));
                    l.setFont(UITheme.FONT_BODY_BOLD);
                } else {
                    l.setForeground(UITheme.DANGER);
                    l.setFont(UITheme.FONT_BODY_BOLD);
                }
                return l;
            }
        });

        JScrollPane scrollPane = new JScrollPane(memberFinesTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        Runnable refreshMemberFinesView = () -> {
            Member selected = (Member) memberCombo.getSelectedItem();
            memberFinesModel.setRowCount(0);
            if (selected == null) return;

            List<Fine> list = store.findFinesByMemberId(selected.getMemberId());
            double assessed = 0.0;
            double paid = 0.0;
            double unpaid = 0.0;

            for (Fine f : list) {
                assessed += f.getAmount();
                if (f.isPaid()) {
                    paid += f.getAmount();
                } else {
                    unpaid += f.getAmount();
                }

                memberFinesModel.addRow(new Object[] {
                        f.getFineId(),
                        f.getBorrowingId(),
                        String.format("$%.2f", f.getAmount()),
                        f.getFineDate() != null ? f.getFineDate().toString() : "-",
                        f.getPaidDate() != null ? f.getPaidDate().toString() : "-",
                        f.getPaymentMethod() != null ? f.getPaymentMethod() : "-",
                        f.isPaid() ? "PAID" : "UNPAID"
                });
            }

            totalAssessedVal.setText(String.format("$%.2f", assessed));
            totalPaidVal.setText(String.format("$%.2f", paid));
            totalOutstandingVal.setText(String.format("$%.2f", unpaid));
        };

        memberCombo.addActionListener(e -> refreshMemberFinesView.run());
        refreshMemberFinesView.run();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton closeBtn = UITheme.secondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        JButton payAllBtn = UITheme.accentButton("Pay All Unpaid Fines");
        payAllBtn.addActionListener(e -> {
            Member selected = (Member) memberCombo.getSelectedItem();
            if (selected == null) return;

            List<Fine> memberFines = store.findFinesByMemberId(selected.getMemberId());
            long unpaidCount = memberFines.stream().filter(f -> !f.isPaid()).count();
            if (unpaidCount == 0) {
                JOptionPane.showMessageDialog(dialog, "This member currently has no unpaid fines.", "Zero Balance", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            double unpaidSum = memberFines.stream().filter(f -> !f.isPaid()).mapToDouble(Fine::getAmount).sum();
            String[] methods = { "Cash", "Credit/Debit Card", "Online Transfer", "Library Credit" };
            String method = (String) JOptionPane.showInputDialog(dialog,
                    "Settle all " + unpaidCount + " unpaid fine(s) for " + selected.getName() + ".\n" +
                    "Total Amount Due: $" + String.format("%.2f", unpaidSum) + "\n\n" +
                    "Select Payment Method:",
                    "Pay All Member Fines",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    methods,
                    methods[0]);

            if (method != null) {
                int settled = store.payAllFinesForMember(selected.getMemberId(), method);
                refreshMemberFinesView.run();
                refreshFinesTable();
                JOptionPane.showMessageDialog(dialog,
                        "Successfully settled " + settled + " fine(s) for " + selected.getName() + "!\n" +
                        "Amount Paid: $" + String.format("%.2f", unpaidSum) + " via " + method,
                        "Payment Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnPanel.add(payAllBtn);
        btnPanel.add(closeBtn);

        JPanel topBox = new JPanel(new BorderLayout(0, 8));
        topBox.setOpaque(false);
        topBox.add(headerPanel, BorderLayout.NORTH);
        topBox.add(selectPanel, BorderLayout.CENTER);
        topBox.add(summaryCard, BorderLayout.SOUTH);

        root.add(topBox, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // ============================================================================
    // FINE ACTION 4: MANAGE FINE PAYMENT
    // ============================================================================

    private void onProcessFinePaymentClicked() {
        Fine fine = getSelectedFine();
        if (fine == null) {
            JOptionPane.showMessageDialog(this, "Please select a fine from the table to process payment.", "No Fine Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (fine.isPaid()) {
            JOptionPane.showMessageDialog(this,
                    "Fine #" + fine.getFineId() + " is already PAID.\n\n" +
                    "Amount: $" + String.format("%.2f", fine.getAmount()) + "\n" +
                    "Paid Date: " + fine.getPaidDate() + "\n" +
                    "Payment Method: " + (fine.getPaymentMethod() != null ? fine.getPaymentMethod() : "Cash") + "\n" +
                    "Notes: " + (fine.getNotes() != null && !fine.getNotes().isEmpty() ? fine.getNotes() : "-"),
                    "Fine Already Paid",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        showProcessPaymentDialog(fine);
    }

    private void showProcessPaymentDialog(Fine fine) {
        JDialog dialog = new JDialog(this, "Process Fine Payment", true);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        JLabel heading = new JLabel("Process Fine Payment (ID: #" + fine.getFineId() + ")");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Record member payment details and settle outstanding fine");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);

        Member member = store.findMemberById(fine.getMemberId());
        String memberName = member != null ? member.getName() + " (#" + member.getMemberId() + ")" : "Member #" + fine.getMemberId();

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

        JLabel memberVal = new JLabel(memberName);
        memberVal.setFont(UITheme.FONT_BODY_BOLD);

        JLabel bookVal = new JLabel(bookTitle);
        bookVal.setFont(UITheme.FONT_BODY);

        JLabel amountVal = new JLabel(String.format("$%.2f", fine.getAmount()));
        amountVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountVal.setForeground(new Color(0x1B, 0x7A, 0x4B));

        JLabel assessedDateVal = new JLabel(fine.getFineDate() != null ? fine.getFineDate().toString() : "-");
        assessedDateVal.setFont(UITheme.FONT_BODY);

        String[] methods = { "Cash", "Credit/Debit Card", "Online Transfer", "Library Credit" };
        JComboBox<String> methodCombo = new JComboBox<>(methods);
        methodCombo.setFont(UITheme.FONT_BODY);

        JTextField paymentNotesField = new JTextField(fine.getNotes() != null ? fine.getNotes() : "");

        addFormField(form, gbc, 0, "Member:", memberVal);
        addFormField(form, gbc, 1, "Book / Borrowing:", bookVal);
        addFormField(form, gbc, 2, "Fine Assessed Date:", assessedDateVal);
        addFormField(form, gbc, 3, "Amount Payable:", amountVal);
        addFormField(form, gbc, 4, "Payment Method:", methodCombo);
        addFormField(form, gbc, 5, "Payment Notes / Ref:", paymentNotesField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton confirmPayBtn = UITheme.accentButton("Confirm & Settle Payment");
        confirmPayBtn.addActionListener(e -> {
            String selectedMethod = (String) methodCombo.getSelectedItem();
            String notes = paymentNotesField.getText().trim();

            fine.setPaid(true);
            fine.setPaidDate(LocalDate.now());
            fine.setPaymentMethod(selectedMethod);
            if (!notes.isEmpty()) {
                fine.setNotes(notes);
            }

            store.updateFine(fine);
            refreshFinesTable();
            dialog.dispose();

            JOptionPane.showMessageDialog(this,
                    "Payment of $" + String.format("%.2f", fine.getAmount()) + " received successfully!\n\n" +
                    "Receipt Summary:\n" +
                    "• Fine ID: #" + fine.getFineId() + "\n" +
                    "• Member: " + memberName + "\n" +
                    "• Payment Method: " + selectedMethod + "\n" +
                    "• Date: " + LocalDate.now(),
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

    private void onWaiveFineClicked() {
        Fine fine = getSelectedFine();
        if (fine == null) {
            JOptionPane.showMessageDialog(this, "Please select a fine from the table to waive.", "No Fine Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to waive and remove Fine #" + fine.getFineId() + "?\n\n" +
                "Amount: $" + String.format("%.2f", fine.getAmount()) + "\n" +
                "Member ID: #" + fine.getMemberId() + "\n" +
                "Status: " + (fine.isPaid() ? "PAID" : "UNPAID") + "\n\n" +
                "This action cannot be undone.",
                "Confirm Waive Fine",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            boolean deleted = store.deleteFine(fine.getFineId());
            if (deleted) {
                refreshFinesTable();
                JOptionPane.showMessageDialog(this, "Fine #" + fine.getFineId() + " was successfully waived and removed.", "Fine Waived", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to waive fine.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ============================================================================
    // UI HELPERS
    // ============================================================================

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
            ((JTextField) field).setPreferredSize(new Dimension(field.getPreferredSize().width, 28));
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
                JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
