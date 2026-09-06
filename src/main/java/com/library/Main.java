package com.library;

import com.library.ui.LoginFrame;

import javax.swing.*;

/** Application entry point. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
