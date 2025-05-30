package autosched2;

import javax.swing.*;
import java.awt.*;

public class SignUpFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public SignUpFrame() {
        setTitle("🎀 Create New Account");
        setSize(400, 280);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(255, 240, 250));

        JPanel panel = new JPanel();
        panel.setBackground(new Color(255, 240, 250));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JLabel title = new JLabel("💫 Sign Up for Scheduler");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe Script", Font.BOLD, 18));
        title.setForeground(new Color(255, 20, 147));

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        styleField(usernameField, "New Username");
        styleField(passwordField, "New Password");

        JButton signupBtn = new JButton("🎀 Sign Up");
        signupBtn.setBackground(new Color(255, 105, 180));
        signupBtn.setForeground(Color.WHITE);
        signupBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        signupBtn.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill in all fields please 💔");
                return;
            }

            if (UserManager.registerUser(user, pass)) {
                JOptionPane.showMessageDialog(this, "Welcome! You can now log in 🎉");
                dispose();
                new LoginFrame();
            } else {
                JOptionPane.showMessageDialog(this, "Username already taken 💢");
            }
        });

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(signupBtn);

        add(panel);
        setVisible(true);
    }

    private void styleField(JTextField field, String title) {
        field.setMaximumSize(new Dimension(250, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createTitledBorder(title));
    }
}
