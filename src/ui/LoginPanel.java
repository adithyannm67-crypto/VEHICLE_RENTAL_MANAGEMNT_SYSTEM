package ui;

import model.User;
import service.ServiceResult;
import service.UserService;
import ui.Widgets.AppButton;
import ui.Widgets.Banner;
import ui.Widgets.Card;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends Screen {

    private final UserService userService = new UserService();
    private final JTextField emailField = Theme.textField();
    private final JPasswordField passwordField = Theme.passwordField();
    private final Banner banner = new Banner();
    private final AppButton loginBtn = new AppButton("LOGIN");

    public LoginPanel(AppFrame app) {
        super(app);

        Card card = new Card(new BorderLayout(), 36);
        JPanel col = column();

        JLabel title = Theme.title("Welcome Back");
        JLabel sub = Theme.muted("Sign in to continue");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(title);
        col.add(gap(4));
        col.add(sub);
        col.add(gap(18));
        col.add(banner);
        col.add(gap(8));
        col.add(Theme.formRow("Email", emailField));
        col.add(gap(14));
        col.add(Theme.formRow("Password", passwordField));
        col.add(gap(22));

        JPanel loginWrap = new JPanel(new BorderLayout());
        loginWrap.setOpaque(false);
        loginWrap.add(loginBtn);
        loginWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        col.add(loginWrap);
        col.add(gap(22));

        JLabel q = Theme.muted("Don't have an account?");
        col.add(centered(q));
        col.add(gap(10));
        AppButton create = new AppButton("CREATE ACCOUNT", AppButton.Variant.SECONDARY);
        JPanel createWrap = new JPanel(new BorderLayout());
        createWrap.setOpaque(false);
        createWrap.add(create);
        createWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        createWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        col.add(createWrap);
        col.add(gap(12));
        AppButton back = new AppButton("Back to start", AppButton.Variant.LINK);
        col.add(centered(back));

        card.add(col, BorderLayout.CENTER);

        loginBtn.addActionListener(e -> doLogin());
        create.addActionListener(e -> app.navigate(AppFrame.REGISTER));
        back.addActionListener(e -> app.navigate(AppFrame.WELCOME));

        setContent(card, 460, Widgets.CenterColumnLayout.CENTER, true);
    }

    /** Lets the register screen show a message here (used only if auto-login is not possible). */
    public void showInfo(String message) { banner.info(message); }

    @Override public JButton defaultButton() { return loginBtn; }

    @Override
    public void onShow() {
        if (banner.getMessage() == null || banner.getMessage().isEmpty()) banner.clear();
        SwingUtilities.invokeLater(() -> {
            if (emailField.getText().isEmpty()) emailField.requestFocusInWindow();
            else passwordField.requestFocusInWindow();
        });
    }

    @Override
    public void reset() {
        emailField.setText("");
        passwordField.setText("");
        banner.clear();
        loginBtn.setEnabled(true);
        loginBtn.setText("LOGIN");
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (email.isEmpty() || password.isEmpty()) {
            banner.error("Please enter your email and password.");
            return;
        }
        banner.clear();
        loginBtn.setEnabled(false);
        loginBtn.setText("SIGNING IN...");
        Async.run(() -> userService.login(email, password), result -> {
            loginBtn.setEnabled(true);
            loginBtn.setText("LOGIN");
            if (result.isSuccess()) {
                User user = result.getData();
                reset();
                app.loginSuccess(user);
            } else {
                passwordField.setText("");
                banner.error(result.getMessage());
            }
        }, ex -> {
            loginBtn.setEnabled(true);
            loginBtn.setText("LOGIN");
            banner.error("Unable to sign in right now. Please try again.");
        });
    }
}
