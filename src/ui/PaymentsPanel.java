package ui;

import model.Payment;
import service.PaymentService;
import ui.Widgets.AppButton;
import ui.Widgets.Banner;
import ui.Widgets.DataTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Read-only list of payment records created with bookings. (No online payment in this project.) */
public class PaymentsPanel extends Screen {

    private final PaymentService paymentService = new PaymentService();
    private final DefaultTableModel model = Widgets.model("Payment ID", "Booking ID", "Date", "Amount", "Method", "Status");
    private final DataTable table = new DataTable(model);
    private final Banner banner = new Banner();

    public PaymentsPanel(AppFrame app) {
        super(app);
        table.setEmptyText("No payment records yet.");
        Theme.widths(table, 90, 100, 180, 110, 100, 110);
        Theme.alignRight(table, 3);
        Theme.statusColumn(table, 5);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        JPanel top = column();
        AppButton back = new AppButton("Back to Dashboard", AppButton.Variant.SECONDARY);
        back.addActionListener(e -> app.navigate(AppFrame.ADMIN_HOME));
        JPanel header = Widgets.pageHeader("Payments", "Payment records for bookings", back);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(header);
        top.add(banner);
        top.add(gap(4));
        root.add(top, BorderLayout.NORTH);
        root.add(Theme.scroll(table), BorderLayout.CENTER);
        setContent(root, 1100, Widgets.CenterColumnLayout.FILL, false);
    }

    @Override
    public void onShow() {
        banner.clear();
        Async.run(paymentService::getAllPayments, list -> {
            model.setRowCount(0);
            for (Payment p : list) {
                model.addRow(new Object[]{p.getPaymentId(), "BK" + (10000 + p.getRentalId()), Theme.dateTime(p.getPaymentDate()),
                        Theme.money(p.getAmount()), p.getPaymentMethod(), p.getPaymentStatus()});
            }
        }, ex -> banner.error("Unable to load payments. Please try again."));
    }

    @Override
    public void reset() { model.setRowCount(0); banner.clear(); }
}
