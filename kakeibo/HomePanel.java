//package kakeibo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class HomePanel extends JFrame {

    public HomePanel() {
        setTitle("家計簿アプリ - ホームパネル");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // 画面中央

        // 全体レイアウト（BorderLayout）
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

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
        menuPanel.setPreferredSize(new Dimension(200, getHeight()));
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
        contentPane.add(headerPanel, BorderLayout.NORTH);
        contentPane.add(menuPanel, BorderLayout.WEST);
        contentPane.add(mainContent, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        // SwingはEDTで実行
        SwingUtilities.invokeLater(() -> {
            HomePanel panel = new HomePanel();
            panel.setVisible(true);
        });
    }
}
