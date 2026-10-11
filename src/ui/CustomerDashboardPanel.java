package ui;

import model.Rental;
import model.User;
import service.RentalService;
import service.VehicleService;
import ui.Widgets.NavCard;
import ui.Widgets.StatCard;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CustomerDashboardPanel extends Screen {

    private final RentalService rentalService = new RentalService();
    private final VehicleService vehicleService = new VehicleService();

    private final JLabel welcome = Theme.title("Welcome back!");
    private final StatCard active = new StatCard("Active bookings", Theme.PRIMARY);
    private final StatCard completed = new StatCard("Trips completed", Theme.SUCCESS);
    private final StatCard available = new StatCard("Vehicles available now", Theme.WARNING);

    public CustomerDashboardPanel(AppFrame app) {
        super(app);
        JPanel col = column();

        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = Theme.muted("Find your next ride.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(welcome);
        col.add(gap(4));
        col.add(sub);
        col.add(gap(22));

        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.add(active);
        stats.add(completed);
        stats.add(available);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(stats);
        col.add(gap(26));

        JLabel quick = Theme.heading("What would you like to do?");
        quick.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(quick);
        col.add(gap(12));

        JPanel nav = new JPanel(new GridLayout(2, 2, 16, 16));
        nav.setOpaque(false);
        nav.add(new NavCard("Browse Vehicles", "Find & rent a car or bike", Theme.PRIMARY, () -> app.navigate(AppFrame.BROWSE)));
        nav.add(new NavCard("My Bookings", "View, return or cancel your rentals", Theme.SUCCESS, () -> app.navigate(AppFrame.MY_BOOKINGS)));
        nav.add(new NavCard("My Profile", "Account details", Theme.WARNING, () -> app.navigate(AppFrame.PROFILE)));
        nav.add(new NavCard("Logout", "Sign out of your account", Theme.ERROR, app::logout));
        nav.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(nav);

        setContent(col, 1000, Widgets.CenterColumnLayout.CENTER, true);
    }

    @Override
    public void onShow() {
        User u = app.getCurrentUser();
        if (u == null) return;
        welcome.setText("Welcome back, " + u.getName() + "!");
        int userId = u.getUserId();
        // statistics reload every time the dashboard opens, so they always reflect cancel/return/rent
        Async.run(() -> {
            List<Rental> mine = rentalService.getRentalsByUser(userId);
            int a = 0, c = 0;
            for (Rental r : mine) {
                if ("CONFIRMED".equalsIgnoreCase(r.getRentalStatus())) a++;
                if ("COMPLETED".equalsIgnoreCase(r.getRentalStatus())) c++;
            }
            int av = vehicleService.search("", "ALL", "AVAILABLE").size();
            return new int[]{a, c, av};
        }, r -> {
            active.setValue(String.valueOf(r[0]));
            completed.setValue(String.valueOf(r[1]));
            available.setValue(String.valueOf(r[2]));
        }, ex -> {
            active.setValue("-");
            completed.setValue("-");
            available.setValue("-");
        });
    }

    @Override
    public void reset() {
        welcome.setText("Welcome back!");
        active.setValue("-");
        completed.setValue("-");
        available.setValue("-");
    }
}
