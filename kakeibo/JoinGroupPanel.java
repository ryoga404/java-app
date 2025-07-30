import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
        setBackground(new Color(245, 248, 255)); // 薄い青系の背景

        // 中央入力パネル
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(getBackground());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel label = new JLabel("参加するグループ名：");
        label.setFont(new Font("Dialog", Font.PLAIN, 18));
        label.setForeground(new Color(33, 111, 237)); // 落ち着いた青色

        groupNameField = new JTextField(20);
        groupNameField.setFont(new Font("Dialog", Font.PLAIN, 16));
        groupNameField.setPreferredSize(new Dimension(250, 30));

        JButton joinButton = new JButton("参加");
        joinButton.setFont(new Font("Dialog", Font.BOLD, 16));
        joinButton.setBackground(new Color(33, 111, 237));
        joinButton.setForeground(Color.WHITE);
        joinButton.setFocusPainted(false);
        joinButton.setPreferredSize(new Dimension(100, 35));

        gbc.insets = new Insets(10, 10, 10, 10);

        // ラベル
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(label, gbc);

        // 入力フィールド
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(groupNameField, gbc);

        // 参加ボタン（下の行、中央寄せ）
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(joinButton, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // メッセージラベル（下部）
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        messageLabel.setPreferredSize(new Dimension(300, 30));
        add(messageLabel, BorderLayout.SOUTH);

        // ボタンアクション
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
            success = groupDAO.joinGroup(userId, groupName);
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
        messageLabel.setForeground(new Color(0, 128, 0)); // 緑色
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
