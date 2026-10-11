package ui;

import model.Vehicle;
import service.VehicleService;
import ui.Widgets.AppButton;
import ui.Widgets.Banner;
import ui.Widgets.Card;
import ui.Widgets.DataTable;
import ui.Widgets.HintTextField;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BrowseVehiclesPanel extends Screen {

    private final VehicleService vehicleService = new VehicleService();

    private final HintTextField searchField = new HintTextField("Search vehicles...");
    private final JComboBox<String> typeBox = Theme.style(new JComboBox<>(new String[]{"All Types", "Car", "Bike"}));
    private final JComboBox<String> statusBox = Theme.style(new JComboBox<>(new String[]{"All Status", "Available", "Rented", "Maintenance"}));
    private final DefaultTableModel model = Widgets.model("ID", "Name", "Type", "Registration No.", "Fuel", "Seats", "Price/Day", "Status");
    private final DataTable table = new DataTable(model);
    private final JLabel countLabel = Theme.muted(" ");
    private final JLabel hintLabel = Theme.muted("Select an available vehicle to rent it.");
    private final AppButton rentBtn = new AppButton("RENT VEHICLE");
    private final Banner banner = new Banner();

    private List<Vehicle> shown = new ArrayList<>();
    private boolean suppress;       // true while Reset changes several controls at once
    private int requestSeq;         // ignore out-of-date search results
    private final Timer debounce;

    public BrowseVehiclesPanel(AppFrame app) {
        super(app);
        table.setEmptyText("No vehicles match your search.");
        Theme.widths(table, 50, 220, 80, 160, 90, 70, 110, 120);
        Theme.alignRight(table, 6);
        Theme.statusColumn(table, 7);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setOpaque(false);

        // ---- top: header + filter bar ----
        JPanel top = column();
        AppButton back = new AppButton("Back to Dashboard", AppButton.Variant.SECONDARY);
        back.addActionListener(e -> app.navigate(AppFrame.CUSTOMER_HOME));
        JPanel header = Widgets.pageHeader("Browse Vehicles", "Search, filter and pick your ride", back);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(header);
        top.add(banner);

        Card filterCard = new Card(new GridBagLayout(), 14);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0; g.insets = new Insets(0, 0, 0, 10); g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 0;
        searchField.setPreferredSize(new Dimension(340, 40));
        filterCard.add(searchField, g);
        g.gridx = 1; filterCard.add(typeBox, g);
        g.gridx = 2; filterCard.add(statusBox, g);
        AppButton reset = new AppButton("Reset", AppButton.Variant.SECONDARY);
        g.gridx = 3; filterCard.add(reset, g);
        g.gridx = 4; g.weightx = 1; g.insets = new Insets(0, 0, 0, 0);
        filterCard.add(Box.createHorizontalGlue(), g);
        filterCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, filterCard.getPreferredSize().height));
        top.add(filterCard);
        top.add(gap(14));
        root.add(top, BorderLayout.NORTH);

        // ---- centre: table ----
        root.add(Theme.scroll(table), BorderLayout.CENTER);

        // ---- bottom: hint + main action ----
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftInfo.setOpaque(false);
        countLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
        leftInfo.add(countLabel);
        leftInfo.add(hintLabel);
        bottom.add(leftInfo, BorderLayout.WEST);
        bottom.add(rentBtn, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        setContent(root, 1200, Widgets.CenterColumnLayout.FILL, false);

        // ---- behaviour ----
        rentBtn.setEnabled(false);
        rentBtn.addActionListener(e -> rentSelected());
        reset.addActionListener(e -> resetFilters());
        debounce = new Timer(280, e -> load());
        debounce.setRepeats(false);
        Widgets.onChange(searchField, () -> { if (!suppress) debounce.restart(); });
        typeBox.addActionListener(e -> { if (!suppress) load(); });
        statusBox.addActionListener(e -> { if (!suppress) load(); });
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updateRentState(); });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && rentBtn.isEnabled()) rentSelected();
            }
        });
    }

    private String typeValue() { return typeBox.getSelectedIndex() == 0 ? "ALL" : ((String) typeBox.getSelectedItem()).toUpperCase(); }
    private String statusValue() { return statusBox.getSelectedIndex() == 0 ? "ALL" : ((String) statusBox.getSelectedItem()).toUpperCase(); }

    @Override
    public void onShow() {
        banner.clear();
        load();
    }

    @Override
    public void reset() {
        suppress = true;
        searchField.setText("");
        typeBox.setSelectedIndex(0);
        statusBox.setSelectedIndex(0);
        suppress = false;
        model.setRowCount(0);
        shown = new ArrayList<>();
        banner.clear();
    }

    private void resetFilters() {
        suppress = true;
        searchField.setText("");
        typeBox.setSelectedIndex(0);
        statusBox.setSelectedIndex(0);
        suppress = false;
        debounce.stop();
        load();
    }

    private void load() {
        final int seq = ++requestSeq;
        final String kw = searchField.getText();
        final String type = typeValue();
        final String status = statusValue();
        Async.run(() -> vehicleService.search(kw, type, status), list -> {
            if (seq != requestSeq) return; // a newer search is already running
            banner.clear();
            shown = new ArrayList<>();
            model.setRowCount(0);
            for (Vehicle v : list) {
                if ("INACTIVE".equalsIgnoreCase(v.getStatus())) continue; // deactivated vehicles are hidden from customers
                shown.add(v);
                model.addRow(new Object[]{v.getVehicleId(), v.getVehicleName(), v.getCategoryLabel(), v.getRegistrationNumber(),
                        v.getFuelType(), v.getSeats(), Theme.money(v.getPricePerDay()), v.getStatus()});
            }
            countLabel.setText(shown.size() + (shown.size() == 1 ? " vehicle" : " vehicles"));
            updateRentState();
        }, ex -> {
            if (seq != requestSeq) return;
            banner.error("Unable to load vehicles. Please try again.");
        });
    }

    private Vehicle selected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= shown.size()) return null;
        return shown.get(table.convertRowIndexToModel(row));
    }

    private void updateRentState() {
        Vehicle v = selected();
        if (v == null) {
            rentBtn.setEnabled(false);
            hintLabel.setText("Select an available vehicle to rent it.");
        } else if ("AVAILABLE".equalsIgnoreCase(v.getStatus())) {
            rentBtn.setEnabled(true);
            hintLabel.setText("Ready to rent: " + v.getVehicleName());
        } else {
            rentBtn.setEnabled(false);
            hintLabel.setText(v.getVehicleName() + " is " + v.getStatus().toLowerCase() + " and cannot be rented right now.");
        }
    }

    private void rentSelected() {
        Vehicle v = selected();
        if (v != null && "AVAILABLE".equalsIgnoreCase(v.getStatus())) app.startBooking(v);
    }
}
