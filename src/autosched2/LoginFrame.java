package autosched2;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("ASC MO Automatic Laboratory Scheduler");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(255, 235, 245));

        JPanel panel = new JPanel();
        panel.setBackground(new Color(255, 235, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JLabel title = new JLabel("ASC MO Automatic Laboratory Scheduler");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(255, 105, 180));

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        styleField(usernameField, "Username");
        styleField(passwordField, "Password");

        JButton loginBtn = new JButton("Log In");
        JButton signupBtn = new JButton("Sign Up");

        loginBtn.setBackground(new Color(255, 105, 180));
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signupBtn.setBackground(new Color(255, 182, 193));
        signupBtn.setForeground(Color.BLACK);
        signupBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        Dimension btnSize = new Dimension(100, 30); // width, height — adjust as needed
        loginBtn.setSize(btnSize);
        signupBtn.setSize(btnSize);

        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBtn.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            if (UserManager.authenticate(user, pass)) {
                dispose();
                new MainTabbedUI(user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Wrong credentials 😭", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        signupBtn.addActionListener(e -> {
            dispose();
            new SignUpFrame();
        });

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(loginBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
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
