package ui;

import model.User;
import service.UserService;
import ui.Widgets.AppButton;
import ui.Widgets.Banner;
import ui.Widgets.DataTable;
import ui.Widgets.HintTextField;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class CustomersPanel extends Screen {

    private final UserService userService = new UserService();
    private final DefaultTableModel model = Widgets.model("Customer ID", "Name", "Email", "Phone", "Address");
    private final DataTable table = new DataTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final HintTextField search = new HintTextField("Search by name, email or phone...");
    private final JLabel countLabel = Theme.muted(" ");
    private final Banner banner = new Banner();

    public CustomersPanel(AppFrame app) {
        super(app);
        table.setRowSorter(sorter);
        table.setEmptyText("No customers found.");
        Theme.widths(table, 90, 180, 240, 120, 260);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        JPanel top = column();
        AppButton back = new AppButton("Back to Dashboard", AppButton.Variant.SECONDARY);
        back.addActionListener(e -> app.navigate(AppFrame.ADMIN_HOME));
        JPanel header = Widgets.pageHeader("Customers", "Registered customer accounts", back);
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
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q), 1, 2, 3));
        countLabel.setText(table.getRowCount() + " shown");
    }

    @Override
    public void onShow() {
        banner.clear();
        Async.run(userService::getAllCustomers, list -> {
            model.setRowCount(0);
            for (User u : list) { // passwords are never shown
                model.addRow(new Object[]{u.getUserId(), u.getName(), u.getEmail(), u.getPhone(), Theme.orDash(u.getAddress())});
            }
            applyFilter();
        }, ex -> banner.error("Unable to load customers. Please try again."));
    }

    @Override
    public void reset() { model.setRowCount(0); search.setText(""); banner.clear(); }
}
