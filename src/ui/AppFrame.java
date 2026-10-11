package ui;

import model.User;
import model.Vehicle;
import service.RentalService;
import ui.Widgets.AppButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * THE one and only window of the application.
 * Every screen is a panel inside a CardLayout; navigation = showing a different card.
 * Nothing else in the project creates a JFrame.
 */
public class AppFrame extends JFrame {

    // screen names
    public static final String WELCOME = "welcome";
    public static final String LOGIN = "login";
    public static final String REGISTER = "register";
    public static final String CUSTOMER_HOME = "customerHome";
    public static final String BROWSE = "browse";
    public static final String BOOKING = "booking";
    public static final String CONFIRMATION = "confirmation";
    public static final String MY_BOOKINGS = "myBookings";
    public static final String PROFILE = "profile";
    public static final String ADMIN_HOME = "adminHome";
    public static final String ADMIN_VEHICLES = "adminVehicles";
    public static final String ADMIN_CUSTOMERS = "adminCustomers";
    public static final String ADMIN_RENTALS = "adminRentals";
    public static final String ADMIN_PAYMENTS = "adminPayments";
    public static final String ADMIN_MAINTENANCE = "adminMaintenance";

    private static final java.util.Set<String> CUSTOMER_SCREENS = new java.util.HashSet<>(java.util.Arrays.asList(
            CUSTOMER_HOME, BROWSE, BOOKING, CONFIRMATION, MY_BOOKINGS, PROFILE));
    private static final java.util.Set<String> ADMIN_SCREENS = new java.util.HashSet<>(java.util.Arrays.asList(
            ADMIN_HOME, ADMIN_VEHICLES, ADMIN_CUSTOMERS, ADMIN_RENTALS, ADMIN_PAYMENTS, ADMIN_MAINTENANCE));

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final Map<String, Screen> screens = new LinkedHashMap<>();
    private final HeaderBar header = new HeaderBar();

    private User currentUser;
    private String currentScreen;

    public AppFrame() {
        setTitle("Vehicle Rental System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 640));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        if (!Toolkit.getDefaultToolkit().isFrameStateSupported(JFrame.MAXIMIZED_BOTH)) {
            // fallback: fill the usable screen area (excludes the taskbar)
            setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        }

        register(WELCOME, new WelcomePanel(this));
        register(LOGIN, new LoginPanel(this));
        register(REGISTER, new RegisterPanel(this));
        register(CUSTOMER_HOME, new CustomerDashboardPanel(this));
        register(BROWSE, new BrowseVehiclesPanel(this));
        register(BOOKING, new BookingPanel(this));
        register(CONFIRMATION, new BookingConfirmationPanel(this));
        register(MY_BOOKINGS, new MyBookingsPanel(this));
        register(PROFILE, new ProfilePanel(this));
        register(ADMIN_HOME, new AdminDashboardPanel(this));
        register(ADMIN_VEHICLES, new ManageVehiclesPanel(this));
        register(ADMIN_CUSTOMERS, new CustomersPanel(this));
        register(ADMIN_RENTALS, new RentalsPanel(this));
        register(ADMIN_PAYMENTS, new PaymentsPanel(this));
        register(ADMIN_MAINTENANCE, new MaintenancePanel(this));

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        navigate(WELCOME);
    }

    private void register(String name, Screen screen) {
        screens.put(name, screen);
        cardPanel.add(screen, name);
    }

    @SuppressWarnings("unchecked")
    private <T extends Screen> T screen(String name) { return (T) screens.get(name); }

    // ------------------------------------------------------------------
    // session
    // ------------------------------------------------------------------
    public User getCurrentUser() { return currentUser; }
    public String getCurrentScreen() { return currentScreen; }

    public void loginSuccess(User user) {
        this.currentUser = user;
        navigate(user.isAdmin() ? ADMIN_HOME : CUSTOMER_HOME);
    }

    /** Clears the session, wipes screen data and returns to the login screen. Nothing is left behind. */
    public void logout() {
        currentUser = null;
        for (Screen s : screens.values()) s.reset();
        navigate(LOGIN);
    }

    // ------------------------------------------------------------------
    // navigation
    // ------------------------------------------------------------------
    public void navigate(String name) {
        // access guard: customers cannot see admin screens and vice versa; nobody sees them logged out
        if (CUSTOMER_SCREENS.contains(name) && (currentUser == null || currentUser.isAdmin())) {
            name = currentUser == null ? LOGIN : ADMIN_HOME;
        } else if (ADMIN_SCREENS.contains(name) && (currentUser == null || !currentUser.isAdmin())) {
            name = currentUser == null ? LOGIN : CUSTOMER_HOME;
        }
        Screen s = screens.get(name);
        currentScreen = name;
        header.rebuild();
        s.onShow();
        cards.show(cardPanel, name);
        getRootPane().setDefaultButton(s.defaultButton());
    }

    public void startBooking(Vehicle vehicle) {
        ((BookingPanel) screens.get(BOOKING)).setVehicle(vehicle);
        navigate(BOOKING);
    }

    public void showConfirmation(RentalService.BookingConfirmation c) {
        ((BookingConfirmationPanel) screens.get(CONFIRMATION)).setConfirmation(c);
        navigate(CONFIRMATION);
    }

    public void showMyBookings(int rentalIdToHighlight) {
        ((MyBookingsPanel) screens.get(MY_BOOKINGS)).highlightOnNextLoad(rentalIdToHighlight);
        navigate(MY_BOOKINGS);
    }

    /** Used after the profile name changes so the header greeting stays correct. */
    public void refreshHeader() { header.rebuild(); }

    public Screen getScreen(String name) { return screens.get(name); }

    // ------------------------------------------------------------------
    // header bar (shown on every signed-in screen)
    // ------------------------------------------------------------------
    private class HeaderBar extends JPanel {
        private final JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 12));

        HeaderBar() {
            super(new BorderLayout());
            setBackground(Theme.NAVY);
            setBorder(new EmptyBorder(0, 28, 0, 20));
            setPreferredSize(new Dimension(10, 60));

            AppButton brand = new AppButton("VEHICLE RENTAL SYSTEM", AppButton.Variant.HEADER);
            brand.setFont(Theme.bold(16));
            brand.setToolTipText("Go to dashboard");
            brand.addActionListener(e -> {
                if (currentUser != null) navigate(currentUser.isAdmin() ? ADMIN_HOME : CUSTOMER_HOME);
            });
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
            left.setOpaque(false);
            left.add(brand);

            nav.setOpaque(false);
            add(left, BorderLayout.WEST);
            add(nav, BorderLayout.EAST);
        }

        void rebuild() {
            nav.removeAll();
            boolean signedIn = currentUser != null;
            setVisible(signedIn);
            if (signedIn) {
                if (currentUser.isAdmin()) {
                    link("Dashboard", ADMIN_HOME);
                    link("Vehicles", ADMIN_VEHICLES);
                    link("Customers", ADMIN_CUSTOMERS);
                    link("Rentals", ADMIN_RENTALS);
                    link("Payments", ADMIN_PAYMENTS);
                    link("Maintenance", ADMIN_MAINTENANCE);
                    JLabel who = Theme.label("  Admin  |", Theme.font(13), new Color(0xB9C7DA));
                    nav.add(who);
                } else {
                    link("Dashboard", CUSTOMER_HOME);
                    link("Browse Vehicles", BROWSE);
                    link("My Bookings", MY_BOOKINGS);
                    link("Profile", PROFILE);
                }
                AppButton logout = new AppButton("Logout", AppButton.Variant.HEADER);
                logout.addActionListener(e -> logout());
                nav.add(logout);
            }
            revalidate();
            repaint();
        }

        private void link(String text, String target) {
            AppButton b = new AppButton(text, AppButton.Variant.HEADER);
            b.setActive(isActive(target));
            b.addActionListener(e -> navigate(target));
            nav.add(b);
        }

        private boolean isActive(String target) {
            String cur = currentScreen;
            if (BOOKING.equals(cur)) cur = BROWSE;
            if (CONFIRMATION.equals(cur)) cur = MY_BOOKINGS;
            return target.equals(cur);
        }
    }
}
