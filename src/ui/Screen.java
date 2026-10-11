package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Base class for every screen. A screen is a JPanel that lives inside the one main window (AppFrame).
 */
public abstract class Screen extends JPanel {
    protected final AppFrame app;

    protected Screen(AppFrame app) {
        super(new BorderLayout());
        this.app = app;
        setBackground(Theme.BG);
    }

    /** Called every time the screen is displayed: (re)load data here so screens are always up to date. */
    public void onShow() { }

    /** Called on logout: forget anything belonging to the previous user. */
    public void reset() { }

    /** The button triggered by the Enter key on this screen (or null). */
    public JButton defaultButton() { return null; }

    /**
     * Places the screen content in a centred column of at most maxWidth pixels.
     * @param mode   CenterColumnLayout.FILL / CENTER / TOP
     * @param scroll wrap in a scroll pane (for forms, so small laptop screens still work)
     */
    protected void setContent(JComponent content, int maxWidth, int mode, boolean scroll) {
        JPanel wrapper = new JPanel(new Widgets.CenterColumnLayout(maxWidth, mode));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(24, 32, 24, 32));
        wrapper.add(content);
        if (scroll) {
            JScrollPane sp = new JScrollPane(wrapper);
            sp.setBorder(null);
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.getVerticalScrollBar().setUnitIncrement(18);
            add(sp, BorderLayout.CENTER);
        } else {
            add(wrapper, BorderLayout.CENTER);
        }
    }

    protected static JPanel column() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    /** Centres one component inside a BoxLayout column without breaking the column's alignment. */
    protected static JComponent centered(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(c);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return p;
    }

    protected static JComponent gap(int h) { return (JComponent) Box.createRigidArea(new Dimension(0, h)); }
}
