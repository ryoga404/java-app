import java.awt.CardLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private String sessionId;      // セッションID
    private String currentUserId;  // 現在ログイン中のユーザーID 

    private Set<String> addedPanels = new HashSet<>(); // 追加済みパネル名

    public MainFrame() {
        setTitle("家計簿アプリ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // 初期パネルを登録
        addPanel("top", new TopPanel(this));
        addPanel("register", new RegisterPanel(this));
        addPanel("login", new LoginPanel(this));

        add(cardPanel);
        showPanel("login");
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void showPanel(String name) {
        SessionDAO sessionDAO = new SessionDAO();
        boolean validSession = (sessionId != null) && sessionDAO.isSessionValid(sessionId);

        if ("login".equals(name) || "register".equals(name)) {
            if (validSession) {
                name = "home";
            }
        } else {
            if (!validSession) {
                JOptionPane.showMessageDialog(this, "セッションが無効です。再ログインしてください。");
                sessionId = null;
                currentUserId = null;
                name = "login";
            }
        }

        // 毎回最新状態にしたいパネルは再生成
        if ("addRecord".equals(name)) {
            cardPanel.add(new AddRecordPanel(this), "addRecord");
            addedPanels.add("addRecord"); // 登録済みとする（なくてもOK）
        } else if (!addedPanels.contains(name)) {
            // 初回のみ生成
            switch (name) {
                case "home":
                    addPanel(name, new HomePanel(this));
                    break;
                // 必要に応じて他パネルも追加
            }
        }

        cardLayout.show(cardPanel, name);
    }

    private void addPanel(String name, JPanel panel) {
        cardPanel.add(panel, name);
        addedPanels.add(name);
    }
}
