package ui;

import model.User;
import service.UserService;
import ui.Widgets.AppButton;
import ui.Widgets.Banner;
import ui.Widgets.Card;
import ui.Widgets.DataTable;
import ui.Widgets.HintTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomersPanel extends Screen {

    private final UserService userService = new UserService();
    private final DefaultTableModel model = Widgets.model("ID", "Name", "Email", "Phone", "Address", "");
    private final DataTable table = new DataTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final HintTextField search = new HintTextField("Search by name, email or phone...");
    private final JLabel countLabel = Theme.muted(" ");
    private final Banner banner = new Banner();

    private final JTextField nameField = Theme.textField();
    private final JTextField emailField = Theme.textField();
    private final JTextField phoneField = Theme.textField();
    private final JTextField addressField = Theme.textField();
    private final AppButton saveBtn = new AppButton("Save changes", AppButton.Variant.PRIMARY);
    private final AppButton cancelBtn = new AppButton("Cancel", AppButton.Variant.SECONDARY);
    private final AppButton editBtn = new AppButton("Edit customer", AppButton.Variant.PRIMARY) {
        @Override public Dimension getPreferredSize() { return new Dimension(112, 32); }
        @Override public Dimension getMinimumSize() { return getPreferredSize(); }
        @Override public Dimension getMaximumSize() { return getPreferredSize(); }
    };
    private final JDialog editDialog;
    private int editingUserId = -1;

    public CustomersPanel(AppFrame app) {
        super(app);
        table.setRowSorter(sorter);
        table.setEmptyText("No customers found.");
        Theme.widths(table, 80, 180, 240, 120, 260, 118);
        table.getColumnModel().getColumn(5).setMinWidth(118);
        table.getColumnModel().getColumn(5).setMaxWidth(118);
        table.getColumnModel().getColumn(5).setPreferredWidth(118);
        table.getColumnModel().getColumn(5).setCellRenderer(new EditButtonRenderer());
        table.addMouseListener(new EditClickHandler());

        editDialog = buildEditDialog();

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

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(Theme.scroll(table), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        setContent(root, 1200, Widgets.CenterColumnLayout.FILL, false);

        Widgets.onChange(search, this::applyFilter);

        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void renderRow(User u) {
        model.addRow(new Object[]{u.getUserId(), u.getName(), u.getEmail(), u.getPhone(), Theme.orDash(u.getAddress()), ""});
    }

    private class EditButtonRenderer extends JPanel implements TableCellRenderer {
        EditButtonRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 0, 0));
            setOpaque(true);
            add(editBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setBackground(isSelected ? new Color(0xEAF1FB) : Theme.CARD);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class EditClickHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int viewRow = table.rowAtPoint(e.getPoint());
            if (viewRow < 0) return;
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (modelRow < 0 || modelRow >= model.getRowCount()) return;
            Object id = model.getValueAt(modelRow, 0);
            if (!(id instanceof Integer)) return;
            int userId = ((Integer) id).intValue();
            // Load the full customer object so the edit form has current values.
            Async.run(() -> userService.getAllCustomers(), all -> {
                for (User u : all) {
                    if (u.getUserId() == userId) {
                        startEdit(u);
                        return;
                    }
                }
                banner.error("Customer not found.");
            }, ex -> banner.error("Unable to load customer details."));
        }
    }

    private JDialog buildEditDialog() {
        JDialog d = new JDialog((Frame) null, "Edit Customer", true);
        d.setUndecorated(false);
        d.getContentPane().setBackground(Theme.BG);
        d.setLayout(new BorderLayout());

        JPanel editContent = buildEditPanel();
        JScrollPane scroller = new JScrollPane(editContent);
        scroller.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroller.getViewport().setBackground(Theme.CARD);
        scroller.getVerticalScrollBar().setUnitIncrement(18);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        d.add(scroller, BorderLayout.CENTER);

        d.pack();
        int width = Math.max(d.getWidth(), 440);
        int height = Math.min(Math.max(d.getHeight(), 360), 620);
        d.setSize(width, height);
        d.setLocationRelativeTo(null);

        d.getRootPane().setDefaultButton(saveBtn);
        d.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeEdit();
            }
        });

        return d;
    }

    private JPanel buildEditPanel() {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));
        card.setBackground(Theme.CARD);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setAlignmentY(Component.TOP_ALIGNMENT);

        JPanel identity = new JPanel(new BorderLayout(0, 8));
        identity.setOpaque(false);
        Widgets.Avatar avatar = new Widgets.Avatar(54);
        avatar.setName2(currentEditName());
        avatar.setPreferredSize(new Dimension(54, 54));
        avatar.setMaximumSize(new Dimension(54, 54));
        identity.add(avatar, BorderLayout.CENTER);
        JPanel identityText = new JPanel();
        identityText.setOpaque(false);
        identityText.setLayout(new BoxLayout(identityText, BoxLayout.Y_AXIS));
        JLabel cap = Theme.heading("Edit customer");
        JLabel sub = Theme.muted("Update the customer's contact details.");
        identityText.add(cap);
        identityText.add(sub);
        identity.add(identityText, BorderLayout.CENTER);
        identity.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.add(Theme.formRow("Name", nameField));
        fields.add(Theme.formRow("Email", emailField));
        fields.add(Theme.formRow("Phone", phoneField));
        fields.add(Theme.formRow("Address", addressField));
        fields.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancelBtn);
        buttons.add(saveBtn);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        cancelBtn.addActionListener(e -> { closeEdit(); banner.clear(); });
        saveBtn.addActionListener(e -> saveEdit());

        form.add(identity);
        form.add(gap(16));
        form.add(fields);
        form.add(gap(16));
        form.add(buttons);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private void applyFilter() {
        String q = search.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q), 1, 2, 3));
        int shown = table.getRowCount();
        countLabel.setText(shown + " shown");
        if (editingUserId > 0 && !rowContains(editingUserId)) {
            closeEdit();
        }
    }

    private boolean rowContains(int userId) {
        for (int i = 0; i < model.getRowCount(); i++) {
            Object id = model.getValueAt(i, 0);
            if (id instanceof Integer && ((Integer) id).intValue() == userId) return true;
            if (id instanceof String) {
                try { if (Integer.parseInt(id.toString()) == userId) return true; } catch (NumberFormatException ignored) {}
            }
        }
        return false;
    }

    @Override
    public void onShow() {
        banner.clear();
        closeEdit();
        Async.run(userService::getAllCustomers, list -> {
            model.setRowCount(0);
            for (User u : list) { // passwords are never shown
                renderRow(u);
            }
            applyFilter();
        }, ex -> banner.error("Unable to load customers. Please try again."));
    }

    @Override
    public void reset() {
        model.setRowCount(0);
        search.setText("");
        closeEdit();
        banner.clear();
    }

    private void closeEdit() {
        editingUserId = -1;
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
        saveBtn.setEnabled(true);
        if (editDialog != null && editDialog.isVisible()) {
            editDialog.setVisible(false);
        }
    }

    private void startEdit(User u) {
        editingUserId = u.getUserId();
        nameField.setText(u.getName() == null ? "" : u.getName());
        emailField.setText(u.getEmail() == null ? "" : u.getEmail());
        phoneField.setText(u.getPhone() == null ? "" : u.getPhone());
        addressField.setText(u.getAddress() == null ? "" : u.getAddress());
        saveBtn.setEnabled(true);
        table.clearSelection();
        editDialog.pack();
        editDialog.setLocationRelativeTo(table);
        editDialog.setVisible(true);
    }

    private String currentEditName() {
        return nameField.getText();
    }

    private void focusNameField() {
        nameField.requestFocusInWindow();
        nameField.selectAll();
    }

    private void saveEdit() {
        if (editingUserId < 0) return;
        final int userId = editingUserId;
        final String name = nameField.getText().trim();
        final String email = emailField.getText().trim();
        final String phone = phoneField.getText().trim();
        final String address = addressField.getText().trim();
        saveBtn.setEnabled(false);
        Async.run(() -> userService.editCustomer(userId, name, email, phone, address), result -> {
            saveBtn.setEnabled(true);
            if (result.isSuccess()) {
                banner.success(result.getMessage());
                closeEdit();
                onShow();
            } else {
                banner.error(result.getMessage());
            }
        }, ex -> {
            saveBtn.setEnabled(true);
            banner.error("Unable to update the customer. Please try again.");
        });
    }
}
