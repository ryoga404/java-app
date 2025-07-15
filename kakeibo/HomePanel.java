import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class HomePanel extends JPanel {

    private MainFrame mainFrame;
    private JPanel viewPanel;
    private HashMap<String, JPanel> views = new HashMap<>();
    private HashMap<String, JButton> menuButtons = new HashMap<>();
    private String currentView = null;
    private Color selectedColor = new Color(200, 220, 240);
    private Color normalColor = Color.WHITE;

    private JLabel usernameLabel;
    private JLabel groupLabel;
    private JButton logoutButton;

    private String sessionId; // セッションIDを保持

    public HomePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // --- ヘッダー ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        headerPanel.setBackground(new Color(60, 141, 188));

        usernameLabel = new JLabel("ログインしていません");
        groupLabel = new JLabel("");
        logoutButton = new JButton("ログアウト");

        usernameLabel.setForeground(Color.WHITE);
        groupLabel.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setEnabled(false); // 初期は無効

        headerPanel.add(usernameLabel);
        headerPanel.add(groupLabel);
        headerPanel.add(logoutButton);

        logoutButton.addActionListener(e -> {
            if (sessionId != null) {
                SessionDAO sessionDAO = new SessionDAO();
                boolean deleted = sessionDAO.deleteSession(sessionId);
                if (deleted) {
                    System.out.println("セッション削除成功：" + sessionId);
                } else {
                    System.err.println("セッション削除失敗：" + sessionId);
                }
            }

            setUserInfo(null, null, null);  // ログアウト状態に戻す
            if (mainFrame != null) {
                mainFrame.setSessionId(null);
                mainFrame.setCurrentUserId(null);
                mainFrame.showPanel("top");
            }
        });

        // --- 左メニュー ---
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(245, 245, 245));
        menuPanel.setPreferredSize(new Dimension(220, 600));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        String[][] menuItems = {
            {"📋データ登録", "addRecord"},
            {"🗓データ抽出（カレンダー）", "calendar"},
            {"📁インポート / エクスポート", "importexport"},
            {"👥グループ管理", "group"}
        };

        Font btnFont = new Font("SansSerif", Font.BOLD, 12);
        for (String[] item : menuItems) {
            String label = item[0];
            String name = item[1];

            JButton btn = new JButton(label);
            btn.setFont(btnFont);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btn.setBackground(normalColor);
            btn.setFocusPainted(false);

            btn.addActionListener(e -> {
                setActiveMenu(name);
                if (name.equals("addRecord")) {
                    // MainFrameに遷移依頼（データ登録画面）
                    mainFrame.showPanel("addRecord");
                } else {
                    animateSwitchView(name);
                }
            });

            menuButtons.put(name, btn);
            menuPanel.add(btn);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            if (!name.equals("addRecord")) {
                JPanel page = createPage(label + "画面です。");
                page.setVisible(false);
                views.put(name, page);
            }
        }

        // --- メインビュー（切り替え対象） ---
        viewPanel = new JPanel(null);
        viewPanel.setBackground(Color.WHITE);
        for (JPanel panel : views.values()) {
            viewPanel.add(panel);
        }

        // 初期表示（addRecordはMainFrameで管理するので除外）
        setActiveMenu("calendar");
        if (views.containsKey("calendar")) {
            views.get("calendar").setBounds(0, 0, 1000, 1000);
            views.get("calendar").setVisible(true);
            currentView = "calendar";
        }

        // 初期はログアウト状態
        setUserInfo(null, null, null);

        // --- 全体配置 ---
        add(headerPanel, BorderLayout.NORTH);
        add(menuPanel, BorderLayout.WEST);
        add(viewPanel, BorderLayout.CENTER);
    }

    // ユーザー情報・セッションIDを表示・保持するメソッド
    public void setUserInfo(String username, String group, String sessionId) {
        this.sessionId = sessionId;
        if (username == null || username.isEmpty()) {
            usernameLabel.setText("ログインしていません");
            groupLabel.setText("");
            logoutButton.setEnabled(false);
        } else {
            usernameLabel.setText("ユーザー名：" + username);
            groupLabel.setText("グループ：" + (group == null ? "" : group));
            logoutButton.setEnabled(true);
        }
    }

    // オーバーロード（セッションIDなし）
    public void setUserInfo(String username, String group) {
        setUserInfo(username, group, null);
    }

    // ラベルだけの中央表示画面を作る
    private JPanel createPage(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setForeground(new Color(80, 80, 80));
        panel.setBackground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    // メニューの見た目変更
    private void setActiveMenu(String name) {
        for (String key : menuButtons.keySet()) {
            JButton btn = menuButtons.get(key);
            btn.setBackground(key.equals(name) ? selectedColor : normalColor);
        }
    }

    // アニメーションで画面切り替え（HomePanel内のもののみ）
    private void animateSwitchView(String nextName) {
        if (nextName.equals(currentView)) return;

        JPanel current = views.get(currentView);
        JPanel next = views.get(nextName);
        int width = viewPanel.getWidth();

        current.setBounds(0, 0, width, viewPanel.getHeight());
        next.setBounds(width, 0, width, viewPanel.getHeight());
        next.setVisible(true);

        Timer timer = new Timer(5, null);
        final int[] x = {0};

        timer.addActionListener(e -> {
            x[0] += 20;
            current.setLocation(-x[0], 0);
            next.setLocation(width - x[0], 0);

            if (x[0] >= width) {
                timer.stop();
                current.setVisible(false);
                next.setLocation(0, 0);
                currentView = nextName;
            }
        });

        timer.start();
    }
}
