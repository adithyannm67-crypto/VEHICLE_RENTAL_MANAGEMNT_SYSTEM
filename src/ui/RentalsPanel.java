package ui;

import model.Rental;
import service.RentalService;
import ui.Widgets.AppButton;
import ui.Widgets.Banner;
import ui.Widgets.DataTable;
import ui.Widgets.HintTextField;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class RentalsPanel extends Screen {

    private final RentalService rentalService = new RentalService();
    private final DefaultTableModel model = Widgets.model("Booking ID", "Customer", "Vehicle", "Pickup Date", "Return Date", "Amount", "Status");
    private final DataTable table = new DataTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final HintTextField search = new HintTextField("Search by booking, customer or vehicle...");
    private final JLabel countLabel = Theme.muted(" ");
    private final Banner banner = new Banner();

    public RentalsPanel(AppFrame app) {
        super(app);
        table.setRowSorter(sorter);
        table.setEmptyText("No rentals found.");
        Theme.widths(table, 100, 180, 200, 120, 120, 110, 120);
        Theme.alignRight(table, 5);
        Theme.statusColumn(table, 6);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        JPanel top = column();
        AppButton back = new AppButton("Back to Dashboard", AppButton.Variant.SECONDARY);
        back.addActionListener(e -> app.navigate(AppFrame.ADMIN_HOME));
        JPanel header = Widgets.pageHeader("Rentals", "All customer bookings", back);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(header);
        top.add(banner);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        bar.setOpaque(false);
        search.setPreferredSize(new Dimension(380, 40));
        bar.add(search);
        bar.add(countLabel);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setBorder(BorderFactory.createEmptyBorder(0, -14, 12, 0));
        top.add(bar);
        root.add(top, BorderLayout.NORTH);
        root.add(Theme.scroll(table), BorderLayout.CENTER);
        setContent(root, 1200, Widgets.CenterColumnLayout.FILL, false);

        Widgets.onChange(search, this::applyFilter);
    }

    private void applyFilter() {
        String q = search.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q), 0, 1, 2));
        countLabel.setText(table.getRowCount() + " shown");
    }

    @Override
    public void onShow() {
        banner.clear();
        Async.run(rentalService::getAllRentals, list -> {
            model.setRowCount(0);
            for (Rental r : list) {
                model.addRow(new Object[]{r.getBookingId(), r.getCustomerName(), r.getVehicleName(), Theme.date(r.getPickupDate()),
                        Theme.date(r.getReturnDate()), Theme.money(r.getTotalAmount()), r.getRentalStatus()});
            }
            applyFilter();
        }, ex -> banner.error("Unable to load rentals. Please try again."));
    }

    @Override
    public void reset() { model.setRowCount(0); search.setText(""); banner.clear(); }
}
