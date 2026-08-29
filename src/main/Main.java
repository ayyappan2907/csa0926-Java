package main;

import auth.UserSession;
import controller.GameController;
import gui.FactoryFrame;
import gui.LoginScreen;
import gui.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Factory Auto Chain — Entry Point.
 *
 * Shows the Google login screen first.
 * The game window launches after authentication.
 */
public class Main {
    public static void main(String[] args) {
        // Initialize central design system and Google Fonts Inter
        UITheme.init();

        SwingUtilities.invokeLater(() -> {
            LoginScreen loginScreen = new LoginScreen(Main::launchGame);
            loginScreen.setVisible(true);
        });
    }

    /** Called on the EDT once the user has authenticated. */
    public static void launchGamePublic(UserSession session) {
        launchGame(session);
    }

    /** Called on the EDT once the user has authenticated. */
    private static void launchGame(UserSession session) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("[APP] Launching game for: " + session);

                GameController controller = new GameController(session);
                FactoryFrame frame = new FactoryFrame(controller);

                frame.setVisible(true);
                frame.setState(Frame.NORMAL);
                frame.toFront();
                frame.requestFocus();
                frame.setAlwaysOnTop(true);
                frame.setAlwaysOnTop(false);

                System.out.println("[APP] Game window launched for " + (session != null ? session.getName() : "Guest"));
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error launching Factory Auto Chain:\n" + ex.getMessage(),
                    "Launch Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
