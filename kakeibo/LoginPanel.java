import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

// ユーザ名とパスワードを入力するフォームとボタン
public class LoginPanel extends JPanel {
    private MainFrame mainFrame;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new GridLayout(3, 2));

        JTextField idField = new JTextField();
        JTextField passField = new JTextField();

        JButton loginBtn = new JButton("ログイン");
        JButton backBtn = new JButton("TOPへ戻る");

        loginBtn.addActionListener(e -> {
            String userId = idField.getText().trim();
            String password = passField.getText().trim();

            UserDAO userDAO = new UserDAO();
            String isLoginSuccess = userDAO.login(userId, password);

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

        backBtn.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel("TOP");
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

    // ★ テスト用 main メソッド（MainFrame 不要で確認可能）
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
