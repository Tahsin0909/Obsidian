package com.library.ui.admin;

import com.library.model.Book;
import com.library.model.Category;
import com.library.store.DataStore;
import com.library.ui.AdminDashboard;
import com.library.ui.UIHelper;
import com.library.ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;

/**
 * Panel managing the library book catalog, inventory counts, and CRUD operations.
 */
public class AdminBooksPanel extends JPanel {

    private final AdminDashboard dashboard;
    private final DataStore store = DataStore.getInstance();

    private DefaultTableModel booksTableModel;
    private JTable booksTable;
    private JTextField searchField;

    public AdminBooksPanel(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        // Responsive Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 8));
        toolbar.setOpaque(false);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBox.setOpaque(false);
        JLabel searchLbl = new JLabel("Search Books:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        searchField = new JTextField();
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

        UIHelper.centerAlignColumns(booksTable, 0, 1, 6, 7, 8);
        UIHelper.setColumnWidths(booksTable, 45, 110, 160, 120, 110, 100, 50, 70, 70);

        JScrollPane scrollPane = new JScrollPane(booksTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        add(toolbar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
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

    public Book getSelectedBook() {
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
            JOptionPane.showMessageDialog(dashboard, "Please select a book from the table to update.", "No Book Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showUpdateBookDialog(book);
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(dashboard, "Add New Book", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(dashboard);
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

        UIHelper.addFormField(form, gbc, 0, "ISBN:", isbnField);
        UIHelper.addFormField(form, gbc, 1, "Book Title:", titleField);
        UIHelper.addFormField(form, gbc, 2, "Author:", authorField);
        UIHelper.addFormField(form, gbc, 3, "Category:", categoryCombo);
        UIHelper.addFormField(form, gbc, 4, "Publisher:", publisherField);
        UIHelper.addFormField(form, gbc, 5, "Publication Year:", yearSpinner);
        UIHelper.addFormField(form, gbc, 6, "Total Copies / Quantity:", qtySpinner);

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
            refresh();
            dashboard.refreshKpis();

            dialog.dispose();
            JOptionPane.showMessageDialog(dashboard, "Book \"" + newBook.getTitle() + "\" added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        JDialog dialog = new JDialog(dashboard, "Update Book Details", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(dashboard);
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

        UIHelper.addFormField(form, gbc, 0, "ISBN:", isbnField);
        UIHelper.addFormField(form, gbc, 1, "Book Title:", titleField);
        UIHelper.addFormField(form, gbc, 2, "Author:", authorField);
        UIHelper.addFormField(form, gbc, 3, "Category:", categoryCombo);
        UIHelper.addFormField(form, gbc, 4, "Publisher:", publisherField);
        UIHelper.addFormField(form, gbc, 5, "Publication Year:", yearSpinner);
        UIHelper.addFormField(form, gbc, 6, "Total Copies / Quantity:", qtySpinner);

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
            refresh();
            if (dashboard.getBorrowingsPanel() != null) {
                dashboard.getBorrowingsPanel().refresh();
            }

            dialog.dispose();
            JOptionPane.showMessageDialog(dashboard, "Book \"" + book.getTitle() + "\" updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(dashboard, "Please select a book from the table to delete.", "No Book Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (store.hasActiveBorrowingsForBook(book.getBookId())) {
            JOptionPane.showMessageDialog(dashboard,
                    "Cannot delete \"" + book.getTitle() + "\" because copy/copies are currently borrowed by member(s).\n" +
                    "Please ensure all borrowed copies are returned before deleting this book.",
                    "Active Borrowings Exist",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(dashboard,
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
                refresh();
                if (dashboard.getBorrowingsPanel() != null) {
                    dashboard.getBorrowingsPanel().refresh();
                }
                dashboard.refreshKpis();
                JOptionPane.showMessageDialog(dashboard, "Book \"" + book.getTitle() + "\" deleted successfully.", "Book Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dashboard, "Failed to delete book. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
