package com.library.ui.admin;

import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Fine;
import com.library.model.Member;
import com.library.store.DataStore;
import com.library.ui.AdminDashboard;
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
 * Panel managing fine assessments, auto-calculations, payment settlement, member balance overviews, and fee waivers.
 */
public class AdminFinesPanel extends JPanel {

    private final AdminDashboard dashboard;
    private final DataStore store = DataStore.getInstance();

    private DefaultTableModel finesTableModel;
    private JTable finesTable;
    private JComboBox<String> fineFilterCombo;
    private JComboBox<String> fineMemberFilterCombo;
    private JTextField fineSearchField;
    private JLabel totalFinesAssessedLabel;
    private JLabel totalFinesUnpaidLabel;
    private JLabel totalFinesCollectedLabel;

    public AdminFinesPanel(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(14, 14, 14, 14));

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
        fineFilterCombo.addActionListener(e -> refresh());

        JLabel memberLbl = new JLabel("Member:");
        memberLbl.setFont(UITheme.FONT_BODY_BOLD);
        fineMemberFilterCombo = new JComboBox<>();
        fineMemberFilterCombo.setFont(UITheme.FONT_BODY);
        fineMemberFilterCombo.setPreferredSize(new Dimension(150, 28));
        refreshMemberFilterCombo();
        fineMemberFilterCombo.addActionListener(e -> refresh());

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

        UIHelper.centerAlignColumns(finesTable, 0, 1, 4, 5, 6, 7, 8, 9);
        UIHelper.setColumnWidths(finesTable, 55, 65, 120, 150, 85, 90, 85, 85, 100, 75);

        JScrollPane scrollPane = new JScrollPane(finesTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setOpaque(false);
        centerPanel.add(toolbarPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(topSummaryPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        refresh();
    }

    private JPanel createFineSummaryStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 10, 0));
        strip.setOpaque(false);
        strip.setBorder(new EmptyBorder(0, 0, 4, 0));

        totalFinesAssessedLabel = new JLabel("$0.00");
        totalFinesUnpaidLabel = new JLabel("$0.00 (0 unpaid)");
        totalFinesCollectedLabel = new JLabel("$0.00 (0 paid)");

        strip.add(UIHelper.createMiniSummaryCard("Total Fines Assessed", totalFinesAssessedLabel, UITheme.PRIMARY));
        strip.add(UIHelper.createMiniSummaryCard("Outstanding / Unpaid", totalFinesUnpaidLabel, UITheme.DANGER));
        strip.add(UIHelper.createMiniSummaryCard("Collected Revenue", totalFinesCollectedLabel, UITheme.ACCENT));

        return strip;
    }

    public void refreshMemberFilterCombo() {
        if (fineMemberFilterCombo == null) return;
        fineMemberFilterCombo.removeAllItems();
        fineMemberFilterCombo.addItem("All Members");
        for (Member m : store.members()) {
            fineMemberFilterCombo.addItem(m.getName() + " (#" + m.getMemberId() + ")");
        }
    }

    public void refresh() {
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

        if (totalFinesAssessedLabel != null) {
            totalFinesAssessedLabel.setText(String.format("$%.2f (%d total)", totalAssessed, store.fines().size()));
        }
        if (totalFinesUnpaidLabel != null) {
            totalFinesUnpaidLabel.setText(String.format("$%.2f (%d unpaid)", totalUnpaid, unpaidCount));
        }
        if (totalFinesCollectedLabel != null) {
            totalFinesCollectedLabel.setText(String.format("$%.2f (%d paid)", totalPaid, paidCount));
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

    public void showCalculateFineDialog(Borrowing preSelectedBorrowing) {
        JDialog dialog = new JDialog(dashboard, "Calculate & Assess Member Fine", true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(dashboard);
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

        UIHelper.addFormField(form, gbc, 0, "Select Loan:", borrowingCombo);
        UIHelper.addFormField(form, gbc, 1, "Member:", memberNameVal);
        UIHelper.addFormField(form, gbc, 2, "Book:", bookTitleVal);
        UIHelper.addFormField(form, gbc, 3, "Due Date:", dueDateVal);
        UIHelper.addFormField(form, gbc, 4, "Overdue Period:", overdueDaysVal);
        UIHelper.addFormField(form, gbc, 5, "Daily Rate ($/day):", rateSpinner);
        UIHelper.addFormField(form, gbc, 6, "Custom Amount:", customAmountCheck);
        UIHelper.addFormField(form, gbc, 7, "Amount Field:", customAmountSpinner);
        UIHelper.addFormField(form, gbc, 8, "Total Fine Due:", calculatedAmountVal);
        UIHelper.addFormField(form, gbc, 9, "Reason / Notes:", notesField);

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
                    refresh();
                    dashboard.refreshKpis();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(dashboard, "Fine #" + existingFine.getFineId() + " updated successfully to $" + String.format("%.2f", amount) + "!", "Fine Updated", JOptionPane.INFORMATION_MESSAGE);
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
            refresh();
            dashboard.refreshKpis();
            dialog.dispose();

            Member m = store.findMemberById(selectedBorrowing.getMemberId());
            String mName = m != null ? m.getName() : "Member #" + selectedBorrowing.getMemberId();
            JOptionPane.showMessageDialog(dashboard,
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

        refresh();
        dashboard.refreshKpis();

        if (newlyAssessed == 0 && updated == 0) {
            JOptionPane.showMessageDialog(dashboard,
                    "No new overdue loans requiring assessment found.\nAll overdue borrowings already have up-to-date fine records.",
                    "Auto-Assessment Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(dashboard,
                    "Batch Auto-Assessment Complete!\n\n" +
                    "• Newly assessed fines: " + newlyAssessed + " (Total: $" + String.format("%.2f", totalNewAmount) + ")\n" +
                    "• Updated overdue fines: " + updated + "\n" +
                    "Standard Rate Applied: $" + String.format("%.2f", defaultRate) + " / day",
                    "Fines Successfully Calculated",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void showMemberFinesDialog(Member initialMember) {
        JDialog dialog = new JDialog(dashboard, "Member Fine Account Overview", true);
        dialog.setSize(620, 500);
        dialog.setLocationRelativeTo(dashboard);
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

        summaryCard.add(UIHelper.createMiniSummaryCard("Total Assessed", totalAssessedVal, UITheme.PRIMARY));
        summaryCard.add(UIHelper.createMiniSummaryCard("Total Paid", totalPaidVal, UITheme.ACCENT));
        summaryCard.add(UIHelper.createMiniSummaryCard("Balance Due", totalOutstandingVal, UITheme.DANGER));

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
        UIHelper.centerAlignColumns(memberFinesTable, 0, 1, 2, 3, 4, 5, 6);

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
                refresh();
                dashboard.refreshKpis();
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

    private void onProcessFinePaymentClicked() {
        Fine fine = getSelectedFine();
        if (fine == null) {
            JOptionPane.showMessageDialog(dashboard, "Please select a fine from the table to process payment.", "No Fine Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (fine.isPaid()) {
            JOptionPane.showMessageDialog(dashboard,
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
        JDialog dialog = new JDialog(dashboard, "Process Fine Payment", true);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(dashboard);
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

        UIHelper.addFormField(form, gbc, 0, "Member:", memberVal);
        UIHelper.addFormField(form, gbc, 1, "Book / Borrowing:", bookVal);
        UIHelper.addFormField(form, gbc, 2, "Fine Assessed Date:", assessedDateVal);
        UIHelper.addFormField(form, gbc, 3, "Amount Payable:", amountVal);
        UIHelper.addFormField(form, gbc, 4, "Payment Method:", methodCombo);
        UIHelper.addFormField(form, gbc, 5, "Payment Notes / Ref:", paymentNotesField);

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
            refresh();
            dashboard.refreshKpis();
            dialog.dispose();

            JOptionPane.showMessageDialog(dashboard,
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
            JOptionPane.showMessageDialog(dashboard, "Please select a fine from the table to waive.", "No Fine Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(dashboard,
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
                refresh();
                dashboard.refreshKpis();
                JOptionPane.showMessageDialog(dashboard, "Fine #" + fine.getFineId() + " was successfully waived and removed.", "Fine Waived", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dashboard, "Failed to waive fine.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
