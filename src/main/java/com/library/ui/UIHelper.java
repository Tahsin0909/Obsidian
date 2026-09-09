package com.library.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Shared UI Layout and Component helper utilities used across dashboards and
 * admin panels.
 */
public final class UIHelper {

    private UIHelper() {
    }

    /**
     * Standardized form field adder for GridBagLayout forms with theme support.
     */
    public static void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.32;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.68;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        field.setFont(UITheme.FONT_BODY);
        if (field instanceof JTextField) {
            UITheme.styleTextField((JTextField) field);
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
            field.setMinimumSize(new Dimension(50, 30));
        } else if (field instanceof JComboBox) {
            UITheme.styleComboBox((JComboBox<?>) field);
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
            field.setMinimumSize(new Dimension(50, 30));
        } else if (field instanceof JSpinner) {
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
            field.setMinimumSize(new Dimension(50, 30));
        }
        panel.add(field, gbc);
    }

    /**
     * Center aligns specified column indices of a JTable.
     */
    public static void centerAlignColumns(JTable table, int... columnIndices) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int idx : columnIndices) {
            if (idx < table.getColumnCount()) {
                table.getColumnModel().getColumn(idx).setCellRenderer(centerRenderer);
            }
        }
    }

    /**
     * Sets preferred widths for specified columns of a JTable.
     */
    public static void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /**
     * Creates a compact metric summary card for summary strips.
     */
    public static JPanel createMiniSummaryCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(4, 2));
        card.setBackground(UITheme.SURFACE_ALT);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_HEADING);
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }
}
