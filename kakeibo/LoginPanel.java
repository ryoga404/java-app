import java.awt.GridLayout;
import javax.swing.*;

public class LoginPanel extends JPanel {
    private MainFrame mainFrame;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridLayout(3, 2));

        JTextField idField = new JTextField();
        JPasswordField passField = new JPasswordField();

        JButton loginBtn = new JButton("ログイン");
        JButton backBtn = new JButton("TOPへ戻る");

        loginBtn.addActionListener(e -> {
            String userId = idField.getText().trim();
            String password = new String(passField.getPassword());

            UserDAO userDAO = new UserDAO();

            // 1. IDとパスワードが正しいかチェック（ログインではない）
            boolean isValid = userDAO.checkCredentials(userId, password);
            if (!isValid) {
                JOptionPane.showMessageDialog(this, "ユーザーIDまたはパスワードが正しくありません。");
                return;
            }

            // 2. パズルCAPTCHAを表示
            PuzzleCaptchaDialog captcha = new PuzzleCaptchaDialog((JFrame) SwingUtilities.getWindowAncestor(this));
            captcha.setVisible(true);

            if (!captcha.isAuthenticated()) {
                JOptionPane.showMessageDialog(this, "二段階認証に失敗しました。");
                return;
            }

            // 3. CAPTCHA通過後にセッションを作成
            String sessionId = userDAO.loginAfterCaptcha(userId);
            if (sessionId != null) {
                if (mainFrame != null) {
                    mainFrame.setCurrentUserId(userId);
                    mainFrame.setSessionId(sessionId);
                    mainFrame.showPanel("home");
                }
                JOptionPane.showMessageDialog(this, "ログイン成功！");
            } else {
                JOptionPane.showMessageDialog(this, "セッションの作成に失敗しました。");
            }
        });

        backBtn.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel("top");
            } else {
                JOptionPane.showMessageDialog(this, "TOPへ戻ります（ダミー）");
            }
        });

        add(new JLabel("ユーザーID"));
        add(idField);
        add(new JLabel("パスワード"));
        add(passField);
        add(loginBtn);
        add(backBtn);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ログイン画面テスト");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);
            frame.setContentPane(new LoginPanel(null));
            frame.setVisible(true);
        });
    }
}
