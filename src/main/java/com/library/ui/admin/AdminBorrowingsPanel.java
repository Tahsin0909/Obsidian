package com.library.ui.admin;

import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Member;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Panel managing borrowing records, book issue operations, returns, and overdue
 * tracking.
 */
public class AdminBorrowingsPanel extends JPanel {

    private final AdminDashboard dashboard;
    private final DataStore store = DataStore.getInstance();

    private DefaultTableModel borrowingsTableModel;
    private JTable borrowingsTable;
    private JComboBox<String> borrowingFilterCombo;
    private JTextField borrowingSearchField;

    public AdminBorrowingsPanel(AdminDashboard dashboard) {
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

        JPanel searchAndFilterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchAndFilterBox.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        searchLbl.setForeground(UITheme.TEXT_DARK);
        borrowingSearchField = new JTextField();
        UITheme.styleTextField(borrowingSearchField);
        borrowingSearchField.setPreferredSize(new Dimension(170, 30));

        JLabel filterLbl = new JLabel("View:");
        filterLbl.setFont(UITheme.FONT_BODY_BOLD);
        filterLbl.setForeground(UITheme.TEXT_DARK);

        String[] filterOptions = { "All Records (History)", "Active Borrowings Only", "Overdue Books Only" };
        borrowingFilterCombo = new JComboBox<>(filterOptions);
        UITheme.styleComboBox(borrowingFilterCombo);
        borrowingFilterCombo.setPreferredSize(new Dimension(175, 30));
        borrowingFilterCombo.addActionListener(e -> refresh());

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
        calculateFineBtn.addActionListener(e -> {
            if (dashboard.getFinesPanel() != null) {
                dashboard.getFinesPanel().showCalculateFineDialog(getSelectedBorrowing());
            }
        });

        actionsBox.add(borrowBtn);
        actionsBox.add(returnBtn);
        actionsBox.add(calculateFineBtn);

        toolbar.add(searchAndFilterBox, BorderLayout.WEST);
        toolbar.add(actionsBox, BorderLayout.EAST);

        String[] cols = { "Borrow ID", "Member Name", "Book Title", "Borrow Date", "Due Date", "Return Date",
                "Status" };
        borrowingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        borrowingsTable = new JTable(borrowingsTableModel);
        borrowingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        borrowingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UITheme.styleTable(borrowingsTable);

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
                String text = borrowingSearchField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        UIHelper.centerAlignColumns(borrowingsTable, 0, 3, 4, 5, 6);
        UIHelper.setColumnWidths(borrowingsTable, 65, 130, 170, 85, 85, 85, 80);

        JScrollPane scrollPane = new JScrollPane(borrowingsTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        add(toolbar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        if (borrowingsTableModel == null)
            return;
        borrowingsTableModel.setRowCount(0);

        String selectedFilter = borrowingFilterCombo != null ? (String) borrowingFilterCombo.getSelectedItem()
                : "All Records (History)";

        for (Borrowing b : store.borrowings()) {
            boolean include = true;
            if ("Active Borrowings Only".equals(selectedFilter)) {
                include = (b.getStatus() == Borrowing.Status.ACTIVE);
            } else if ("Overdue Books Only".equals(selectedFilter)) {
                include = b.isOverdue();
            }

            if (!include)
                continue;

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

    public Borrowing getSelectedBorrowing() {
        if (borrowingsTable == null)
            return null;
        int selectedRow = borrowingsTable.getSelectedRow();
        if (selectedRow == -1)
            return null;
        int modelRow = borrowingsTable.convertRowIndexToModel(selectedRow);
        int borrowingId = (Integer) borrowingsTableModel.getValueAt(modelRow, 0);
        for (Borrowing b : store.borrowings()) {
            if (b.getBorrowingId() == borrowingId) {
                return b;
            }
        }
        return null;
    }

    private void showBorrowBookDialog() {
        JDialog dialog = new JDialog(dashboard, "Borrow Book for Member", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(dashboard);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(UITheme.CARD_BG);
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
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
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
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
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

        UIHelper.addFormField(form, gbc, 0, "Select Member:", memberCombo);
        UIHelper.addFormField(form, gbc, 1, "Select Book:", bookCombo);
        UIHelper.addFormField(form, gbc, 2, "Loan Duration (Days):", daysSpinner);
        UIHelper.addFormField(form, gbc, 3, "Calculated Due Date:", dueDatePreview);

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
                JOptionPane.showMessageDialog(dialog, "Please select an active member.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (book == null) {
                JOptionPane.showMessageDialog(dialog, "No book selected or no copies currently available.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Borrowing borrowing = store.borrowBook(member.getMemberId(), book.getBookId(), days);
            if (borrowing != null) {
                refresh();
                if (dashboard.getBooksPanel() != null) {
                    dashboard.getBooksPanel().refresh();
                }
                dashboard.refreshKpis();

                dialog.dispose();
                JOptionPane.showMessageDialog(dashboard,
                        "Book \"" + book.getTitle() + "\" successfully issued to " + member.getName() + "!\n" +
                                "Due Date: " + borrowing.getDueDate(),
                        "Book Issued",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to issue book. The book might be out of stock.", "Error",
                        JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(dashboard, "Borrowing record not found.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (target.getStatus() == Borrowing.Status.RETURNED) {
                JOptionPane.showMessageDialog(dashboard,
                        "This book has already been returned on " + target.getReturnDate() + ".", "Already Returned",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Member member = store.findMemberById(target.getMemberId());
            String memberName = member != null ? member.getName() : "Member #" + target.getMemberId();
            Book book = store.findBookById(target.getBookId());
            String bookTitle = book != null ? book.getTitle() : "Book #" + target.getBookId();

            int choice = JOptionPane.showConfirmDialog(dashboard,
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
                    refresh();
                    if (dashboard.getBooksPanel() != null) {
                        dashboard.getBooksPanel().refresh();
                    }
                    dashboard.refreshKpis();

                    if (target.getDueDate() != null && LocalDate.now().isAfter(target.getDueDate())) {
                        long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(target.getDueDate(),
                                LocalDate.now());
                        int fineChoice = JOptionPane.showConfirmDialog(dashboard,
                                "Book returned successfully!\n\n" +
                                        "Note: This book was overdue by " + overdueDays + " day(s).\n" +
                                        "Would you like to assess/calculate a fine for this return now?",
                                "Overdue Return - Assess Fine?",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);
                        if (fineChoice == JOptionPane.YES_OPTION && dashboard.getFinesPanel() != null) {
                            dashboard.getFinesPanel().showCalculateFineDialog(target);
                        }
                    } else {
                        JOptionPane.showMessageDialog(dashboard,
                                "Book \"" + bookTitle + "\" returned successfully!\nStock quantity restored.",
                                "Return Processed", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(dashboard, "Failed to process return.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            showSelectReturnDialog();
        }
    }

    private void showSelectReturnDialog() {
        List<Borrowing> activeList = new ArrayList<>();
        for (Borrowing b : store.borrowings()) {
            if (b.getStatus() == Borrowing.Status.ACTIVE) {
                activeList.add(b);
            }
        }

        if (activeList.isEmpty()) {
            JOptionPane.showMessageDialog(dashboard, "There are currently no active borrowings to return.",
                    "No Active Borrowings", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(dashboard, "Process Book Return", true);
        dialog.setSize(500, 260);
        dialog.setLocationRelativeTo(dashboard);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(UITheme.CARD_BG);
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
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
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

        UIHelper.addFormField(form, gbc, 0, "Active Loan:", combo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton returnBtn = UITheme.accentButton("Confirm Return");
        returnBtn.addActionListener(e -> {
            Borrowing selected = (Borrowing) combo.getSelectedItem();
            if (selected == null)
                return;

            boolean success = store.returnBook(selected.getBorrowingId());
            if (success) {
                refresh();
                if (dashboard.getBooksPanel() != null) {
                    dashboard.getBooksPanel().refresh();
                }
                dashboard.refreshKpis();
                dialog.dispose();

                Book book = store.findBookById(selected.getBookId());
                String bTitle = book != null ? book.getTitle() : "Book #" + selected.getBookId();
                JOptionPane.showMessageDialog(dashboard,
                        "Book \"" + bTitle + "\" was returned successfully!\nStock quantity restored.",
                        "Return Processed", JOptionPane.INFORMATION_MESSAGE);
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
}
