package ui;

import service.RentalService;
import ui.Widgets.AppButton;
import ui.Widgets.CheckBadge;
import ui.Widgets.Card;

import javax.swing.*;
import java.awt.*;

public class BookingConfirmationPanel extends Screen {

    private final JLabel bookingId = Theme.muted("-");
    private final JPanel rows = column();
    private final AppButton viewBtn = new AppButton("VIEW MY BOOKING");
    private int rentalId = -1;

    public BookingConfirmationPanel(AppFrame app) {
        super(app);
        Card card = new Card(new BorderLayout(), 36);
        JPanel col = column();

        CheckBadge badge = new CheckBadge(72);
        JLabel title = Theme.label("BOOKING CONFIRMED", Theme.bold(24), Theme.SUCCESS);
        col.add(centered(badge));
        col.add(gap(14));
        col.add(centered(title));
        col.add(gap(4));
        col.add(centered(bookingId));
        col.add(gap(22));
        rows.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(rows);
        col.add(gap(24));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(viewBtn);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        col.add(wrap);
        col.add(gap(10));
        AppButton more = new AppButton("Browse more vehicles", AppButton.Variant.LINK);
        col.add(centered(more));

        card.add(col, BorderLayout.CENTER);
        viewBtn.addActionListener(e -> app.showMyBookings(rentalId));
        more.addActionListener(e -> app.navigate(AppFrame.BROWSE));
        setContent(card, 520, Widgets.CenterColumnLayout.CENTER, true);
    }

    public void setConfirmation(RentalService.BookingConfirmation c) {
        rentalId = c.rental.getRentalId();
        bookingId.setText("Booking ID  " + c.rental.getBookingId());
        rows.removeAll();
        addRow("Vehicle", c.vehicle.getVehicleName());
        addRow("Pickup", Theme.date(c.rental.getPickupDate()));
        addRow("Return", Theme.date(c.rental.getReturnDate()));
        addRow("Days", String.valueOf(c.rental.getTotalDays()));
        addRow("Total", Theme.money(c.rental.getTotalAmount()));
        rows.revalidate();
        rows.repaint();
    }

    private void addRow(String caption, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEDF0F5)));
        JLabel c = Theme.label(caption, Theme.font(15), Theme.MUTED);
        JLabel v = Theme.label(value, Theme.bold(15), Theme.TEXT);
        c.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        row.add(c, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        rows.add(row);
    }

    @Override public JButton defaultButton() { return viewBtn; }

    @Override public void reset() { rows.removeAll(); rentalId = -1; bookingId.setText("-"); }
}
