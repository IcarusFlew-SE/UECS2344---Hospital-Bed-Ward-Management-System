// AppTheme - visual constants and styling for the UI
package hospital.app;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public final class AppTheme {

    private AppTheme() {}

    // ── Palette ──────────────────────────────────────────────────────────────
    // Deep navy – header bars, login panel
    public static final Color PRIMARY = new Color(0x1A2E4A);
    // Teal accent – action buttons, highlights
    public static final Color ACCENT = new Color(0x0E7C86);
    // Lighter teal for hover
    public static final Color ACCENT_LIGHT = new Color(0x17A0AD);
    // Light surface for panel backgrounds
    public static final Color SURFACE = new Color(0xF0F4F8);
    // White content card background
    public static final Color CARD = Color.WHITE;
    // Mid-grey borders and separators
    public static final Color BORDER_COLOR = new Color(0xC8D6E5);
    // Success green
    public static final Color SUCCESS = new Color(0x27AE60);
    // Warning amber
    public static final Color WARNING = new Color(0xF39C12);
    // Danger red
    public static final Color DANGER = new Color(0xC0392B);
    // Info blue
    public static final Color INFO = new Color(0x2980B9);

    // Bed-status row colours (light tints for table rows)
    public static final Color STATUS_AVAILABLE = new Color(0xD4EFDF); // green tint
    public static final Color STATUS_OCCUPIED = new Color(0xFADAD7); // red tint
    public static final Color STATUS_CLEANING = new Color(0xFEF9E7); // yellow tint
    public static final Color STATUS_RESERVED = new Color(0xD6EAF8); // blue tint

    // Text colours
    public static final Color TEXT_PRIMARY = new Color(0x1A2E4A);
    public static final Color TEXT_SECONDARY = new Color(0x5D7A8A);
    public static final Color TEXT_ON_DARK = Color.WHITE;

    // Fonts
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);

    // Borders
    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        );
    }

    public static Border sectionBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true), title);
        tb.setTitleFont(FONT_SECTION);
        tb.setTitleColor(PRIMARY);
        return BorderFactory.createCompoundBorder(tb,
            BorderFactory.createEmptyBorder(8, 10, 10, 10));
    }

    public static Border pagePadding() {
        return BorderFactory.createEmptyBorder(16, 18, 16, 18);
    }

    // Component builders
    // Creates a prominent primary action button (teal).
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(TEXT_ON_DARK);
        btn.setFont(FONT_BODY.deriveFont(Font.BOLD));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        return btn;
    }

    // Creates a secondary / outline-style button (navy).
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(TEXT_ON_DARK);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    // Creates a danger button (red).
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER);
        btn.setForeground(TEXT_ON_DARK);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    // Creates a section header label.
    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // Creates a regular body label.
    public static JLabel bodyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // Applies standard max-height sizing to a combo box.
    public static void sizeCombo(JComboBox<?> combo) {
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, combo.getPreferredSize().height + 4));
        combo.setFont(FONT_BODY);
    }

    // Applies standard max-height sizing to a text field.
    public static void sizeField(JComponent field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height + 4));
        field.setFont(FONT_BODY);
    }

    // Styles a JTable with theme defaults.
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(24);
        table.getTableHeader().setFont(FONT_BODY.deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(TEXT_ON_DARK);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(ACCENT_LIGHT);
        table.setSelectionForeground(TEXT_ON_DARK);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
    }

    // Styles a JScrollPane to match the card look.
    public static void styleScrollPane(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
    }


    public static void applyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    // Override Nimbus defaults with our palette
                    UIManager.put("nimbusBase", PRIMARY);
                    UIManager.put("nimbusBlueGrey", new Color(0x5D7A8A));
                    UIManager.put("control", SURFACE);
                    UIManager.put("text", TEXT_PRIMARY);
                    UIManager.put("nimbusFocus", ACCENT);
                    UIManager.put("nimbusSelectionBackground", ACCENT);
                    UIManager.put("Table.alternateRowColor", new Color(0xF7FAFC));
                    break;
                }
            }
        } catch (Exception e) {
            // Fall back to default L&F silently
        }
    }

    // Renderer that color-codes rows based on the bed status column
    public static DefaultTableCellRenderer bedStatusRenderer(int statusColIndex) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    Object statusVal = table.getModel().getValueAt(row, statusColIndex);
                    String status = statusVal == null ? "" : statusVal.toString();
                    switch (status) {
                        case "AVAILABLE" -> setBackground(STATUS_AVAILABLE);
                        case "OCCUPIED"  -> setBackground(STATUS_OCCUPIED);
                        case "CLEANING"  -> setBackground(STATUS_CLEANING);
                        case "RESERVED"  -> setBackground(STATUS_RESERVED);
                        default          -> setBackground(Color.WHITE);
                    }
                } else {
                    setBackground(ACCENT_LIGHT);
                    setForeground(TEXT_ON_DARK);
                }
                setFont(FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return this;
            }
        };
    }
}