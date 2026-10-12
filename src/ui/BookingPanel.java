package ui;

import model.User;
import model.Vehicle;
import service.CardPaymentProcessor;
import service.CashPaymentProcessor;
import service.PaymentMethodProcessor;
import service.RentalService;
import service.UpiPaymentProcessor;
import ui.Widgets.AppButton;
import ui.Widgets.Banner;
import ui.Widgets.Card;
import util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** "Rent vehicle" screen: vehicle summary on the left, dates + live price on the right. */
public class BookingPanel extends Screen {

    private final RentalService rentalService = new RentalService();
    private Vehicle vehicle;

    private final JLabel nameLabel = Theme.label("-", Theme.bold(22), Theme.TEXT);
    private final JLabel typeLabel = Theme.label("-", Theme.font(13), Theme.MUTED);
    private final JPanel details = column();

    private JSpinner pickup;
    private JSpinner returnDate;
    private final JComboBox<String> paymentMethod = Theme.style(new JComboBox<>(new String[]{"CASH", "CARD", "UPI"}));
    private final JLabel daysLabel = Theme.label("-", Theme.font(14), Theme.TEXT);
    private final JLabel rateLabel = Theme.label("-", Theme.font(14), Theme.TEXT);
    private final JLabel extrasLabel = Theme.label("-", Theme.font(14), Theme.TEXT);
    private final JLabel totalLabel = Theme.label("-", Theme.bold(26), Theme.PRIMARY);
    private final Banner banner = new Banner();
    private final AppButton confirmBtn = new AppButton("CONFIRM BOOKING");
    private boolean building;

    public BookingPanel(AppFrame app) {
        super(app);
        JPanel root = column();
        AppButton back = new AppButton("Back to Vehicles", AppButton.Variant.SECONDARY);
        back.addActionListener(e -> app.navigate(AppFrame.BROWSE));
        JPanel header = Widgets.pageHeader("Rent Vehicle", "Choose your rental dates", back);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(header);

        JPanel two = new JPanel(new GridLayout(1, 2, 20, 0));
        two.setOpaque(false);

        // left card: vehicle
        Card left = new Card(new BorderLayout(), 26);
        JPanel lc = column();
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        lc.add(nameLabel);
        lc.add(gap(2));
        lc.add(typeLabel);
        lc.add(gap(18));
        details.setAlignmentX(Component.LEFT_ALIGNMENT);
        lc.add(details);
        left.add(lc, BorderLayout.NORTH);

        // right card: dates
        Card right = new Card(new BorderLayout(), 26);
        JPanel rc = column();
        JLabel h = Theme.heading("Rental dates");
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        rc.add(h);
        rc.add(gap(14));
        rc.add(banner);
        rc.add(gap(6));
        LocalDate today = LocalDate.now();
        pickup = Widgets.dateSpinner(today, today);
        returnDate = Widgets.dateSpinner(today.plusDays(1), today);
        JPanel dates = new JPanel(new GridLayout(1, 2, 14, 0));
        dates.setOpaque(false);
        dates.add(Theme.formRow("Pickup date", pickup));
        dates.add(Theme.formRow("Return date", returnDate));
        dates.setAlignmentX(Component.LEFT_ALIGNMENT);
        dates.setMaximumSize(new Dimension(Integer.MAX_VALUE, dates.getPreferredSize().height));
        rc.add(dates);
        rc.add(gap(16));
        rc.add(Theme.formRow("Payment method (simulated)", paymentMethod));
        rc.add(gap(20));

        JPanel summary = new JPanel(new GridBagLayout());
        summary.setBackground(new Color(0xF5F8FC));
        summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        addSummaryRow(summary, 0, "Duration", daysLabel);
        addSummaryRow(summary, 1, "Rate", rateLabel);
        addSummaryRow(summary, 2, "Insurance & extras", extrasLabel);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 3; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(10, 0, 0, 0);
        summary.add(Theme.label("Total", Theme.bold(15), Theme.TEXT), g);
        g.gridx = 1; g.anchor = GridBagConstraints.EAST; g.weightx = 1;
        summary.add(totalLabel, g);
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, summary.getPreferredSize().height));
        rc.add(summary);
        rc.add(gap(20));

        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setOpaque(false);
        btnWrap.add(confirmBtn);
        btnWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        rc.add(btnWrap);
        right.add(rc, BorderLayout.NORTH);

        two.add(left);
        two.add(right);
        two.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(two);

        setContent(root, 1000, Widgets.CenterColumnLayout.TOP, true);

        pickup.addChangeListener(e -> onDatesChanged(true));
        returnDate.addChangeListener(e -> onDatesChanged(false));
        confirmBtn.addActionListener(e -> confirm());
    }

    private void addSummaryRow(JPanel p, int row, String caption, JLabel value) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = row; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(3, 0, 3, 0);
        p.add(Theme.label(caption, Theme.font(14), Theme.MUTED), g);
        g.gridx = 1; g.anchor = GridBagConstraints.EAST; g.weightx = 1;
        p.add(value, g);
    }

    private void addDetail(String caption, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEDF0F5)));
        JLabel c = Theme.label(caption, Theme.font(14), Theme.MUTED);
        JLabel v = Theme.label(value, Theme.bold(14), Theme.TEXT);
        c.setBorder(BorderFactory.createEmptyBorder(9, 0, 9, 0));
        row.add(c, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        details.add(row);
    }

    public void setVehicle(Vehicle v) {
        this.vehicle = v;
        building = true;
        LocalDate today = LocalDate.now();
        Widgets.setDate(pickup, today);
        Widgets.setDate(returnDate, today.plusDays(1));
        building = false;
        nameLabel.setText(v.getVehicleName());
        typeLabel.setText(v.getCategoryLabel() + "  \u2022  " + v.getRegistrationNumber());
        details.removeAll();
        addDetail("Type", v.getCategoryLabel());
        addDetail("Registration No.", v.getRegistrationNumber());
        addDetail("Fuel", v.getFuelType());
        addDetail("Seats", String.valueOf(v.getSeats()));
        addDetail("Price per day", Theme.money(v.getPricePerDay()));
        addDetail("Status", v.getStatus());
        details.revalidate();
        details.repaint();
        paymentMethod.setSelectedIndex(0);
        banner.clear();
        confirmBtn.setEnabled(true);
        confirmBtn.setText("CONFIRM BOOKING");
        recalc();
    }

    private void onDatesChanged(boolean pickupChanged) {
        if (building) return;
        // keep return date after pickup date automatically
        if (pickupChanged && !Widgets.dateOf(returnDate).isAfter(Widgets.dateOf(pickup))) {
            building = true;
            Widgets.setDate(returnDate, Widgets.dateOf(pickup).plusDays(1));
            building = false;
        }
        recalc();
    }

    /** Live price: no "Calculate" button needed. */
    private void recalc() {
        if (vehicle == null) return;
        LocalDate p = Widgets.dateOf(pickup), r = Widgets.dateOf(returnDate);
        if (!ValidationUtil.isValidDateRange(p, r)) {
            daysLabel.setText("-");
            rateLabel.setText(Theme.money(vehicle.getPricePerDay()) + " / day");
            extrasLabel.setText("-");
            totalLabel.setText("-");
            banner.error("Return date must be after the pickup date.");
            confirmBtn.setEnabled(false);
            return;
        }
        banner.clear();
        int days = (int) ChronoUnit.DAYS.between(p, r);
        double total = rentalService.previewTotal(vehicle, p, r);
        double extras = vehicle.calculateExtraCharges(days);
        daysLabel.setText(days + (days == 1 ? " day" : " days"));
        rateLabel.setText(Theme.money(vehicle.getPricePerDay()) + " / day");
        extrasLabel.setText(Theme.money(extras));
        totalLabel.setText(Theme.money(total));
        confirmBtn.setEnabled(true);
    }

    /** Maps the selected method to its (simulated) processor. */
    private PaymentMethodProcessor selectedProcessor() {
        switch (paymentMethod.getSelectedIndex()) {
            case 1: return new CardPaymentProcessor();
            case 2: return new UpiPaymentProcessor();
            default: return new CashPaymentProcessor();
        }
    }

    private void confirm() {
        User u = app.getCurrentUser();
        if (u == null || vehicle == null) return;
        LocalDate p = Widgets.dateOf(pickup), r = Widgets.dateOf(returnDate);
        confirmBtn.setEnabled(false);
        confirmBtn.setText("BOOKING...");
        final int vehicleId = vehicle.getVehicleId();
        // Payments are simulated: the chosen method is recorded on the payment row.
        final PaymentMethodProcessor processor = selectedProcessor();
        Async.run(() -> rentalService.bookVehicle(u.getUserId(), vehicleId, p, r, processor), result -> {
            confirmBtn.setText("CONFIRM BOOKING");
            if (result.isSuccess()) {
                app.showConfirmation(result.getData());
            } else {
                confirmBtn.setEnabled(true);
                banner.error(result.getMessage());
            }
        }, ex -> {
            confirmBtn.setEnabled(true);
            confirmBtn.setText("CONFIRM BOOKING");
            banner.error("Unable to complete the booking. Please try again.");
        });
    }

    @Override public JButton defaultButton() { return confirmBtn; }

    @Override public void reset() { vehicle = null; banner.clear(); }
}
