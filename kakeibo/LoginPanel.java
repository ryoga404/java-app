import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginPanel extends JPanel {
    private MainFrame mainFrame;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // レイアウトを設定
        setLayout(new GridLayout(3, 2));

        // IDとパスワード入力フィールド
        JTextField idField = new JTextField();
        JPasswordField passField = new JPasswordField();

        // ボタン作成
        JButton loginBtn = new JButton("ログイン");
        JButton backBtn = new JButton("TOPへ戻る");

        // ログインボタンのアクション
        loginBtn.addActionListener(e -> {
            String userId = idField.getText().trim();
            char[] password = passField.getPassword();

            UserDAO userDAO = new UserDAO();
            String isLoginSuccess = userDAO.login(userId, new String(password)); // 成功時は非nullを想定

            if (isLoginSuccess != null) {
                SessionDAO sessionDAO = new SessionDAO();
                String sessionId = sessionDAO.createSession(userId);

                if (sessionId != null) {
                    if (mainFrame != null) {
                        mainFrame.setCurrentUserId(userId);       // ← これを忘れない！
                        mainFrame.setSessionId(sessionId);
                        mainFrame.showPanel("home");
                    }
                    JOptionPane.showMessageDialog(this, "ログイン成功！セッションID: " + sessionId);
                } else {
                    JOptionPane.showMessageDialog(this, "セッションの作成に失敗しました。");
                }
            } else {
                JOptionPane.showMessageDialog(this, "ログイン失敗。ユーザーIDまたはパスワードが正しくありません。");
            }
        });

        // TOPへ戻るボタンのアクション
        backBtn.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel("top");
            } else {
                JOptionPane.showMessageDialog(this, "TOPへ戻ります（ダミー）");
            }
        });

        // UI 部品の追加
        add(new JLabel("ユーザーID"));
        add(idField);
        add(new JLabel("パスワード"));
        add(passField);
        add(loginBtn);
        add(backBtn);
    }

    // テスト起動用
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
