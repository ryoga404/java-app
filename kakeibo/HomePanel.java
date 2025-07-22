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

public class HomePanel extends JPanel {

    private MainFrame mainFrame;
    private JPanel viewPanel;
    private HashMap<String, JPanel> views = new HashMap<>();
    private HashMap<String, JButton> menuButtons = new HashMap<>();
    private String currentView = null;
    private final Color selectedColor = new Color(200, 220, 240);
    private final Color normalColor = Color.WHITE;

    private JLabel usernameLabel;
    private JLabel groupLabel;
    private JButton logoutButton;

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
        logoutButton.setFocusable(false);

        logoutButton.addActionListener(e -> mainFrame.logout());

        headerPanel.add(usernameLabel);
        headerPanel.add(groupLabel);
        headerPanel.add(logoutButton);

        // --- 左メニュー ---
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(245, 245, 245));
        menuPanel.setPreferredSize(new Dimension(220, 600));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        String[][] menuItems = {
            {"📋データ登録", "addRecord"},
            {"✏️編集", "editRecord"},
            {"🗓データ抽出（カレンダー）", "calendar"},
            {"📁インポート / エクスポート", "importexport"},
            {"👥グループ管理", "group"},
            {"➕グループ作成", "createGroup"},
            {"🔗グループ参加", "joinGroup"}
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
                animateSwitchView(name);
            });

            menuButtons.put(name, btn);
            menuPanel.add(btn);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // --- 中央ビュー（画面表示部） ---
        viewPanel = new JPanel(null);
        viewPanel.setBackground(Color.WHITE);

        // 各画面の登録
        views.put("addRecord", new AddRecordPanel(mainFrame));
        views.put("editRecord", new EditRecordPanel(mainFrame));
        views.put("calendar", createPage("カレンダー画面です。"));
        views.put("importexport", createPage("インポート / エクスポート画面です。"));
        views.put("group", createPage("グループ管理画面です。"));
        views.put("createGroup", new GroupCreatePanel(mainFrame));
        views.put("joinGroup", new JoinGroupPanel(mainFrame));

        for (JPanel panel : views.values()) {
            panel.setVisible(false);
            viewPanel.add(panel);
        }

        add(headerPanel, BorderLayout.NORTH);
        add(menuPanel, BorderLayout.WEST);
        add(viewPanel, BorderLayout.CENTER);

        setActiveMenu("addRecord");
        switchView("addRecord");
    }

    public void setUserInfo(String userId, String group) {
        if (userId == null || userId.isEmpty()) {
            usernameLabel.setText("ログインしていません");
            groupLabel.setText("");
        } else {
            usernameLabel.setText("ユーザーID：" + userId);
            groupLabel.setText("グループ：" + ((group == null || group.isEmpty()) ? "グループなし" : group));
        }
    }

    private JPanel createPage(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setForeground(new Color(80, 80, 80));
        panel.setBackground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void setActiveMenu(String name) {
        for (String key : menuButtons.keySet()) {
            JButton btn = menuButtons.get(key);
            btn.setBackground(key.equals(name) ? selectedColor : normalColor);
        }
    }

    private void animateSwitchView(String nextName) {
        if (nextName.equals(currentView)) return;
        switchView(nextName);
    }

    private void switchView(String nextName) {
        if (currentView != null) {
            JPanel current = views.get(currentView);
            current.setVisible(false);
        }

        JPanel next = views.get(nextName);
        if (next == null) return;

        next.setBounds(0, 0, viewPanel.getWidth(), viewPanel.getHeight());
        next.setVisible(true);
        currentView = nextName;

        if ("editRecord".equals(nextName) && next instanceof EditRecordPanel editPanel) {
            editPanel.refreshUserInfo();
            editPanel.loadData();
            editPanel.updateTableData();
        } else if ("addRecord".equals(nextName) && next instanceof AddRecordPanel addPanel) {
            addPanel.refreshUserInfo();
        }
    }
}
