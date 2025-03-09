package realestate.login;

import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;
import javax.swing.*;

import realestate.RealEstateFrame; // Import realestate.RealEstateFrame from one level lower package

public class LoginFrame extends JFrame implements ActionListener {
    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton, resetPasswordButton;
    private UserManager userManager;

    public LoginFrame() {
        super("User Login");
        userManager = new UserManager();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null); // Center window

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        panel.add(loginButton);

        registerButton = new JButton("Register");
        registerButton.addActionListener(this);
        panel.add(registerButton);

        resetPasswordButton = new JButton("Reset Password");
        resetPasswordButton.addActionListener(this);
        panel.add(resetPasswordButton);

        add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (e.getSource() == loginButton) {
            if (userManager.authenticateUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                openRealEstateFrame();
                this.dispose(); // Close realestate.login window after successful realestate.login
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == registerButton) {
            if (userManager.registerUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Username already exists.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == resetPasswordButton) {
            String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (userManager.resetPassword(username, newPassword.trim())) {
                    JOptionPane.showMessageDialog(this, "Password reset successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Username not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void openRealEstateFrame() {
        this.dispose();

        SwingUtilities.invokeLater(() -> {
            RealEstateFrame realEstateFrame = new RealEstateFrame();
            realEstateFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            realEstateFrame.setSize(1024, 768);
            realEstateFrame.setLocationRelativeTo(null);
            realEstateFrame.setVisible(true);
        });
    }
}
