package com.library;

import com.library.ui.LoginFrame;
import com.library.ui.UITheme;

import javax.swing.*;

/** Application entry point. */
public class Main {
    public static void main(String[] args) {
        UITheme.applyGlobalDefaults();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
