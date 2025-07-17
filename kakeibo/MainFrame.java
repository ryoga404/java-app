import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private String sessionId;
    private String currentUserId;

    private Set<String> addedPanels = new HashSet<>();
    private Map<String, JPanel> panels = new HashMap<>();

    public MainFrame() {
        setTitle("家計簿アプリ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        addPanel("top", new TopPanel(this));
        addPanel("register", new RegisterPanel(this));
        addPanel("login", new LoginPanel(this));
        addPanel("home", new HomePanel(this));

        add(cardPanel, BorderLayout.CENTER);
        showPanel("top");
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

    private void addPanel(String name, JPanel panel) {
        panels.put(name, panel);
        cardPanel.add(panel, name);
        addedPanels.add(name);
    }

    private JPanel getPanel(String name) {
        return panels.get(name);
    }

    public void showPanel(String name) {
        SessionDAO sessionDAO = new SessionDAO();
        boolean validSession = (sessionId != null) && sessionDAO.isSessionValid(sessionId);

        // ログイン済みならlogin/registerに行こうとしたらhomeへ
        if (name.equals("login") || name.equals("register")) {
            if (validSession) {
                name = "home";
            }
        } else if (!name.equals("top")) {
            // ログインが必要な画面でセッション無効ならトップに戻す
            if (!validSession) {
                JOptionPane.showMessageDialog(this, "セッションが無効です。再ログインしてください。");
                sessionId = null;
                currentUserId = null;
                name = "top";
            }
        }

        // 動的に追加するパネル
        if (name.equals("addRecord") && !addedPanels.contains(name)) {
            addPanel(name, new AddRecordPanel(this));
        } else if (name.equals("editRecord") && !addedPanels.contains(name)) {
            addPanel(name, new EditRecordPanel(this));
        }

        // homeパネルにユーザー情報をセット
        if (name.equals("home")) {
            HomePanel homePanel = (HomePanel) getPanel("home");
            if (homePanel != null) {
                homePanel.setUserInfo(currentUserId, "", sessionId);
            }
        } else if (name.equals("addRecord")) {
            AddRecordPanel addPanel = (AddRecordPanel) getPanel("addRecord");
            if (addPanel != null) {
                addPanel.refreshUserInfo();
            }
        } else if (name.equals("editRecord")) {
            EditRecordPanel editPanel = (EditRecordPanel) getPanel("editRecord");
            if (editPanel == null) {
                editPanel = new EditRecordPanel(this);
                addPanel("editRecord", editPanel);
            }
            editPanel.loadData();
            editPanel.updateTableData();
            editPanel.refreshUserInfo();  // ← ここを追加！
        }

        cardLayout.show(cardPanel, name);
    }

    public void logout() {
        if (sessionId != null) {
            SessionDAO sessionDAO = new SessionDAO();
            boolean deleted = sessionDAO.deleteSession(sessionId);
            if (!deleted) {
                JOptionPane.showMessageDialog(this, "ログアウト処理に失敗しました。", "エラー", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        sessionId = null;
        currentUserId = null;
        showPanel("top");
    }
}
