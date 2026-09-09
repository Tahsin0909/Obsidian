package com.library.ui.member;

import com.library.model.Book;
import com.library.store.DataStore;
import com.library.ui.UIHelper;
import com.library.ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 * Panel allowing members to browse and search the library catalog.
 * Features live regex filtering across all columns, stock status, and
 * categorization.
 */
public class MemberCatalogPanel extends JPanel {

    private final DataStore store = DataStore.getInstance();
    private DefaultTableModel catalogModel;
    private JTable table;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    public MemberCatalogPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.CARD_BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        buildUI();
    }

    private void buildUI() {
        // Search Bar Top
        JPanel filterRow = new JPanel(new BorderLayout(8, 0));
        filterRow.setOpaque(false);

        JLabel searchLbl = new JLabel("Search Books:");
        searchLbl.setFont(UITheme.FONT_BODY_BOLD);
        searchLbl.setForeground(UITheme.TEXT_DARK);
        searchField = new JTextField();
        UITheme.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(280, 32));

        filterRow.add(searchLbl, BorderLayout.WEST);
        filterRow.add(searchField, BorderLayout.CENTER);

        String[] cols = { "ISBN", "Title", "Author", "Category", "Publisher", "Year", "Available Copies" };
        catalogModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(catalogModel);
        UITheme.styleTable(table);

        sorter = new TableRowSorter<>(catalogModel);
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

        UIHelper.centerAlignColumns(table, 0, 5, 6);
        UIHelper.setColumnWidths(table, 100, 200, 150, 100, 100, 50, 100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER));

        add(filterRow, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        refreshTable();
    }

    public void refreshTable() {
        if (catalogModel == null)
            return;
        catalogModel.setRowCount(0);
        for (Book b : store.books()) {
            catalogModel.addRow(new Object[] {
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategoryName(),
                    b.getPublisher(),
                    b.getPublicationYear(),
                    b.getAvailableQuantity() > 0 ? (b.getAvailableQuantity() + " available") : "Out of Stock"
            });
        }
    }
}
