import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JPanel topBarPanel;
    private JLabel userLabel;
    private JButton logoutButton;

    private String sessionId;      // セッションID
    private String currentUserId;  // 現在ログイン中のユーザーID 

    private Set<String> addedPanels = new HashSet<>(); // 追加済みパネル名

    public MainFrame() {
        setTitle("家計簿アプリ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // ヘッダ（右上にユーザ名とログアウトボタン）
        topBarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userLabel = new JLabel("未ログイン");
        logoutButton = new JButton("ログアウト");
        logoutButton.setEnabled(false);
        topBarPanel.add(userLabel);
        topBarPanel.add(logoutButton);

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "ログアウトしますか？", "ログアウト確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                logout();
            }
        });

        add(topBarPanel, BorderLayout.NORTH);

        // メイン画面（カードレイアウト）
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // 初期パネル登録
        addPanel("top", new TopPanel(this));         // ログイン/登録選択画面
        addPanel("register", new RegisterPanel(this));
        addPanel("login", new LoginPanel(this));

        add(cardPanel, BorderLayout.CENTER);
        showPanel("top");  // 最初はトップ画面
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        updateUserLabel();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        updateUserLabel();
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    private void updateUserLabel() {
        if (currentUserId != null) {
            userLabel.setText("ログインユーザー: " + currentUserId);
            logoutButton.setEnabled(true);
        } else {
            userLabel.setText("未ログイン");
            logoutButton.setEnabled(false);
        }
    }

    public void showPanel(String name) {
        SessionDAO sessionDAO = new SessionDAO();
        boolean validSession = (sessionId != null) && sessionDAO.isSessionValid(sessionId);

        // ログイン・登録画面に遷移する場合、すでにログイン中ならhomeへ
        if (name.equals("login") || name.equals("register")) {
            if (validSession) {
                name = "home";
            }
        } else if (!name.equals("top")) {
            // それ以外の画面でセッション無効ならトップへ戻す
            if (!validSession) {
                JOptionPane.showMessageDialog(this, "セッションが無効です。再ログインしてください。");
                sessionId = null;
                currentUserId = null;
                updateUserLabel();
                name = "top";
            }
        }

        // 動的パネル（毎回新しく表示したいパネル）
        if (name.equals("addRecord")) {
            cardPanel.add(new AddRecordPanel(this), "addRecord");
            addedPanels.add("addRecord");
        } else if (!addedPanels.contains(name)) {
            switch (name) {
                case "home":
                    addPanel(name, new HomePanel(this));
                    break;
                // 他のパネルがあればここに追加
            }
        }

        cardLayout.show(cardPanel, name);
    }

    private void addPanel(String name, JPanel panel) {
        cardPanel.add(panel, name);
        addedPanels.add(name);
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
        updateUserLabel();
        showPanel("top");  // ログアウト後はトップ画面に戻る
    }
}
