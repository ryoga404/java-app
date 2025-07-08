//package jp.ac.kcs.swing.library;
package library;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;


public class TopPanel extends JPanel {
    public MainFrame mainFrame;

    public TopPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());

        // 各ボタン作成
        JButton memberButton = new JButton("会員登録");
        JButton bookButton = new JButton("本登録");
        JButton lendButton = new JButton("貸出処理");
        JButton returnButton = new JButton("返却処理");
        JButton listButton = new JButton("一覧表示");

        // すべてのボタンを配列で処理
        JButton[] buttons = { memberButton, bookButton, lendButton, returnButton, listButton };

        // 幅を自動で合わせるために一度 preferredSize を比較
        int maxWidth = 0;
        for (JButton btn : buttons) {
            maxWidth = Math.max(maxWidth, btn.getPreferredSize().width);
        }

        // 同じサイズに揃えて中央寄せ
        for (JButton btn : buttons) {
            Dimension size = new Dimension(maxWidth, btn.getPreferredSize().height);
            btn.setMaximumSize(size);
            btn.setPreferredSize(size);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        // パネル作成・BoxLayout設定（縦並び）
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        // ボタン追加（間にスペース）
        for (int i = 0; i < buttons.length; i++) {
            buttonPanel.add(buttons[i]);
            if (i < buttons.length - 1) {
                buttonPanel.add(Box.createVerticalStrut(10));
            }
        }

        // アクションリスナー登録（ボタンが押されたら特定のパネルに切り替える）s
        memberButton.addActionListener(e -> mainFrame.showPanel("MEMBER"));
        bookButton.addActionListener(e -> mainFrame.showPanel("BOOK"));
        lendButton.addActionListener(e -> mainFrame.showPanel("LEND"));
        returnButton.addActionListener(e -> mainFrame.showPanel("RETURN"));
        listButton.addActionListener(e -> mainFrame.showPanel("LIST"));

        // 中央配置
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(buttonPanel, gbc);
    }
}
