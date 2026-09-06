package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Shared color palette, fonts, and small factory helpers used across every UI panel
 * so the whole application looks consistent.
 */
public final class UITheme {

    public static final Color PRIMARY = new Color(0x1E, 0x40, 0x5C);      // deep blue
    public static final Color PRIMARY_LIGHT = new Color(0x2E, 0x5F, 0x8A);
    public static final Color ACCENT = new Color(0x2E, 0xA0, 0x6E);       // green (success/available)
    public static final Color WARNING = new Color(0xD9, 0x7B, 0x1F);      // amber (due soon)
    public static final Color DANGER = new Color(0xC0, 0x39, 0x2B);       // red (overdue / delete)
    public static final Color BG = new Color(0xF4, 0xF6, 0xF8);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(0x22, 0x2B, 0x38);
    public static final Color TEXT_MUTED = new Color(0x6B, 0x76, 0x84);
    public static final Color BORDER = new Color(0xDD, 0xE3, 0xEA);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {
    }

    public static void applyGlobalDefaults() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to default cross-platform L&F
        }
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.font", FONT_BODY_BOLD);
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, PRIMARY, Color.WHITE);
        return b;
    }

    public static JButton accentButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, ACCENT, Color.WHITE);
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DANGER, Color.WHITE);
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, new Color(0xE7, 0xEB, 0xEF), TEXT_DARK);
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(FONT_BODY_BOLD);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorderPainted(false);
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    public static void styleTableHeader(JTableHeader header) {
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(FONT_BODY_BOLD);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));
    }
}
