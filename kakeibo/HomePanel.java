import java.awt.*;
import javax.swing.*;

public class HomePanel extends JPanel {

    private MainFrame mainFrame;

    public HomePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // HomePanel 自体のレイアウトを BorderLayout に設定
        setLayout(new BorderLayout());

        // --- ヘッダー ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        headerPanel.setBackground(new Color(60, 141, 188)); // #3c8dbc

        JLabel usernameLabel = new JLabel("ユーザー名：田中 太郎");
        JLabel groupLabel = new JLabel("グループ：家族共有");
        JButton logoutButton = new JButton("ログアウト");

        usernameLabel.setForeground(Color.WHITE);
        groupLabel.setForeground(Color.WHITE);

        headerPanel.add(usernameLabel);
        headerPanel.add(groupLabel);
        headerPanel.add(logoutButton);

        // --- 左メニュー ---
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(244, 244, 244));
        menuPanel.setPreferredSize(new Dimension(200, 600));  // 600は任意
        menuPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] menuItems = {
            "データ登録", "データ抽出（カレンダー）",
            "インポート/エクスポート", "グループ管理"
        };

        for (String item : menuItems) {
            JButton btn = new JButton(item);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            menuPanel.add(btn);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // --- 中央コンテンツ ---
        JPanel mainContent = new JPanel();
        mainContent.setBackground(Color.WHITE);
        JLabel placeholder = new JLabel("ここに機能画面が表示されます");
        mainContent.add(placeholder);

        // --- 配置 ---
        add(headerPanel, BorderLayout.NORTH);
        add(menuPanel, BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
    }
}
