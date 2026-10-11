package ui;

import ui.Widgets.AppButton;

import javax.swing.*;
import java.awt.*;

/** First screen: introduces the app, nothing else. */
public class WelcomePanel extends Screen {

    private final AppButton start = new AppButton("GET STARTED", AppButton.Variant.LIGHT);

    public WelcomePanel(AppFrame app) {
        super(app);
        setLayout(new GridBagLayout());

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel l1 = Theme.label("VEHICLE RENTAL", Theme.bold(64), Color.WHITE);
        JLabel l2 = Theme.label("SYSTEM", Theme.bold(64), Theme.ACCENT);
        JLabel tag = Theme.label("Rent. Ride. Return.", Theme.font(22), new Color(0xD3DEEC));
        for (JLabel l : new JLabel[]{l1, l2, tag}) l.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.addActionListener(e -> app.navigate(AppFrame.LOGIN));

        center.add(l1);
        center.add(l2);
        center.add(Box.createRigidArea(new Dimension(0, 18)));
        center.add(tag);
        center.add(Box.createRigidArea(new Dimension(0, 46)));
        center.add(start);

        JLabel footer = Theme.label("Vehicle Rental Management System", Theme.font(13), new Color(0x8FA3BF));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        add(center, gbc);
        gbc.gridy = 1; gbc.weighty = 0; gbc.insets = new Insets(0, 0, 22, 0);
        add(footer, gbc);
    }

    @Override public JButton defaultButton() { return start; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setPaint(new GradientPaint(0, 0, new Color(0x0A1B33), w, h, new Color(0x1E4E8C)));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(255, 255, 255, 12));
        g2.fillOval(-w / 8, -h / 4, w / 2, w / 2);
        g2.fillOval(w - w / 3, h - h / 3, w / 2, w / 2);
        g2.setColor(new Color(125, 180, 245, 18));
        g2.fillOval(w / 2, -h / 3, w / 3, w / 3);
        g2.dispose();
    }
}
