package ui;

import service.AdminStatsService;
import ui.Widgets.Banner;
import ui.Widgets.NavCard;
import ui.Widgets.StatCard;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardPanel extends Screen {

    private final AdminStatsService statsService = new AdminStatsService();
    private final StatCard vehicles = new StatCard("Vehicles", Theme.PRIMARY);
    private final StatCard availableCard = new StatCard("Available", Theme.SUCCESS);
    private final StatCard rented = new StatCard("Rented", Theme.WARNING);
    private final StatCard customers = new StatCard("Customers", Theme.PRIMARY);
    private final StatCard activeRentals = new StatCard("Active Rentals", Theme.WARNING);
    private final StatCard completedRentals = new StatCard("Completed Rentals", Theme.SUCCESS);
    private final Banner banner = new Banner();

    public AdminDashboardPanel(AppFrame app) {
        super(app);
        JPanel col = column();
        JLabel title = Theme.title("Admin Dashboard");
        JLabel sub = Theme.muted("Overview of your rental business");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(title);
        col.add(gap(4));
        col.add(sub);
        col.add(gap(18));
        col.add(banner);

        JPanel row1 = new JPanel(new GridLayout(1, 3, 16, 0));
        row1.setOpaque(false);
        row1.add(vehicles);
        row1.add(availableCard);
        row1.add(rented);
        JPanel row2 = new JPanel(new GridLayout(1, 3, 16, 0));
        row2.setOpaque(false);
        row2.add(customers);
        row2.add(activeRentals);
        row2.add(completedRentals);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(row1);
        col.add(gap(16));
        col.add(row2);
        col.add(gap(28));

        JLabel manage = Theme.heading("Manage");
        manage.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(manage);
        col.add(gap(12));

        JPanel nav = new JPanel(new GridLayout(1, 5, 14, 0));
        nav.setOpaque(false);
        nav.add(new NavCard("Manage Vehicles", "Add, edit, activate", Theme.PRIMARY, () -> app.navigate(AppFrame.ADMIN_VEHICLES)));
        nav.add(new NavCard("Customers", "Registered users", Theme.SUCCESS, () -> app.navigate(AppFrame.ADMIN_CUSTOMERS)));
        nav.add(new NavCard("Rentals", "All bookings", Theme.WARNING, () -> app.navigate(AppFrame.ADMIN_RENTALS)));
        nav.add(new NavCard("Payments", "Payment records", Theme.PRIMARY, () -> app.navigate(AppFrame.ADMIN_PAYMENTS)));
        nav.add(new NavCard("Maintenance", "Service records", Theme.ERROR, () -> app.navigate(AppFrame.ADMIN_MAINTENANCE)));
        nav.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(nav);

        setContent(col, 1150, Widgets.CenterColumnLayout.CENTER, true);
    }

    private StatCard[] all() { return new StatCard[]{vehicles, availableCard, rented, customers, activeRentals, completedRentals}; }

    /** Statistics load automatically every time the dashboard is shown, so no refresh button is needed. */
    @Override
    public void onShow() {
        banner.clear();
        Async.run(statsService::gather, s -> {
            vehicles.setValue(String.valueOf(s.totalVehicles));
            availableCard.setValue(String.valueOf(s.availableVehicles));
            rented.setValue(String.valueOf(s.rentedVehicles));
            customers.setValue(String.valueOf(s.totalCustomers));
            activeRentals.setValue(String.valueOf(s.activeRentals));
            completedRentals.setValue(String.valueOf(s.completedRentals));
        }, ex -> {
            for (StatCard c : all()) c.setValue("-");
            banner.error("Unable to load dashboard statistics. Please try again.");
        });
    }

    @Override
    public void reset() {
        for (StatCard c : all()) c.setValue("-");
        banner.clear();
    }
}
