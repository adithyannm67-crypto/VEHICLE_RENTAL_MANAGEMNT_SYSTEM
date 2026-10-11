package ui;

import model.Vehicle;
import service.ServiceResult;
import service.VehicleService;
import ui.Widgets.AppButton;
import ui.Widgets.Banner;
import ui.Widgets.Card;
import ui.Widgets.DataTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/** Admin vehicle management. Vehicles are deactivated (INACTIVE), never hard-deleted, so rental history stays. */
public class ManageVehiclesPanel extends Screen {

    private final VehicleService vehicleService = new VehicleService();
    private final DefaultTableModel model = Widgets.model("ID", "Name", "Type", "Registration No.", "Fuel", "Seats", "Price/Day", "Status");
    private final DataTable table = new DataTable(model);
    private final Banner banner = new Banner();
    private final JLabel countLabel = Theme.muted(" ");

    private final AppButton addBtn = new AppButton("Add Vehicle");
    private final AppButton editBtn = new AppButton("Edit Vehicle", AppButton.Variant.SECONDARY);
    private final AppButton deactivateBtn = new AppButton("Deactivate", AppButton.Variant.DANGER);
    private final AppButton activateBtn = new AppButton("Activate", AppButton.Variant.SECONDARY);

    // inline form (shown on the right when adding / editing)
    private final Card formCard = new Card(new BorderLayout(), 22);
    private final JLabel formTitle = Theme.heading("Add Vehicle");
    private final Banner formBanner = new Banner();
    private final JTextField nameField = Theme.textField();
    private final JComboBox<String> typeBox = Theme.style(new JComboBox<>(new String[]{"CAR", "BIKE"}));
    private final JTextField regField = Theme.textField();
    private final JTextField fuelField = Theme.textField();
    private final JTextField seatsField = Theme.textField();
    private final JTextField priceField = Theme.textField();
    private final AppButton saveBtn = new AppButton("SAVE VEHICLE");
    private final JPanel formWrap = new JPanel(new BorderLayout());

    private List<Vehicle> vehicles = new ArrayList<>();
    private int editingId = -1;  // -1 = adding

    public ManageVehiclesPanel(AppFrame app) {
        super(app);
        Theme.widths(table, 50, 220, 80, 160, 90, 70, 110, 120);
        Theme.alignRight(table, 6);
        Theme.statusColumn(table, 7);
        Widgets.numericOnly(seatsField, false);
        Widgets.numericOnly(priceField, true);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);

        // ---- top ----
        JPanel top = column();
        AppButton back = new AppButton("Back to Dashboard", AppButton.Variant.SECONDARY);
        back.addActionListener(e -> app.navigate(AppFrame.ADMIN_HOME));
        JPanel header = Widgets.pageHeader("Manage Vehicles", "Add, edit and activate or deactivate vehicles", back);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(header);
        top.add(banner);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deactivateBtn);
        toolbar.add(activateBtn);
        toolbar.add(countLabel);
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, -10, 12, 0));
        top.add(toolbar);
        root.add(top, BorderLayout.NORTH);

        // ---- centre: table ----
        root.add(Theme.scroll(table), BorderLayout.CENTER);

        // ---- east: form ----
        JPanel fc = column();
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        fc.add(formTitle);
        fc.add(gap(12));
        fc.add(formBanner);
        fc.add(gap(4));
        fc.add(Theme.formRow("Name", nameField));
        fc.add(gap(10));
        fc.add(Theme.formRow("Type", typeBox));
        fc.add(gap(10));
        fc.add(Theme.formRow("Registration No.", regField));
        fc.add(gap(10));
        fc.add(Theme.formRow("Fuel", fuelField));
        JPanel sp = new JPanel(new GridLayout(1, 2, 12, 0));
        sp.setOpaque(false);
        sp.add(Theme.formRow("Seats", seatsField));
        sp.add(Theme.formRow("Price / Day (\u20B9)", priceField));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, sp.getPreferredSize().height));
        fc.add(gap(10));
        fc.add(sp);
        fc.add(gap(18));
        JPanel fb = new JPanel(new GridLayout(1, 2, 10, 0));
        fb.setOpaque(false);
        AppButton cancelForm = new AppButton("Cancel", AppButton.Variant.SECONDARY);
        fb.add(cancelForm);
        fb.add(saveBtn);
        fb.setAlignmentX(Component.LEFT_ALIGNMENT);
        fb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        fc.add(fb);
        formCard.add(fc, BorderLayout.NORTH);
        formWrap.setOpaque(false);
        formWrap.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        formWrap.setPreferredSize(new Dimension(372, 10));
        formWrap.add(formCard, BorderLayout.NORTH);
        formWrap.setVisible(false);
        root.add(formWrap, BorderLayout.EAST);

        setContent(root, 1300, Widgets.CenterColumnLayout.FILL, false);

        // ---- behaviour ----
        addBtn.addActionListener(e -> openForm(null));
        editBtn.addActionListener(e -> { Vehicle v = selected(); if (v != null) openForm(v); });
        deactivateBtn.addActionListener(e -> deactivate());
        activateBtn.addActionListener(e -> activate());
        cancelForm.addActionListener(e -> closeForm());
        saveBtn.addActionListener(e -> save());
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updateButtons(); });
        updateButtons();
    }

    @Override
    public void onShow() {
        banner.clear();
        closeForm();
        load(-1);
    }

    @Override
    public void reset() {
        model.setRowCount(0);
        vehicles = new ArrayList<>();
        banner.clear();
        closeForm();
    }

    private void load(int reselectId) {
        Async.run(vehicleService::getAllVehicles, list -> {
            vehicles = list;
            model.setRowCount(0);
            int sel = -1;
            for (int i = 0; i < list.size(); i++) {
                Vehicle v = list.get(i);
                model.addRow(new Object[]{v.getVehicleId(), v.getVehicleName(), v.getCategoryLabel(), v.getRegistrationNumber(),
                        v.getFuelType(), v.getSeats(), Theme.money(v.getPricePerDay()), v.getStatus()});
                if (v.getVehicleId() == reselectId) sel = i;
            }
            countLabel.setText("   " + list.size() + (list.size() == 1 ? " vehicle" : " vehicles"));
            if (sel >= 0) table.setRowSelectionInterval(sel, sel);
            updateButtons();
        }, ex -> banner.error("Unable to load vehicles. Please try again."));
    }

    private Vehicle selected() {
        int row = table.getSelectedRow();
        return (row < 0 || row >= vehicles.size()) ? null : vehicles.get(table.convertRowIndexToModel(row));
    }

    private void updateButtons() {
        Vehicle v = selected();
        boolean has = v != null;
        boolean inactive = has && "INACTIVE".equalsIgnoreCase(v.getStatus());
        editBtn.setEnabled(has);
        deactivateBtn.setEnabled(has && !inactive);
        activateBtn.setEnabled(inactive);
    }

    // ---- inline form ----
    private void openForm(Vehicle v) {
        formBanner.clear();
        editingId = v == null ? -1 : v.getVehicleId();
        formTitle.setText(v == null ? "Add Vehicle" : "Edit Vehicle");
        nameField.setText(v == null ? "" : v.getVehicleName());
        typeBox.setSelectedItem(v == null ? "CAR" : v.getVehicleType().toUpperCase());
        regField.setText(v == null ? "" : v.getRegistrationNumber());
        fuelField.setText(v == null ? "" : v.getFuelType());
        seatsField.setText(v == null ? "" : String.valueOf(v.getSeats()));
        priceField.setText(v == null ? "" : String.valueOf(v.getPricePerDay()));
        formWrap.setVisible(true);
        revalidate();
        nameField.requestFocusInWindow();
    }

    private void closeForm() {
        formWrap.setVisible(false);
        formBanner.clear();
        editingId = -1;
        revalidate();
    }

    private void save() {
        int seats;
        double price;
        try {
            seats = Integer.parseInt(seatsField.getText().trim());
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException ex) {
            formBanner.error("Seats and price must be valid numbers.");
            return;
        }
        final String name = nameField.getText(), type = (String) typeBox.getSelectedItem();
        final String reg = regField.getText().trim(), fuel = fuelField.getText();
        final int id = editingId;
        final double p = price;
        final int s = seats;
        saveBtn.setEnabled(false);
        Async.run((Callable<ServiceResult<Vehicle>>) () -> {
            if (id < 0) return vehicleService.addVehicle(name, type, reg, fuel, s, p);
            Vehicle existing = vehicleService.getById(id);
            String status = existing == null ? "AVAILABLE" : existing.getStatus(); // status is changed via Activate/Deactivate
            return vehicleService.updateVehicle(id, name, type, reg, fuel, s, p, status);
        }, result -> {
            saveBtn.setEnabled(true);
            if (result.isSuccess()) {
                closeForm();
                banner.success(id < 0 ? "Vehicle added successfully." : "Vehicle updated successfully.");
                load(id);
            } else {
                formBanner.error(result.getMessage());
            }
        }, ex -> {
            saveBtn.setEnabled(true);
            formBanner.error("Unable to save vehicle.");
        });
    }

    // ---- activate / deactivate ----
    private void deactivate() {
        Vehicle v = selected();
        if (v == null) return;
        if (!Widgets.confirm(this, "Deactivate vehicle", "Deactivate this vehicle?\n" + v.getVehicleName() + " (" + v.getRegistrationNumber() + ")",
                "Cancel", "Deactivate")) return;
        final int id = v.getVehicleId();
        change(() -> vehicleService.deactivateVehicle(id), id);
    }

    private void activate() {
        Vehicle v = selected();
        if (v == null) return;
        final int id = v.getVehicleId();
        change(() -> vehicleService.activateVehicle(id), id);
    }

    private void change(Callable<ServiceResult<Void>> action, int id) {
        Async.run(action, result -> {
            if (result.isSuccess()) banner.success(result.getMessage()); else banner.error(result.getMessage());
            load(id);
        }, ex -> {
            banner.error("Unable to update the vehicle status.");
            load(id);
        });
    }
}
