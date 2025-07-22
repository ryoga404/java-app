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

    private GroupDAO groupDAO = new GroupDAO();

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
        addPanel("home", new HomePanel(this));  // HomePanelにAddRecord, EditRecordは内包

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
        System.out.println("showPanel called with: " + name);
        SessionDAO sessionDAO = new SessionDAO();
        boolean validSession = (sessionId != null) && sessionDAO.isSessionValid(sessionId);
        System.out.println("sessionId: " + sessionId + ", validSession: " + validSession);

        if ((name.equals("login") || name.equals("register")) && validSession) {
            System.out.println("Valid session exists. Redirecting to home.");
            name = "home";
        } else if (!name.equals("top") && !validSession && !(name.equals("login") || name.equals("register"))) {
            System.out.println("Invalid session. Redirecting to top.");
            JOptionPane.showMessageDialog(this, "セッションが無効です。再ログインしてください。");
            sessionId = null;
            currentUserId = null;
            name = "top";
        }

        // 「home」画面を表示する際にユーザ情報セット
        if (name.equals("home")) {
            HomePanel homePanel = (HomePanel) getPanel("home");
            if (homePanel != null) {
                String groupName = "";
                if (currentUserId != null) {
                    groupName = groupDAO.getGroupNameByUserId(currentUserId);
                    if (groupName == null || groupName.isEmpty()) {
                        groupName = "グループなし";
                    }
                }
                homePanel.setUserInfo(currentUserId, groupName);
            }
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
