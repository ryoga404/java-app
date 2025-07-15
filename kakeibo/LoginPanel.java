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

        // IDのテキストフィールド
        JTextField idField = new JTextField();

        // パスワードのフィールドを JPasswordField に変更
        JPasswordField passField = new JPasswordField();

        // ログインボタン
        JButton loginBtn = new JButton("ログイン");

        // TOPに戻るボタン
        JButton backBtn = new JButton("TOPへ戻る");

        // ログインボタンのアクションリスナー
        loginBtn.addActionListener(e -> {
            String userId = idField.getText().trim();
            char[] password = passField.getPassword();  // JPasswordField から char[] を取得

            UserDAO userDAO = new UserDAO();
            // パスワードは char[] で渡す
            String isLoginSuccess = userDAO.login(userId, new String(password));  // char[] から String に変換

            if (isLoginSuccess != null) {
                SessionDAO sessionDAO = new SessionDAO();
                String sessionId = sessionDAO.createSession(userId);

                if (sessionId != null) {
                    if (mainFrame != null) {
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

        // TOPに戻るボタン
        backBtn.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel("TOP");
            } else {
                JOptionPane.showMessageDialog(this, "TOPへ戻ります（ダミー）");
            }
        });

        // UI 部品をカードに追加
        add(new JLabel("ユーザーID"));
        add(idField);
        add(new JLabel("パスワード"));
        add(passField);
        add(loginBtn);
        add(backBtn);
    }

    // テスト用 main メソッド（MainFrame 不要で確認可能）
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
