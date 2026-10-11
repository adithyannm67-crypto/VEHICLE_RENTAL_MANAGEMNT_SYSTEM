package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/** Small reusable UI building blocks shared by every screen. */
public final class Widgets {
    private Widgets() { }

    private static void smooth(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // ===================================================================
    // Buttons
    // ===================================================================
    public static class AppButton extends JButton {
        public enum Variant { PRIMARY, SECONDARY, DANGER, LIGHT, LINK, HEADER }

        private final Variant variant;
        private boolean active;

        public AppButton(String text, Variant variant) {
            super(text);
            this.variant = variant;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(variant == Variant.LIGHT ? Theme.bold(16) : Theme.bold(variant == Variant.HEADER ? 13 : 13));
        }

        public AppButton(String text) { this(text, Variant.PRIMARY); }

        public void setActive(boolean a) { this.active = a; repaint(); }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int tw = fm.stringWidth(getText());
            switch (variant) {
                case LIGHT: return new Dimension(tw + 80, 54);
                case HEADER: return new Dimension(tw + 26, 36);
                case LINK: return new Dimension(tw + 8, 28);
                default: return new Dimension(tw + 40, 40);
            }
        }

        @Override
        public Dimension getMinimumSize() { return getPreferredSize(); }

        @Override
        public Dimension getMaximumSize() { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            boolean en = isEnabled();
            boolean hover = getModel().isRollover() && en;
            boolean press = getModel().isPressed() && en;
            int w = getWidth(), h = getHeight();
            Color bg = null, fg = Color.WHITE, border = null;
            int arc = 10;

            switch (variant) {
                case PRIMARY:
                    bg = !en ? new Color(0xB8C4D6) : press ? Theme.NAVY : hover ? Theme.PRIMARY_DARK : Theme.PRIMARY;
                    break;
                case SECONDARY:
                    bg = hover ? new Color(0xF1F5F9) : Color.WHITE;
                    border = hover ? Theme.PRIMARY : new Color(0xC9D2DF);
                    fg = en ? Theme.TEXT : new Color(0xA3ABB8);
                    break;
                case DANGER:
                    bg = !en ? new Color(0xF1B5B5) : press ? new Color(0x991B1B) : hover ? new Color(0xB91C1C) : Theme.ERROR;
                    break;
                case LIGHT:
                    bg = hover ? new Color(0xE8EEF7) : Color.WHITE;
                    fg = Theme.NAVY;
                    arc = 14;
                    break;
                case LINK:
                    fg = !en ? Theme.MUTED : hover ? Theme.NAVY : Theme.PRIMARY;
                    break;
                case HEADER:
                    bg = hover ? new Color(255, 255, 255, 34) : (active ? new Color(255, 255, 255, 22) : null);
                    fg = active ? Color.WHITE : new Color(0xD3DEEC);
                    arc = 8;
                    break;
                default:
                    break;
            }
            if (bg != null) {
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(fg);
            g2.drawString(getText(), tx, ty);
            if (variant == Variant.LINK && hover) {
                g2.drawLine(tx, ty + 2, tx + fm.stringWidth(getText()), ty + 2);
            }
            if (variant == Variant.HEADER && active) {
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(10, h - 4, w - 20, 3, 3, 3);
            }
            g2.dispose();
        }
    }

    // ===================================================================
    // Cards
    // ===================================================================
    public static class Card extends JPanel {
        protected boolean hover;

        public Card(LayoutManager lm, int pad) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(pad, pad, pad, pad));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(Theme.CARD);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(hover ? Theme.PRIMARY : Theme.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** A clickable navigation card: title + one-line description. */
    public static class NavCard extends Card {
        private final Color accent;

        public NavCard(String title, String subtitle, Color accent, Runnable action) {
            super(new BorderLayout(0, 4), 20);
            this.accent = accent;
            setBorder(new EmptyBorder(26, 22, 20, 22));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JLabel t = Theme.label(title, Theme.bold(17), Theme.TEXT);
            JLabel s = Theme.label(subtitle, Theme.font(13), Theme.MUTED);
            add(t, BorderLayout.NORTH);
            add(s, BorderLayout.CENTER);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { action.run(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(accent);
            g2.fillRoundRect(22, 14, 34, 4, 4, 4);
            g2.dispose();
        }
    }

    /** A statistic tile: small caption + big number. */
    public static class StatCard extends Card {
        private final JLabel value = Theme.label("-", Theme.bold(30), Theme.TEXT);
        private final Color accent;

        public StatCard(String caption, Color accent) {
            super(new BorderLayout(0, 2), 16);
            this.accent = accent;
            setBorder(new EmptyBorder(14, 22, 14, 14));
            add(Theme.label(caption, Theme.font(13), Theme.MUTED), BorderLayout.NORTH);
            add(value, BorderLayout.CENTER);
        }

        public void setValue(String v) { value.setText(v); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(accent);
            g2.fillRoundRect(0, 12, 5, getHeight() - 26, 5, 5);
            g2.dispose();
        }
    }

    // ===================================================================
    // Banner (inline message instead of a dialog)
    // ===================================================================
    public static class Banner extends JPanel {
        public enum Type { INFO, SUCCESS, ERROR, WARNING }

        private final JTextArea text = new JTextArea();
        private Color bg = Color.WHITE, bar = Theme.PRIMARY;
        private int lastWidth = -1;

        public Banner() {
            super(new BorderLayout());
            setOpaque(false);
            text.setEditable(false);
            text.setFocusable(false);
            text.setOpaque(false);
            text.setLineWrap(true);
            text.setWrapStyleWord(true);
            text.setFont(Theme.font(13));
            text.setBorder(new EmptyBorder(10, 16, 10, 14));
            add(text, BorderLayout.CENTER);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setVisible(false);
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override public void componentResized(java.awt.event.ComponentEvent e) {
                    if (getWidth() != lastWidth) { lastWidth = getWidth(); revalidate(); } // re-wrap for the new width
                }
            });
        }

        public void show(Type type, String message) {
            Color fg;
            switch (type) {
                case SUCCESS: bar = Theme.SUCCESS; bg = new Color(0xEAF7EE); fg = new Color(0x166534); break;
                case ERROR: bar = Theme.ERROR; bg = new Color(0xFDECEC); fg = new Color(0x991B1B); break;
                case WARNING: bar = Theme.WARNING; bg = new Color(0xFFF2E8); fg = new Color(0x9A3412); break;
                default: bar = Theme.PRIMARY; bg = new Color(0xEAF1FB); fg = Theme.PRIMARY_DARK; break;
            }
            text.setForeground(fg);
            text.setText(message);
            setVisible(true);
            revalidate();
            repaint();
        }

        public void success(String m) { show(Type.SUCCESS, m); }
        public void error(String m) { show(Type.ERROR, m); }
        public void info(String m) { show(Type.INFO, m); }
        public void clear() { text.setText(""); setVisible(false); revalidate(); }
        public String getMessage() { return text.getText(); }

        @Override
        public Dimension getPreferredSize() {
            int w = getWidth() > 0 ? getWidth() : 360;
            text.setSize(w, Short.MAX_VALUE);
            return new Dimension(w, text.getPreferredSize().height);
        }

        @Override
        public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(bar);
            g2.fillRoundRect(0, 0, 5, getHeight(), 5, 5);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===================================================================
    // Avatar / check badge (drawn, so no emoji-font dependency)
    // ===================================================================
    public static class Avatar extends JComponent {
        private String initial = "?";
        private final int size;

        public Avatar(int size) {
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));
        }

        public void setName2(String name) {
            initial = (name == null || name.trim().isEmpty()) ? "?" : name.trim().substring(0, 1).toUpperCase();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, size, size, Theme.NAVY));
            g2.fillOval(0, 0, size - 1, size - 1);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.bold(size / 2));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initial, (size - fm.stringWidth(initial)) / 2, (size - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    public static class CheckBadge extends JComponent {
        private final int size;

        public CheckBadge(int size) {
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(Theme.SUCCESS);
            g2.fillOval(0, 0, size - 1, size - 1);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(size / 10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int) (size * 0.28), (int) (size * 0.52), (int) (size * 0.44), (int) (size * 0.68));
            g2.drawLine((int) (size * 0.44), (int) (size * 0.68), (int) (size * 0.74), (int) (size * 0.34));
            g2.dispose();
        }
    }

    // ===================================================================
    // Layout: one child, centred horizontally, limited to a maximum width
    // ===================================================================
    public static class CenterColumnLayout implements LayoutManager {
        public static final int FILL = 0, CENTER = 1, TOP = 2;
        private final int maxWidth, mode;

        public CenterColumnLayout(int maxWidth, int mode) { this.maxWidth = maxWidth; this.mode = mode; }

        private Component child(Container p) { return p.getComponentCount() > 0 ? p.getComponent(0) : null; }

        @Override public void addLayoutComponent(String name, Component comp) { }
        @Override public void removeLayoutComponent(Component comp) { }

        @Override
        public Dimension preferredLayoutSize(Container p) {
            Insets in = p.getInsets();
            Component c = child(p);
            Dimension d = c == null ? new Dimension() : c.getPreferredSize();
            return new Dimension(Math.min(d.width, maxWidth) + in.left + in.right, d.height + in.top + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container p) {
            Insets in = p.getInsets();
            Component c = child(p);
            int h = c == null ? 0 : c.getMinimumSize().height;
            return new Dimension(200 + in.left + in.right, h + in.top + in.bottom);
        }

        @Override
        public void layoutContainer(Container p) {
            Component c = child(p);
            if (c == null) return;
            Insets in = p.getInsets();
            int availW = p.getWidth() - in.left - in.right;
            int availH = p.getHeight() - in.top - in.bottom;
            int w = Math.min(availW, maxWidth);
            int x = in.left + (availW - w) / 2;
            if (mode == FILL) {
                c.setBounds(x, in.top, w, availH);
            } else {
                int ph = c.getPreferredSize().height;
                int y = in.top + (mode == CENTER ? Math.max(0, (availH - ph) / 2) : 0);
                c.setBounds(x, y, w, ph);
            }
        }
    }

    // ===================================================================
    // Table
    // ===================================================================
    public static class DataTable extends JTable {
        private String emptyText = "No records found.";

        public DataTable(TableModel m) {
            super(m);
            Theme.styleTable(this);
        }

        public void setEmptyText(String t) { this.emptyText = t; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() == 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                smooth(g2);
                g2.setFont(Theme.font(14));
                g2.setColor(Theme.MUTED);
                FontMetrics fm = g2.getFontMetrics();
                Rectangle r = getVisibleRect();
                g2.drawString(emptyText, r.x + (r.width - fm.stringWidth(emptyText)) / 2, r.y + Math.min(90, r.height / 2));
                g2.dispose();
            }
        }
    }

    /** A read-only table model with the given column names. */
    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    // ===================================================================
    // Inputs
    // ===================================================================
    public static class HintTextField extends JTextField {
        private final String hint;

        public HintTextField(String hint) {
            super(20);
            this.hint = hint;
            Theme.style(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                smooth(g2);
                g2.setColor(new Color(0x9CA3AF));
                g2.setFont(getFont());
                Insets in = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }

    public static void onChange(JTextComponent c, Runnable r) {
        c.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { r.run(); }
            @Override public void removeUpdate(DocumentEvent e) { r.run(); }
            @Override public void changedUpdate(DocumentEvent e) { r.run(); }
        });
    }

    /** Only lets the user type digits (and, if decimal is true, one dot with up to 2 decimals). */
    public static void numericOnly(JTextField f, boolean decimal) {
        String regex = decimal ? "\\d{0,9}(\\.\\d{0,2})?" : "\\d{0,9}";
        ((AbstractDocument) f.getDocument()).setDocumentFilter(new DocumentFilter() {
            private boolean ok(String s) { return s.matches(regex); }

            @Override
            public void insertString(FilterBypass fb, int off, String str, AttributeSet a) throws BadLocationException {
                String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = cur.substring(0, off) + str + cur.substring(off);
                if (ok(next)) super.insertString(fb, off, str, a);
            }

            @Override
            public void replace(FilterBypass fb, int off, int len, String str, AttributeSet a) throws BadLocationException {
                String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = cur.substring(0, off) + (str == null ? "" : str) + cur.substring(off + len);
                if (ok(next)) super.replace(fb, off, len, str, a);
            }
        });
    }

    public static JSpinner dateSpinner(LocalDate initial, LocalDate earliest) {
        Date init = Date.from(initial.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date start = Date.from(earliest.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner s = new JSpinner(new SpinnerDateModel(init, start, null, Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "dd MMM yyyy"));
        return Theme.style(s);
    }

    public static LocalDate dateOf(JSpinner s) {
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static void setDate(JSpinner s, LocalDate d) {
        s.setValue(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    // ===================================================================
    // Page header + dialogs
    // ===================================================================
    public static JPanel pageHeader(String title, String subtitle, JComponent... actions) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel t = Theme.title(title);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(t);
        if (subtitle != null) {
            JLabel s = Theme.muted(subtitle);
            s.setAlignmentX(Component.LEFT_ALIGNMENT);
            s.setBorder(new EmptyBorder(4, 0, 0, 0));
            left.add(s);
        }
        p.add(left, BorderLayout.WEST);
        if (actions.length > 0) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);
            for (JComponent a : actions) right.add(a);
            p.add(right, BorderLayout.EAST);
        }
        return p;
    }

    /** Confirmation dialog with explicit button labels, e.g. [Cancel] [Deactivate]. */
    public static boolean confirm(Component parent, String title, String message, String cancelText, String confirmText) {
        Object[] options = {cancelText, confirmText};
        int r = JOptionPane.showOptionDialog(parent, message, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return r == 1;
    }
}
