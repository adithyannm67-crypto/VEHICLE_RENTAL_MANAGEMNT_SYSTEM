import service.UserService;
import ui.AppFrame;
import ui.Theme;

import javax.swing.*;

/**
 * Application entry point.
 * Opens ONE main window (AppFrame); every screen lives inside it.
 */
public class Main {
    public static void main(String[] args) {
        // Modern cross-platform look; falls back to the default if Nimbus is missing.
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }
        Theme.installDefaults();

        // Make sure the default admin account exists (this is the existing behaviour).
        boolean dbOk = true;
        try {
            new UserService().ensureDefaultAdmin();
        } catch (Exception e) {
            dbOk = false;
            System.err.println("[VRS] Database connection problem: " + e.getMessage());
            e.printStackTrace();
        }

        final boolean showDbWarning = !dbOk;
        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame();
            frame.setVisible(true);
            if (showDbWarning) {
                JOptionPane.showMessageDialog(frame,
                        "Could not connect to the database.\nPlease check your connection and restart the application.",
                        "Connection problem", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}
