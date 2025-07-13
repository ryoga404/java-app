import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    
    private String sessionId;  // セッションID保持

    public MainFrame() {
        setTitle("家計簿アプリ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // 画面パネルを追加
        cardPanel.add(new TopPanel(this), "top");
        cardPanel.add(new RegisterPanel(this), "register");
        cardPanel.add(new LoginPanel(this), "login");
        // ログイン後画面例
        cardPanel.add(new HomePanel(this), "home");

        add(cardPanel);
        showPanel("login");
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void showPanel(String name) {
        SessionDAO sessionDAO = new SessionDAO();
        boolean validSession = (sessionId != null) && sessionDAO.isSessionValid(sessionId);

        if ("login".equals(name) || "register".equals(name)) {
            if (validSession) {
                // ログイン済みならログイン・登録画面は不要なのでホームへ
                name = "home";
            }
        } else {
            // login/register以外は有効なセッションが必須
            if (!validSession) {
                JOptionPane.showMessageDialog(this, "セッションが無効です。再ログインしてください。");
                sessionId = null;
                name = "login";
            }
        }
        cardLayout.show(cardPanel, name);
    }
}
