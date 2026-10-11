package ui;

import model.User;
import service.ServiceResult;
import service.UserService;
import ui.Widgets.AppButton;
import ui.Widgets.Banner;
import ui.Widgets.Card;
import util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends Screen {

    private final UserService userService = new UserService();
    private final JTextField nameField = Theme.textField();
    private final JTextField emailField = Theme.textField();
    private final JTextField phoneField = Theme.textField();
    private final JTextField addressField = Theme.textField();
    private final JPasswordField passwordField = Theme.passwordField();
    private final JPasswordField confirmField = Theme.passwordField();
    private final Banner banner = new Banner();
    private final AppButton createBtn = new AppButton("CREATE ACCOUNT");

    public RegisterPanel(AppFrame app) {
        super(app);

        Card card = new Card(new BorderLayout(), 36);
        JPanel col = column();

        JLabel title = Theme.title("Create Account");
        JLabel sub = Theme.muted("It only takes a minute");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(title);
        col.add(gap(4));
        col.add(sub);
        col.add(gap(18));
        col.add(banner);
        col.add(gap(8));

        JPanel grid = new JPanel(new GridLayout(0, 2, 18, 14));
        grid.setOpaque(false);
        grid.add(Theme.formRow("Name", nameField));
        grid.add(Theme.formRow("Email", emailField));
        grid.add(Theme.formRow("Phone", phoneField));
        grid.add(Theme.formRow("Address (Optional)", addressField));
        grid.add(Theme.formRow("Password", passwordField));
        grid.add(Theme.formRow("Confirm Password", confirmField));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, grid.getPreferredSize().height));
        col.add(grid);
        col.add(gap(8));
        JLabel hint = Theme.label("Password: at least 6 characters, with a letter and a number.", Theme.font(12), Theme.MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(hint);
        col.add(gap(20));

        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setOpaque(false);
        btnWrap.add(createBtn);
        btnWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        col.add(btnWrap);
        col.add(gap(16));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        bottom.setOpaque(false);
        bottom.add(Theme.muted("Already have an account?"));
        AppButton login = new AppButton("Login", AppButton.Variant.LINK);
        bottom.add(login);
        bottom.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        col.add(bottom);

        card.add(col, BorderLayout.CENTER);

        createBtn.addActionListener(e -> doRegister());
        login.addActionListener(e -> app.navigate(AppFrame.LOGIN));

        setContent(card, 700, Widgets.CenterColumnLayout.CENTER, true);
    }

    @Override public JButton defaultButton() { return createBtn; }

    @Override public void onShow() { banner.clear(); SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow()); }

    @Override
    public void reset() {
        for (JTextField f : new JTextField[]{nameField, emailField, phoneField, addressField, passwordField, confirmField}) f.setText("");
        banner.clear();
        createBtn.setEnabled(true);
        createBtn.setText("CREATE ACCOUNT");
    }

    private String validate(String name, String email, String phone, String pw, String confirm) {
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pw.isEmpty() || confirm.isEmpty())
            return "Please fill in all required fields.";
        if (!ValidationUtil.isValidEmail(email)) return "Please enter a valid email address.";
        if (!ValidationUtil.isValidPhone(phone)) return "Phone number must be exactly 10 digits.";
        if (!ValidationUtil.isValidPassword(pw)) return "Password must be at least 6 characters and contain a letter and a number.";
        if (!pw.equals(confirm)) return "Passwords do not match.";
        return null;
    }

    private void doRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String pw = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        String problem = validate(name, email, phone, pw, confirm);
        if (problem != null) { banner.error(problem); return; }

        banner.clear();
        createBtn.setEnabled(false);
        createBtn.setText("CREATING...");
        // register, then sign the new user straight in
        Async.run(() -> {
            ServiceResult<User> reg = userService.register(name, email, phone, pw, address);
            if (!reg.isSuccess()) return reg;
            ServiceResult<User> login = userService.login(email, pw);
            return login.isSuccess() ? login : ServiceResult.<User>ok("Account created. Please log in.", null);
        }, result -> {
            createBtn.setEnabled(true);
            createBtn.setText("CREATE ACCOUNT");
            if (!result.isSuccess()) { banner.error(result.getMessage()); return; }
            if (result.getData() != null) {
                reset();
                app.loginSuccess(result.getData());
            } else {
                reset();
                ((LoginPanel) app.getScreen(AppFrame.LOGIN)).showInfo(result.getMessage());
                app.navigate(AppFrame.LOGIN);
            }
        }, ex -> {
            createBtn.setEnabled(true);
            createBtn.setText("CREATE ACCOUNT");
            banner.error("Unable to create your account. Please try again.");
        });
    }
}
