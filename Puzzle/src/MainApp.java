import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {
    private JLabel statusLabel;

    public MainApp() {
        setTitle("メイン画面");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JButton authButton = new JButton("認証する");
        statusLabel = new JLabel("認証未実施", SwingConstants.CENTER);

        authButton.addActionListener(e -> {
            PuzzleCaptchaDialog dialog = new PuzzleCaptchaDialog(this);
            dialog.setVisible(true);

            if (dialog.isAuthenticated()) {
                statusLabel.setText("認証成功");
            } else {
                statusLabel.setText("認証失敗");
            }
        });

        setLayout(new BorderLayout(10, 10));
        add(authButton, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.setVisible(true);
        });
    }
}
