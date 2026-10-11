package ui;

import model.Maintenance;
import model.Vehicle;
import service.MaintenanceService;
import service.ServiceResult;
import service.VehicleService;
import ui.Widgets.AppButton;
import ui.Widgets.Banner;
import ui.Widgets.Card;
import ui.Widgets.DataTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaintenancePanel extends Screen {

    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final VehicleService vehicleService = new VehicleService();

    // form (every field uses the full card width, so the cost is always completely visible)
    private final JTextField vehicleIdField = Theme.textField();
    private final JLabel vehicleHint = Theme.label(" ", Theme.font(12), Theme.MUTED);
    private final JSpinner dateSpinner = Widgets.dateSpinner(LocalDate.now(), LocalDate.now().minusYears(5));
    private final JTextField descriptionField = Theme.textField();
    private final JTextField costField = Theme.textField();
    private final Banner formBanner = new Banner();
    private final AppButton addBtn = new AppButton("ADD MAINTENANCE RECORD");

    // table
    private final DefaultTableModel model = Widgets.model("ID", "Vehicle", "Description", "Date", "Cost", "Status");
    private final DataTable table = new DataTable(model);
    private final Banner banner = new Banner();
    private final AppButton completeBtn = new AppButton("MARK COMPLETED");

    private List<Maintenance> records = new ArrayList<>();
    private final Map<Integer, Vehicle> vehicleCache = new HashMap<>();

    public MaintenancePanel(AppFrame app) {
        super(app);
        Widgets.numericOnly(vehicleIdField, false);
        Widgets.numericOnly(costField, true);
        costField.setFont(Theme.font(16));
        table.setEmptyText("No maintenance records yet.");
        Theme.widths(table, 50, 170, 220, 110, 100, 110);
        Theme.alignRight(table, 4);
        Theme.statusColumn(table, 5);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);

        JPanel top = column();
        AppButton back = new AppButton("Back to Dashboard", AppButton.Variant.SECONDARY);
        back.addActionListener(e -> app.navigate(AppFrame.ADMIN_HOME));
        JPanel header = Widgets.pageHeader("Maintenance", "Record service work and release vehicles when done", back);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(header);
        root.add(top, BorderLayout.NORTH);

        // ---- left: form card ----
        Card formCard = new Card(new BorderLayout(), 24);
        JPanel fc = column();
        JLabel ft = Theme.heading("New maintenance record");
        ft.setAlignmentX(Component.LEFT_ALIGNMENT);
        fc.add(ft);
        fc.add(gap(12));
        fc.add(formBanner);
        fc.add(gap(4));
        fc.add(Theme.formRow("Vehicle ID", vehicleIdField));
        vehicleHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        vehicleHint.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 0));
        fc.add(vehicleHint);
        fc.add(gap(10));
        fc.add(Theme.formRow("Date", dateSpinner));
        fc.add(gap(12));
        fc.add(Theme.formRow("Description", descriptionField));
        fc.add(gap(12));
        fc.add(Theme.formRow("Maintenance Cost (\u20B9)", costField));
        fc.add(gap(20));
        JPanel bw = new JPanel(new BorderLayout());
        bw.setOpaque(false);
        bw.add(addBtn);
        bw.setAlignmentX(Component.LEFT_ALIGNMENT);
        bw.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        fc.add(bw);
        formCard.add(fc, BorderLayout.NORTH);
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 18));
        left.setPreferredSize(new Dimension(420, 10));
        left.add(formCard, BorderLayout.NORTH);
        root.add(left, BorderLayout.WEST);

        // ---- right: table + action ----
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        JPanel rt = column();
        rt.add(banner);
        right.add(rt, BorderLayout.NORTH);
        right.add(Theme.scroll(table), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        actions.add(completeBtn);
        right.add(actions, BorderLayout.SOUTH);
        root.add(right, BorderLayout.CENTER);

        setContent(root, 1300, Widgets.CenterColumnLayout.FILL, false);

        completeBtn.setEnabled(false);
        addBtn.addActionListener(e -> addRecord());
        completeBtn.addActionListener(e -> markCompleted());
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updateButtons(); });
        Widgets.onChange(vehicleIdField, this::updateVehicleHint);
    }

    @Override
    public void onShow() {
        banner.clear();
        formBanner.clear();
        loadVehicles();
        loadRecords();
    }

    @Override
    public void reset() {
        model.setRowCount(0);
        records = new ArrayList<>();
        vehicleCache.clear();
        clearForm();
        banner.clear();
    }

    private void clearForm() {
        vehicleIdField.setText("");
        descriptionField.setText("");
        costField.setText("");
        Widgets.setDate(dateSpinner, LocalDate.now());
        formBanner.clear();
        vehicleHint.setText(" ");
    }

    private void loadVehicles() {
        Async.run(vehicleService::getAllVehicles, list -> {
            vehicleCache.clear();
            for (Vehicle v : list) vehicleCache.put(v.getVehicleId(), v);
            updateVehicleHint();
        }, null);
    }

    private void updateVehicleHint() {
        String t = vehicleIdField.getText().trim();
        if (t.isEmpty()) { vehicleHint.setText(" "); return; }
        try {
            Vehicle v = vehicleCache.get(Integer.parseInt(t));
            if (v == null) {
                vehicleHint.setForeground(Theme.ERROR);
                vehicleHint.setText("No vehicle with this ID");
            } else {
                vehicleHint.setForeground(Theme.SUCCESS);
                vehicleHint.setText(v.getVehicleName() + " (" + v.getRegistrationNumber() + ")  \u2022  " + v.getStatus());
            }
        } catch (NumberFormatException e) {
            vehicleHint.setText(" ");
        }
    }

    private void loadRecords() {
        Async.run(maintenanceService::getAll, list -> {
            records = list;
            model.setRowCount(0);
            for (Maintenance m : list) {
                model.addRow(new Object[]{m.getMaintenanceId(), m.getVehicleName(), Theme.orDash(m.getDescription()),
                        Theme.date(m.getMaintenanceDate()), Theme.money(m.getCost()), m.getStatus()});
            }
            updateButtons();
        }, ex -> banner.error("Unable to load maintenance records. Please try again."));
    }

    private Maintenance selected() {
        int row = table.getSelectedRow();
        return (row < 0 || row >= records.size()) ? null : records.get(table.convertRowIndexToModel(row));
    }

    private void updateButtons() {
        Maintenance m = selected();
        completeBtn.setEnabled(m != null && !"COMPLETED".equalsIgnoreCase(m.getStatus()));
    }

    private void addRecord() {
        formBanner.clear();
        int vehicleId;
        try {
            vehicleId = Integer.parseInt(vehicleIdField.getText().trim());
        } catch (NumberFormatException e) {
            formBanner.error("Please enter a valid vehicle ID.");
            return;
        }
        double cost;
        try {
            cost = Double.parseDouble(costField.getText().trim());
        } catch (NumberFormatException e) {
            formBanner.error("Please enter a valid maintenance cost.");
            return;
        }
        final int vid = vehicleId;
        final double c = cost;
        final String desc = descriptionField.getText();
        final LocalDate date = Widgets.dateOf(dateSpinner);
        addBtn.setEnabled(false);
        Async.run(() -> maintenanceService.addRecord(vid, desc, date, c), result -> {
            addBtn.setEnabled(true);
            if (result.isSuccess()) {
                clearForm();
                banner.success(result.getMessage());
                loadVehicles();
                loadRecords();
            } else {
                formBanner.error(result.getMessage());
            }
        }, ex -> {
            addBtn.setEnabled(true);
            formBanner.error("Unable to save the maintenance record.");
        });
    }

    private void markCompleted() {
        Maintenance m = selected();
        if (m == null) return;
        completeBtn.setEnabled(false);
        Async.run(() -> maintenanceService.markCompleted(m.getMaintenanceId(), m.getVehicleId()), result -> {
            if (result.isSuccess()) banner.success(result.getMessage()); else banner.error(result.getMessage());
            loadVehicles();
            loadRecords(); // table refreshes automatically
        }, ex -> {
            banner.error("Unable to update the maintenance record.");
            loadRecords();
        });
    }
}
