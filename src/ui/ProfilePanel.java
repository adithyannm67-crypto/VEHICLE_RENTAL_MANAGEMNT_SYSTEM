package ui;

import model.User;
import service.UserService;
import ui.Widgets.AppButton;
import ui.Widgets.Avatar;
import ui.Widgets.Banner;
import ui.Widgets.Card;

import javax.swing.*;
import java.awt.*;

/** Proper profile screen (view mode + inline edit mode). The password is never shown. */
public class ProfilePanel extends Screen {

    private final UserService userService = new UserService();

    /** One caption + value; switches between a label (view) and a text field (edit). */
    private static class Row extends JPanel {
        private final CardLayout cl = new CardLayout();
        private final JLabel view = Theme.label("-", Theme.bold(16), Theme.TEXT);
        private final JTextField edit = Theme.textField();
        private final boolean editable;

        Row(String caption, boolean editable) {
            super(new BorderLayout(0, 4));
            this.editable = editable;
            setOpaque(false);
            add(Theme.label(caption, Theme.font(12), Theme.MUTED), BorderLayout.NORTH);
            JPanel value = new JPanel(cl);
            value.setOpaque(false);
            view.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            value.add(view, "view");
            value.add(edit, "edit");
            add(value, BorderLayout.CENTER);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
            setPreferredSize(new Dimension(100, 66));
        }

        void set(String v) { view.setText(Theme.orDash(v)); edit.setText(v == null ? "" : v); }
        void mode(boolean editing) { cl.show((Container) getComponent(1), editing && editable ? "edit" : "view"); }
        String text() { return edit.getText().trim(); }
    }

    private final Avatar avatar = new Avatar(88);
    private final JLabel nameBig = Theme.label("-", Theme.bold(22), Theme.TEXT);
    private final JLabel emailSmall = Theme.muted("-");
    private final Row nameRow = new Row("Name", true);
    private final Row emailRow = new Row("Email", false);
    private final Row phoneRow = new Row("Phone", true);
    private final Row addressRow = new Row("Address", true);
    private final Banner banner = new Banner();
    private final AppButton editBtn = new AppButton("EDIT PROFILE");
    private final AppButton saveBtn = new AppButton("SAVE CHANGES");
    private final AppButton cancelBtn = new AppButton("Cancel", AppButton.Variant.SECONDARY);
    private final JPanel buttonCards = new JPanel(new CardLayout());

    public ProfilePanel(AppFrame app) {
        super(app);
        JPanel root = column();
        AppButton back = new AppButton("Back to Dashboard", AppButton.Variant.SECONDARY);
        back.addActionListener(e -> app.navigate(AppFrame.CUSTOMER_HOME));
        JPanel header = Widgets.pageHeader("My Profile", "Your account details", back);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(header);

        Card card = new Card(new BorderLayout(), 32);
        JPanel col = column();
        col.add(centered(avatar));
        col.add(gap(12));
        col.add(centered(nameBig));
        col.add(gap(2));
        col.add(centered(emailSmall));
        col.add(gap(18));
        col.add(banner);
        col.add(gap(6));
        col.add(nameRow);
        col.add(emailRow);
        col.add(phoneRow);
        col.add(addressRow);
        col.add(gap(18));

        buttonCards.setOpaque(false);
        JPanel viewButtons = new JPanel(new BorderLayout());
        viewButtons.setOpaque(false);
        viewButtons.add(editBtn);
        JPanel editButtons = new JPanel(new GridLayout(1, 2, 12, 0));
        editButtons.setOpaque(false);
        editButtons.add(cancelBtn);
        editButtons.add(saveBtn);
        buttonCards.add(viewButtons, "view");
        buttonCards.add(editButtons, "edit");
        buttonCards.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        buttonCards.setPreferredSize(new Dimension(100, 44));
        col.add(buttonCards);

        card.add(col, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(card);

        setContent(root, 560, Widgets.CenterColumnLayout.TOP, true);

        editBtn.addActionListener(e -> setEditing(true));
        cancelBtn.addActionListener(e -> { fill(); setEditing(false); banner.clear(); });
        saveBtn.addActionListener(e -> save());
    }

    private void setEditing(boolean editing) {
        for (Row r : new Row[]{nameRow, emailRow, phoneRow, addressRow}) r.mode(editing);
        ((CardLayout) buttonCards.getLayout()).show(buttonCards, editing ? "edit" : "view");
        defaultSaveButton = editing ? saveBtn : null;
        app.getRootPane().setDefaultButton(defaultSaveButton);
    }

    private JButton defaultSaveButton;

    private void fill() {
        User u = app.getCurrentUser();
        if (u == null) return;
        avatar.setName2(u.getName());
        nameBig.setText(u.getName());
        emailSmall.setText(u.getEmail());
        nameRow.set(u.getName());
        emailRow.set(u.getEmail());
        phoneRow.set(u.getPhone());
        addressRow.set(u.getAddress());
    }

    @Override
    public void onShow() {
        banner.clear();
        fill();
        setEditing(false);
    }

    @Override
    public void reset() {
        for (Row r : new Row[]{nameRow, emailRow, phoneRow, addressRow}) r.set("");
        nameBig.setText("-");
        emailSmall.setText("-");
        banner.clear();
    }

    private void save() {
        User u = app.getCurrentUser();
        if (u == null) return;
        final String name = nameRow.text(), phone = phoneRow.text(), address = addressRow.text();
        saveBtn.setEnabled(false);
        Async.run(() -> userService.updateProfile(u.getUserId(), name, phone, address), result -> {
            saveBtn.setEnabled(true);
            if (result.isSuccess()) {
                u.setName(name);
                u.setPhone(phone);
                u.setAddress(address);
                fill();
                setEditing(false);
                banner.success("Profile updated.");
                app.refreshHeader();
            } else {
                banner.error(result.getMessage());
            }
        }, ex -> {
            saveBtn.setEnabled(true);
            banner.error("Unable to update your profile. Please try again.");
        });
    }
}
