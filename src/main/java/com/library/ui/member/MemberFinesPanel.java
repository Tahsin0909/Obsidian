package com.library.ui.member;

import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Fine;
import com.library.model.Member;
import com.library.store.DataStore;
import com.library.ui.MemberDashboard;
import com.library.ui.UIHelper;
import com.library.ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel managing fines and fees for the logged-in member.
 * Features metric summaries, status/search filtering, fine payment processing,
 * batch fine settlement, and detailed receipt dialogues.
 */
public class MemberFinesPanel extends JPanel {

    private final MemberDashboard dashboard;
    private final Member member;
    private final DataStore store = DataStore.getInstance();

    private DefaultTableModel finesTableModel;
    private JTable finesTable;
    private JComboBox<String> fineFilterCombo;
    private JTextField fineSearchField;
    private JLabel totalFinesAssessedLabel;
    private JLabel totalFinesUnpaidLabel;
    private JLabel totalFinesPaidLabel;

    public MemberFinesPanel(MemberDashboard dashboard, Member member) {
        this.dashboard = dashboard;
        this.member = member;

        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        buildUI();
    }

    private void buildUI() {
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
        fineFilterCombo.addActionListener(e -> refreshTable());

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

        UIHelper.centerAlignColumns(finesTable, 0, 1, 3, 4, 5, 6, 7, 8);
        UIHelper.setColumnWidths(finesTable, 60, 65, 200, 90, 95, 90, 90, 110, 80);

        JScrollPane scrollPane = new JScrollPane(finesTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(toolbarPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(summaryStrip, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel createFineSummaryStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 14, 0));
        strip.setOpaque(false);
        strip.setBorder(new EmptyBorder(0, 0, 4, 0));

        totalFinesAssessedLabel = new JLabel("$0.00");
        totalFinesUnpaidLabel = new JLabel("$0.00 (0 unpaid)");
        totalFinesPaidLabel = new JLabel("$0.00 (0 paid)");

        strip.add(UIHelper.createMiniSummaryCard("Total Fines Assessed", totalFinesAssessedLabel, UITheme.PRIMARY));
        strip.add(UIHelper.createMiniSummaryCard("Outstanding Balance / Unpaid", totalFinesUnpaidLabel, UITheme.DANGER));
        strip.add(UIHelper.createMiniSummaryCard("Total Settled / Paid", totalFinesPaidLabel, UITheme.ACCENT));

        return strip;
    }

    public void refreshTable() {
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

        if (dashboard != null) {
            dashboard.refreshKpiCards();
        }
    }

    public Fine getSelectedFine() {
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

    public void showProcessPaymentDialog(Fine fine) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Pay Fine (Fine #" + fine.getFineId() + ")", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(owner);
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

        UIHelper.addFormField(form, gbc, 0, "Fine Reference:", fineIdVal);
        UIHelper.addFormField(form, gbc, 1, "Book Title:", bookVal);
        UIHelper.addFormField(form, gbc, 2, "Assessed Date:", assessedDateVal);
        UIHelper.addFormField(form, gbc, 3, "Amount Payable:", amountVal);
        UIHelper.addFormField(form, gbc, 4, "Payment Method:", methodCombo);
        UIHelper.addFormField(form, gbc, 5, "Payment Notes / Ref:", notesField);

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
            refreshTable();
            if (dashboard != null && dashboard.getBorrowingsPanel() != null) {
                dashboard.getBorrowingsPanel().refreshTable();
            }
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
            refreshTable();
            if (dashboard != null && dashboard.getBorrowingsPanel() != null) {
                dashboard.getBorrowingsPanel().refreshTable();
            }
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

    public void showFineDetailsDialog(Fine fine) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Fine Details & Receipt (Fine #" + fine.getFineId() + ")", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(owner);
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

        UIHelper.addFormField(form, gbc, 0, "Fine ID:", fineIdVal);
        UIHelper.addFormField(form, gbc, 1, "Member:", memberVal);
        UIHelper.addFormField(form, gbc, 2, "Loan / Book:", loanVal);
        UIHelper.addFormField(form, gbc, 3, "Overdue Duration:", overdueVal);
        UIHelper.addFormField(form, gbc, 4, "Fine Amount:", amountVal);
        UIHelper.addFormField(form, gbc, 5, "Status:", statusVal);
        UIHelper.addFormField(form, gbc, 6, "Assessed Date:", fineDateVal);
        UIHelper.addFormField(form, gbc, 7, "Paid Date:", paidDateVal);
        UIHelper.addFormField(form, gbc, 8, "Payment Method:", methodVal);
        UIHelper.addFormField(form, gbc, 9, "Notes:", notesVal);

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
}
