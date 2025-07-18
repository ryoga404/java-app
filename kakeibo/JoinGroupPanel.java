import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class JoinGroupPanel extends JPanel {
    private JTextField groupNameField;
    private JLabel messageLabel;

    private final MainFrame mainFrame;

    public JoinGroupPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(10, 10));

        // 上部ボタンパネル
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton homeButton = new JButton("ホームへ戻る");
        JButton logoutButton = new JButton("ログアウト");
        topPanel.add(homeButton);
        topPanel.add(logoutButton);
        add(topPanel, BorderLayout.NORTH);

        // 中央入力パネル
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel label = new JLabel("参加するグループ名：");
        groupNameField = new JTextField(20);
        JButton joinButton = new JButton("参加");

        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(groupNameField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        centerPanel.add(joinButton, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // メッセージラベル
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setForeground(Color.BLUE);
        add(messageLabel, BorderLayout.SOUTH);

        // ボタンアクション

        homeButton.addActionListener(e -> {
            clearMessage();
            mainFrame.showPanel("home");
        });

        logoutButton.addActionListener(e -> {
            clearMessage();
            mainFrame.logout();
        });

        joinButton.addActionListener(e -> joinGroup());
    }

    private void joinGroup() {
        clearMessage();

        String groupName = groupNameField.getText().trim();
        if (groupName.isEmpty()) {
            showError("グループ名を入力してください。");
            return;
        }

        String userId = mainFrame.getCurrentUserId();
        if (userId == null) {
            showError("ユーザーがログインしていません。");
            return;
        }

        GroupDAO groupDAO = new GroupDAO();

        boolean success = false;
        try {
            success = GroupDAO.joinGroup(userId, groupName);
        } catch (Exception ex) {
            showError("参加処理中にエラーが発生しました。");
            ex.printStackTrace();
            return;
        }

        if (success) {
            showMessage("グループ「" + groupName + "」に参加しました。");
            groupNameField.setText("");
        } else {
            showError("グループへの参加に失敗しました。");
        }
    }

    private void showMessage(String msg) {
        messageLabel.setForeground(new Color(0, 128, 0)); // 緑
        messageLabel.setText(msg);
    }

    private void showError(String msg) {
        messageLabel.setForeground(Color.RED);
        messageLabel.setText(msg);
    }

    private void clearMessage() {
        messageLabel.setText(" ");
    }
}
