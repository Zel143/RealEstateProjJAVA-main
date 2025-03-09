package realestate.login;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

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

        // Load the image
        String imagePath = new File("img/welcome_image.png").getAbsolutePath();
        ImageIcon originalIcon = new ImageIcon(imagePath);

        // Check if the image loads properly
        if (originalIcon.getIconWidth() == -1) {
            System.err.println("Error: Image not found at " + imagePath);
            return;
        }

        // Get original dimensions
        int imgWidth = originalIcon.getIconWidth();
        int imgHeight = originalIcon.getIconHeight();

        // Define a max display size (adjustable)
        int maxWidth = 800;
        int maxHeight = 600;

        // Compute scaling factor to maintain aspect ratio
        double widthScale = (double) maxWidth / imgWidth;
        double heightScale = (double) maxHeight / imgHeight;
        double scale = Math.min(widthScale, heightScale); // Choose the smaller scale to fit within max dimensions

        // Calculate new dimensions
        int newWidth = (int) (imgWidth * scale);
        int newHeight = (int) (imgHeight * scale);

        // Scale image
        Image scaledImage = originalIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        // Create the welcome screen frame
        JFrame welcomeFrame = new JFrame();
        JLabel label = new JLabel(scaledIcon);
        welcomeFrame.add(label);

        // Set dynamic size based on the scaled image
        welcomeFrame.setUndecorated(true);
        welcomeFrame.setSize(newWidth, newHeight);
        welcomeFrame.setLocationRelativeTo(null); // Center the window
        welcomeFrame.setVisible(true);

        // Show image for 5 seconds before opening RealEstateFrame
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    welcomeFrame.dispose();

                    RealEstateFrame realEstateFrame = new RealEstateFrame();
                    realEstateFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    realEstateFrame.setSize(1024, 768);
                    realEstateFrame.setLocationRelativeTo(null);
                    realEstateFrame.setVisible(true);
                });
            }
        }, 5000);
    }

}
