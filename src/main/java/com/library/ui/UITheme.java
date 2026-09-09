package com.library.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared color palette, fonts, component styling helpers, and theme engine
 * providing seamless Dark Mode and Light Mode support across the application.
 */
public final class UITheme {

    // Dynamic Palette (defaults initialized via static block)
    public static Color PRIMARY;
    public static Color PRIMARY_LIGHT;
    public static Color PRIMARY_TEXT;
    public static Color ACCENT;
    public static Color WARNING;
    public static Color DANGER;
    public static Color BG;
    public static Color CARD_BG;
    public static Color SURFACE_ALT;
    public static Color INPUT_BG;
    public static Color TEXT_DARK;
    public static Color TEXT_MUTED;
    public static Color BORDER;
    public static Color SECONDARY_BTN_BG;
    public static Color SECONDARY_BTN_FG;

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private static boolean darkMode = false;
    private static final List<Runnable> themeChangeListeners = new ArrayList<>();

    static {
        // Initialize with default palette before applyGlobalDefaults
        setPalette(false);
    }

    private UITheme() {
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    /**
     * Detects if the host OS (particularly Linux desktop environments) is running
     * in Dark Mode.
     */
    public static boolean detectSystemDarkMode() {
        // 1. Check GTK_THEME environment variable
        String gtkEnv = System.getenv("GTK_THEME");
        if (gtkEnv != null && gtkEnv.toLowerCase().contains("dark")) {
            return true;
        }

        // 2. Check GNOME / Linux Mint / Cinnamon / MATE desktop settings
        try {
            Process p = new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && line.toLowerCase().contains("dark")) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            Process p = new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "color-scheme").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && line.toLowerCase().contains("dark")) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }

        // 3. Fallback: inspect LookAndFeel component luminance
        Color panelBg = UIManager.getColor("Panel.background");
        if (panelBg != null) {
            double lum = 0.299 * panelBg.getRed() + 0.587 * panelBg.getGreen() + 0.114 * panelBg.getBlue();
            if (lum < 128) {
                return true;
            }
        }
        Color labelFg = UIManager.getColor("Label.foreground");
        if (labelFg != null) {
            double lum = 0.299 * labelFg.getRed() + 0.587 * labelFg.getGreen() + 0.114 * labelFg.getBlue();
            if (lum > 180) {
                return true;
            }
        }

        return false;
    }

    /**
     * Applies system look and feel, auto-detects dark mode, and configures
     * UIManager keys.
     */
    public static void applyGlobalDefaults() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to cross-platform L&F
        }

        boolean sysDark = detectSystemDarkMode();
        setDarkMode(sysDark);
    }

    /**
     * Toggles between Dark Mode and Light Mode, and notifies all registered
     * listeners.
     */
    public static void toggleTheme() {
        setDarkMode(!darkMode);
    }

    /**
     * Sets the active theme mode, updates the color palette and UIManager
     * properties,
     * and triggers theme change listeners.
     */
    public static void setDarkMode(boolean dark) {
        darkMode = dark;
        setPalette(dark);
        updateUIManager();

        for (Runnable listener : new ArrayList<>(themeChangeListeners)) {
            try {
                listener.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void addThemeChangeListener(Runnable listener) {
        if (listener != null && !themeChangeListeners.contains(listener)) {
            themeChangeListeners.add(listener);
        }
    }

    public static void removeThemeChangeListener(Runnable listener) {
        themeChangeListeners.remove(listener);
    }

    private static void setPalette(boolean dark) {
        if (dark) {
            // Modern Dark Mode Palette
            BG = new Color(0x18, 0x1A, 0x20); // Deep canvas
            CARD_BG = new Color(0x23, 0x27, 0x30); // Card / Surface
            SURFACE_ALT = new Color(0x1C, 0x1E, 0x24); // Inset / Control background
            INPUT_BG = new Color(0x1C, 0x1E, 0x24); // Input background
            BORDER = new Color(0x37, 0x3D, 0x4B); // Card & Grid border
            TEXT_DARK = new Color(0xEB, 0xF0, 0xF7); // Primary text
            TEXT_MUTED = new Color(0x9E, 0xA8, 0xB6); // Secondary text
            PRIMARY = new Color(0x1D, 0x4E, 0x78); // Primary button blue
            PRIMARY_LIGHT = new Color(0x2E, 0x5F, 0x8A); // Selection / Accent blue
            PRIMARY_TEXT = new Color(0x60, 0xA5, 0xFA); // High-contrast blue for text/KPIs
            ACCENT = new Color(0x2E, 0xB8, 0x7A); // Vibrant emerald green
            WARNING = new Color(0xF5, 0x9E, 0x0B); // Amber
            DANGER = new Color(0xEF, 0x44, 0x44); // Crimson
            SECONDARY_BTN_BG = new Color(0x33, 0x39, 0x47); // Dark secondary button
            SECONDARY_BTN_FG = new Color(0xEB, 0xF0, 0xF7); // Secondary text
        } else {
            // Clean Light Mode Palette
            BG = new Color(0xF4, 0xF6, 0xF8);
            CARD_BG = Color.WHITE;
            SURFACE_ALT = new Color(0xFA, 0xFB, 0xFC);
            INPUT_BG = Color.WHITE;
            BORDER = new Color(0xDD, 0xE3, 0xEA);
            TEXT_DARK = new Color(0x22, 0x2B, 0x38);
            TEXT_MUTED = new Color(0x6B, 0x76, 0x84);
            PRIMARY = new Color(0x1E, 0x40, 0x5C);
            PRIMARY_LIGHT = new Color(0x2E, 0x5F, 0x8A);
            PRIMARY_TEXT = new Color(0x1E, 0x40, 0x5C);
            ACCENT = new Color(0x2E, 0xA0, 0x6E);
            WARNING = new Color(0xD9, 0x7B, 0x1F);
            DANGER = new Color(0xC0, 0x39, 0x2B);
            SECONDARY_BTN_BG = new Color(0xE7, 0xEB, 0xEF);
            SECONDARY_BTN_FG = new Color(0x22, 0x2B, 0x38);
        }
    }

    private static void updateUIManager() {
        UIManager.put("Panel.background", BG);
        UIManager.put("Panel.foreground", TEXT_DARK);
        UIManager.put("Label.foreground", TEXT_DARK);

        UIManager.put("TextField.background", INPUT_BG);
        UIManager.put("TextField.foreground", TEXT_DARK);
        UIManager.put("TextField.caretForeground", TEXT_DARK);
        UIManager.put("PasswordField.background", INPUT_BG);
        UIManager.put("PasswordField.foreground", TEXT_DARK);
        UIManager.put("PasswordField.caretForeground", TEXT_DARK);

        UIManager.put("ComboBox.background", INPUT_BG);
        UIManager.put("ComboBox.foreground", TEXT_DARK);

        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("Table.background", CARD_BG);
        UIManager.put("Table.foreground", TEXT_DARK);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("Table.selectionForeground", Color.WHITE);

        UIManager.put("TableHeader.font", FONT_BODY_BOLD);
        UIManager.put("TableHeader.background", PRIMARY);
        UIManager.put("TableHeader.foreground", Color.WHITE);

        UIManager.put("ScrollPane.background", CARD_BG);
        UIManager.put("Viewport.background", CARD_BG);

        UIManager.put("OptionPane.background", CARD_BG);
        UIManager.put("OptionPane.messageForeground", TEXT_DARK);
    }

    // =========================================================================
    // Component Styling Helpers
    // =========================================================================

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
        styleButton(b, SECONDARY_BTN_BG, SECONDARY_BTN_FG);
        return b;
    }

    public static void styleButton(JButton b, Color bg, Color fg) {
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

    public static void styleTextField(JTextField tf) {
        tf.setUI(new javax.swing.plaf.basic.BasicTextFieldUI());
        tf.setFont(FONT_BODY);
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_DARK);
        tf.setCaretColor(TEXT_DARK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(5, 8, 5, 8)));
    }

    public static void stylePasswordField(JPasswordField pf) {
        pf.setUI(new javax.swing.plaf.basic.BasicPasswordFieldUI());
        pf.setFont(FONT_BODY);
        pf.setBackground(INPUT_BG);
        pf.setForeground(TEXT_DARK);
        pf.setCaretColor(TEXT_DARK);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(5, 8, 5, 8)));
    }

    public static void styleComboBox(JComboBox<?> cb) {
        cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = super.createArrowButton();
                b.setBackground(INPUT_BG);
                b.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                return b;
            }
        });
        cb.setFont(FONT_BODY);
        cb.setBackground(INPUT_BG);
        cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected && index >= 0) {
                    l.setBackground(PRIMARY_LIGHT);
                    l.setForeground(Color.WHITE);
                } else {
                    l.setBackground(INPUT_BG);
                    l.setForeground(TEXT_DARK);
                }
                l.setBorder(new EmptyBorder(4, 8, 4, 8));
                return l;
            }
        });
    }

    public static void styleTable(JTable table) {
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_DARK);
        table.setGridColor(BORDER);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(28);
        table.setFont(FONT_BODY);
        table.setFillsViewportHeight(true);
        styleTableHeader(table.getTableHeader());

        // Keep viewport and enclosing JScrollPane matching CARD_BG
        table.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                Container p = table.getParent();
                if (p instanceof JViewport) {
                    p.setBackground(CARD_BG);
                    if (p.getParent() instanceof JScrollPane) {
                        JScrollPane sp = (JScrollPane) p.getParent();
                        sp.setBackground(CARD_BG);
                        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
                    }
                }
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });
    }

    public static void styleTableHeader(JTableHeader header) {
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                l.setBackground(PRIMARY);
                l.setForeground(Color.WHITE);
                l.setFont(FONT_BODY_BOLD);
                l.setOpaque(true);

                if (table != null && column < table.getColumnCount()) {
                    TableCellRenderer cellRenderer = table.getColumnModel().getColumn(column).getCellRenderer();
                    if (cellRenderer instanceof JLabel) {
                        l.setHorizontalAlignment(((JLabel) cellRenderer).getHorizontalAlignment());
                    } else {
                        l.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                } else {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                }

                String text = (value != null) ? value.toString() : "";
                if (table != null && table.getRowSorter() != null) {
                    List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();
                    int modelIndex = table.convertColumnIndexToModel(column);
                    if (!sortKeys.isEmpty() && sortKeys.get(0).getColumn() == modelIndex) {
                        SortOrder order = sortKeys.get(0).getSortOrder();
                        if (order == SortOrder.ASCENDING) {
                            text += "  ▲";
                        } else if (order == SortOrder.DESCENDING) {
                            text += "  ▼";
                        }
                    }
                }
                l.setText(text);

                int rightBorder = (table != null && column == table.getColumnCount() - 1) ? 0 : 1;
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, rightBorder, PRIMARY_LIGHT),
                        new EmptyBorder(7, 10, 7, 10)));

                return l;
            }
        });

        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);

        header.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                Container p = header.getParent();
                if (p instanceof JViewport && p.getParent() instanceof JScrollPane) {
                    JScrollPane sp = (JScrollPane) p.getParent();
                    JPanel corner = new JPanel();
                    corner.setBackground(PRIMARY);
                    sp.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);
                }
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });
    }

    /**
     * Styles a JTabbedPane with custom rendering that resolves GTK theme clashes,
     * delivering clear high-contrast tabs in both Dark and Light modes.
     */
    public static void styleTabbedPane(JTabbedPane tabs) {
        tabs.setFont(FONT_BODY_BOLD);
        tabs.setBackground(BG);
        tabs.setOpaque(true);

        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabInsets = new Insets(8, 16, 8, 16);
                selectedTabPadInsets = new Insets(2, 2, 2, 2);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    g2.setColor(CARD_BG);
                    g2.fillRect(x, y, w, h);
                    // Accent indicator bar along the bottom of the active tab
                    g2.setColor(ACCENT);
                    g2.fillRect(x, y + h - 3, w, 3);
                } else {
                    g2.setColor(darkMode ? new Color(0x1E, 0x21, 0x29) : new Color(0xE8, 0xEC, 0xF1));
                    g2.fillRect(x, y, w, h);
                }

                g2.setColor(BORDER);
                g2.drawRect(x, y, w - 1, h);
                g2.dispose();
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                    int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setFont(font);
                if (isSelected) {
                    g.setColor(darkMode ? Color.WHITE : PRIMARY);
                } else {
                    g.setColor(TEXT_MUTED);
                }
                g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                g.setColor(BORDER);
                g.drawRect(0, 0, tabPane.getWidth() - 1, tabPane.getHeight() - 1);
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                    int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // Suppressed for clean modern design
            }
        });
    }

    /**
     * Creates a theme toggle button that switches between Dark and Light mode.
     */
    public static JButton createThemeToggleButton(Runnable onToggle) {
        JButton btn = new JButton(darkMode ? "☀️ Light" : "🌙 Dark");
        btn.setFont(FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        btn.setBackground(SECONDARY_BTN_BG);
        btn.setForeground(SECONDARY_BTN_FG);
        btn.setOpaque(true);

        btn.addActionListener(e -> {
            toggleTheme();
            btn.setText(darkMode ? "☀️ Light" : "🌙 Dark");
            btn.setBackground(SECONDARY_BTN_BG);
            btn.setForeground(SECONDARY_BTN_FG);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(6, 12, 6, 12)));
            if (onToggle != null) {
                onToggle.run();
            }
        });

        return btn;
    }
}
