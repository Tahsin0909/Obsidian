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
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel displaying books currently and previously borrowed by the member.
 * Includes loan durations, overdue status flags, fine indicators, and quick navigation.
 */
public class MemberBorrowingsPanel extends JPanel {

    private final MemberDashboard dashboard;
    private final Member member;
    private final DataStore store = DataStore.getInstance();

    private DefaultTableModel myBorrowingsModel;
    private JTable table;

    public MemberBorrowingsPanel(MemberDashboard dashboard, Member member) {
        this.dashboard = dashboard;
        this.member = member;

        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        buildUI();
    }

    private void buildUI() {
        String[] cols = {"Borrow ID", "Book Title", "Author", "Borrow Date", "Due Date", "Days Remaining / Status", "Fine Status"};
        myBorrowingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(myBorrowingsModel);
        table.setFillsViewportHeight(true);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(28);
        UITheme.styleTableHeader(table.getTableHeader());

        UIHelper.centerAlignColumns(table, 0, 3, 4, 5, 6);
        UIHelper.setColumnWidths(table, 70, 180, 130, 90, 90, 140, 110);

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
            if (dashboard != null) {
                dashboard.switchTab(2); // Jump to Fines Tab
            }
        });

        bottomBar.add(viewFinesTabBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
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
}
