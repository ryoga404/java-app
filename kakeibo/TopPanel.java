import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class TopPanel extends JPanel {
    private MainFrame mainFrame;
    private JLabel titleLabel;

    public TopPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255)); // 薄い青背景

        // タイトルラベル（上部中央）
        titleLabel = new JLabel("家計簿アプリ");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 48));
        titleLabel.setForeground(new Color(60, 120, 255)); // 青系
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
        add(titleLabel, BorderLayout.NORTH);

        // ボタンを並べるパネル（中央）
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        // 上に空きスペースを入れてボタン群を下寄せ
        buttonPanel.add(Box.createVerticalStrut(80)); // 調整可能な余白

        int btnWidth = 300;
        int btnHeight = 60;

        // 新規登録ボタン
        JButton registerBtn = createAnimatedButton("新規登録", "register", btnWidth, btnHeight);
        registerBtn.setAlignmentX(CENTER_ALIGNMENT);

        // ログインボタン
        JButton loginBtn = createAnimatedButton("ログイン", "login", btnWidth, btnHeight);
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);

        buttonPanel.add(registerBtn);
        buttonPanel.add(Box.createVerticalStrut(20)); // ボタン間の空きを広げる
        buttonPanel.add(loginBtn);

        add(buttonPanel, BorderLayout.CENTER);
    }

    // アニメーション付きのボタンを作成
    private JButton createAnimatedButton(String text, String panelName, int width, int height) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 22));
        btn.setBackground(new Color(100, 160, 255));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 120, 230), 2, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));
        btn.setAlignmentX(CENTER_ALIGNMENT);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(135, 200, 255));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(100, 160, 255));
            }
        });

        btn.addActionListener(e -> {
            animateButtonPop(btn, () -> mainFrame.showPanel(panelName));
        });

        return btn;
    }

    // ポヨンと弾むアニメーション（サイズを変えて中央を維持）
    private void animateButtonPop(JButton btn, Runnable afterAnimation) {
        int steps = 6;
        int interval = 20;
        int maxScale = 15;

        Dimension originalSize = btn.getSize();
        Point originalLocation = btn.getLocation();

        Timer timer = new Timer(interval, null);
        final int[] step = {0};

        timer.addActionListener(e -> {
            int s = step[0];
            if (s <= steps) {
                int scale = maxScale - Math.abs(steps / 2 - s) * (maxScale * 2 / steps);
                btn.setSize(originalSize.width + scale, originalSize.height + scale);
                btn.setLocation(originalLocation.x - scale / 2, originalLocation.y - scale / 2);
                step[0]++;
            } else {
                btn.setSize(originalSize);
                btn.setLocation(originalLocation);
                timer.stop();
                afterAnimation.run();
            }
        });

        timer.start();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 400);
    }
}
