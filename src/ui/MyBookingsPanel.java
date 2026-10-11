package ui;

import model.Rental;
import model.User;
import service.RentalService;
import service.ServiceResult;
import ui.Widgets.AppButton;
import ui.Widgets.Banner;
import ui.Widgets.DataTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MyBookingsPanel extends Screen {

    private final RentalService rentalService = new RentalService();
    private final DefaultTableModel model = Widgets.model("Booking ID", "Vehicle", "Pickup Date", "Return Date", "Days", "Amount", "Status");
    private final DataTable table = new DataTable(model);
    private final AppButton cancelBtn = new AppButton("Cancel Booking", AppButton.Variant.DANGER);
    private final AppButton returnBtn = new AppButton("Return Vehicle");
    private final Banner banner = new Banner();
    private List<Rental> rentals = new ArrayList<>();
    private int highlightId = -1;

    public MyBookingsPanel(AppFrame app) {
        super(app);
        table.setEmptyText("You have no bookings yet. Browse vehicles to get started.");
        Theme.widths(table, 100, 220, 120, 120, 60, 110, 120);
        Theme.alignRight(table, 5);
        Theme.statusColumn(table, 6);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);

        JPanel top = column();
        AppButton back = new AppButton("Back to Dashboard", AppButton.Variant.SECONDARY);
        back.addActionListener(e -> app.navigate(AppFrame.CUSTOMER_HOME));
        JPanel header = Widgets.pageHeader("My Bookings", "Return or cancel your active rentals", back);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(header);
        top.add(banner);
        top.add(gap(8));
        root.add(top, BorderLayout.NORTH);
        root.add(Theme.scroll(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        AppButton browse = new AppButton("Browse Vehicles", AppButton.Variant.SECONDARY);
        actions.add(cancelBtn);
        actions.add(returnBtn);
        actions.add(browse);
        root.add(actions, BorderLayout.SOUTH);

        setContent(root, 1200, Widgets.CenterColumnLayout.FILL, false);

        cancelBtn.setEnabled(false);
        returnBtn.setEnabled(false);
        cancelBtn.addActionListener(e -> cancelSelected());
        returnBtn.addActionListener(e -> returnSelected());
        browse.addActionListener(e -> app.navigate(AppFrame.BROWSE));
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updateButtons(); });
    }

    public void highlightOnNextLoad(int rentalId) { this.highlightId = rentalId; }

    @Override
    public void onShow() {
        banner.clear();
        load();
    }

    @Override
    public void reset() {
        model.setRowCount(0);
        rentals = new ArrayList<>();
        banner.clear();
        highlightId = -1;
    }

    private void load() {
        User u = app.getCurrentUser();
        if (u == null) return;
        final int userId = u.getUserId();
        Async.run(() -> rentalService.getRentalsByUser(userId), list -> {
            rentals = list;
            model.setRowCount(0);
            int toSelect = -1;
            for (int i = 0; i < list.size(); i++) {
                Rental r = list.get(i);
                model.addRow(new Object[]{r.getBookingId(), r.getVehicleName(), Theme.date(r.getPickupDate()),
                        Theme.date(r.getReturnDate()), r.getTotalDays(), Theme.money(r.getTotalAmount()), r.getRentalStatus()});
                if (r.getRentalId() == highlightId) toSelect = i;
            }
            highlightId = -1;
            if (toSelect >= 0) table.setRowSelectionInterval(toSelect, toSelect);
            updateButtons();
        }, ex -> banner.error("Unable to load your bookings. Please try again."));
    }

    private Rental selected() {
        int row = table.getSelectedRow();
        return (row < 0 || row >= rentals.size()) ? null : rentals.get(table.convertRowIndexToModel(row));
    }

    private void updateButtons() {
        Rental r = selected();
        boolean active = r != null && "CONFIRMED".equalsIgnoreCase(r.getRentalStatus());
        cancelBtn.setEnabled(active);
        returnBtn.setEnabled(active);
    }

    private void cancelSelected() {
        Rental r = selected();
        if (r == null) return;
        if (!Widgets.confirm(this, "Cancel booking", "Cancel booking " + r.getBookingId() + " for " + r.getVehicleName() + "?",
                "Keep Booking", "Cancel Booking")) return;
        run(() -> rentalService.cancelBooking(r.getRentalId()));
    }

    private void returnSelected() {
        Rental r = selected();
        if (r == null) return;
        if (!Widgets.confirm(this, "Return vehicle", "Return " + r.getVehicleName() + " (" + r.getBookingId() + ")?",
                "Not Yet", "Return Vehicle")) return;
        run(() -> rentalService.returnVehicle(r.getRentalId()));
    }

    private void run(java.util.concurrent.Callable<ServiceResult<Void>> action) {
        cancelBtn.setEnabled(false);
        returnBtn.setEnabled(false);
        Async.run(action, result -> {
            if (result.isSuccess()) banner.success(result.getMessage()); else banner.error(result.getMessage());
            load(); // table refreshes automatically
        }, ex -> {
            banner.error("Unable to update the booking. Please try again.");
            load();
        });
    }
}
