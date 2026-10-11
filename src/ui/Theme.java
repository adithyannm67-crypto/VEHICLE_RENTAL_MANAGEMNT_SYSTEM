package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * The single place that defines how the application looks:
 * colours, fonts, spacing, and styling of tables / fields / combo boxes.
 */
public final class Theme {
    private Theme() { }

    // ---- colours -------------------------------------------------------
    public static final Color NAVY = new Color(0x0F2744);
    public static final Color PRIMARY = new Color(0x1E4E8C);
    public static final Color PRIMARY_DARK = new Color(0x163B6B);
    public static final Color ACCENT = new Color(0x7DB4F5);
    public static final Color BG = new Color(0xF3F5F9);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(0xDDE3EC);
    public static final Color TEXT = new Color(0x1F2937);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color SUCCESS = new Color(0x16A34A);
    public static final Color WARNING = new Color(0xEA580C);
    public static final Color ERROR = new Color(0xDC2626);
    public static final Color ROW_ALT = new Color(0xF8FAFC);
    public static final Color SELECTION = new Color(0xDBEAFE);

    // ---- fonts ---------------------------------------------------------
    public static final String FAMILY = pickFamily();

    private static String pickFamily() {
        Set<String> have = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : new String[]{"Segoe UI", "Helvetica Neue", "Roboto", "Ubuntu", "Noto Sans", "DejaVu Sans"}) {
            if (have.contains(f)) return f;
        }
        return Font.SANS_SERIF;
    }

    public static Font font(int size) { return new Font(FAMILY, Font.PLAIN, size); }
    public static Font bold(int size) { return new Font(FAMILY, Font.BOLD, size); }

    /** Makes dialogs, tooltips and any un-styled component use the same font. */
    public static void installDefaults() {
        UIManager.put("defaultFont", font(14));
        for (String key : new String[]{"Label.font", "Button.font", "TextField.font", "PasswordField.font",
                "ComboBox.font", "Spinner.font", "OptionPane.messageFont", "OptionPane.buttonFont",
                "ToolTip.font", "Table.font"}) {
            UIManager.put(key, font(14));
        }
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
    }

    // ---- text helpers --------------------------------------------------
    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JLabel title(String text) { return label(text, bold(26), TEXT); }
    public static JLabel heading(String text) { return label(text, bold(18), TEXT); }
    public static JLabel muted(String text) { return label(text, font(13), MUTED); }

    public static String money(double v) {
        if (v == Math.floor(v)) return "\u20B9" + String.format(Locale.ENGLISH, "%,.0f", v);
        return "\u20B9" + String.format(Locale.ENGLISH, "%,.2f", v);
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    public static String date(LocalDate d) { return d == null ? "-" : DATE_FMT.format(d); }
    public static String dateTime(LocalDateTime d) { return d == null ? "-" : DATE_TIME_FMT.format(d); }

    public static String orDash(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s; }

    // ---- inputs --------------------------------------------------------
    private static Border fieldBorder(boolean focused) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(focused ? PRIMARY : BORDER, 1),
                new EmptyBorder(8, 12, 8, 12));
    }

    /** Applies the standard look to a text field / password field. */
    public static <T extends JTextComponent> T style(T f) {
        f.setFont(font(14));
        f.setForeground(TEXT);
        f.setBackground(Color.WHITE);
        f.setCaretColor(TEXT);
        f.setBorder(fieldBorder(false));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.setBorder(fieldBorder(true)); }
            @Override public void focusLost(FocusEvent e) { f.setBorder(fieldBorder(false)); }
        });
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, 40));
        return f;
    }

    public static JTextField textField() { return style(new JTextField(18)); }
    public static JPasswordField passwordField() { return style(new JPasswordField(18)); }

    public static <T> JComboBox<T> style(JComboBox<T> c) {
        c.setFont(font(14));
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(Math.max(c.getPreferredSize().width, 140), 40));
        return c;
    }

    public static JSpinner style(JSpinner s) {
        s.setFont(font(14));
        s.setPreferredSize(new Dimension(s.getPreferredSize().width, 40));
        return s;
    }

    /** A small caption above an input. The returned panel stretches across a BoxLayout column. */
    public static JPanel formRow(String caption, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(label(caption, bold(12), new Color(0x374151)), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    // ---- tables --------------------------------------------------------
    public static Color statusColor(String status) {
        if (status == null) return MUTED;
        switch (status.toUpperCase()) {
            case "AVAILABLE": case "COMPLETED": case "SUCCESS": return SUCCESS;
            case "RENTED": case "CONFIRMED": case "PENDING": return PRIMARY;
            case "MAINTENANCE": case "IN_PROGRESS": return WARNING;
            case "CANCELLED": case "FAILED": return ERROR;
            default: return MUTED; // INACTIVE etc.
        }
    }

    /** Plain text cell with padding, zebra striping and selection colour. */
    static class CellRenderer extends DefaultTableCellRenderer {
        CellRenderer(int align) { setHorizontalAlignment(align); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, row, col);
            setFont(font(13));
            setForeground(TEXT);
            setBackground(sel ? SELECTION : (row % 2 == 0 ? Color.WHITE : ROW_ALT));
            setBorder(new EmptyBorder(0, 12, 0, 12));
            return this;
        }
    }

    /** Status shown as a small coloured pill. */
    static class StatusRenderer extends CellRenderer {
        private Color pill = MUTED;
        private Color rowBg = Color.WHITE;

        StatusRenderer() { super(SwingConstants.LEFT); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            pill = statusColor(v == null ? null : v.toString());
            rowBg = getBackground();
            setFont(bold(11));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(rowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            String text = getText() == null ? "" : getText();
            FontMetrics fm = g2.getFontMetrics(getFont());
            int pw = fm.stringWidth(text) + 20, ph = 22;
            int x = 12, y = (getHeight() - ph) / 2;
            g2.setColor(new Color(pill.getRed(), pill.getGreen(), pill.getBlue(), 30));
            g2.fillRoundRect(x, y, pw, ph, ph, ph);
            g2.setColor(pill);
            g2.setFont(getFont());
            g2.drawString(text, x + 10, y + (ph - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Gives a JTable the standard application look. */
    public static void styleTable(JTable t) {
        t.setRowHeight(38);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(SELECTION);
        t.setSelectionForeground(TEXT);
        t.setFont(font(13));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setDefaultRenderer(Object.class, new CellRenderer(SwingConstants.LEFT));
        t.setDefaultRenderer(Number.class, new CellRenderer(SwingConstants.LEFT));

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, v, false, false, r, c);
                l.setFont(bold(12));
                l.setForeground(new Color(0x475569));
                l.setBackground(new Color(0xEEF2F7));
                l.setOpaque(true);
                l.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 12, 0, 12)));
                int align = SwingConstants.LEFT;
                TableCellRenderer cr = table.getColumnModel().getColumn(c).getCellRenderer();
                if (cr instanceof CellRenderer) align = ((CellRenderer) cr).getHorizontalAlignment();
                l.setHorizontalAlignment(align);
                l.setPreferredSize(new Dimension(l.getPreferredSize().width, 40));
                return l;
            }
        });
    }

    /** Preferred column widths (they scale proportionally when the window is resized). */
    public static void widths(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
    }

    public static void alignRight(JTable t, int... cols) {
        for (int c : cols) t.getColumnModel().getColumn(c).setCellRenderer(new CellRenderer(SwingConstants.RIGHT));
    }

    public static void statusColumn(JTable t, int col) {
        t.getColumnModel().getColumn(col).setCellRenderer(new StatusRenderer());
    }

    public static JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }
}
