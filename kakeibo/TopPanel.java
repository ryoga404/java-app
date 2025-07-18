import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class TopPanel extends JPanel {
    private MainFrame mainFrame;
    private BufferedImage backgroundImage;
    private final Font titleFont = new Font("Meiryo", Font.BOLD, 48);

    public TopPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 背景画像読み込み
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/resources/image.jpg"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("背景画像が見つかりません: " + e.getMessage());
            backgroundImage = null;
        }

        setLayout(new BorderLayout());

        // タイトル用パネル（縁取り文字、文字位置下げ、高さ十分に）
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(titleFont);

                String text = "家計簿アプリ";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = fm.getAscent() + 80; // 70→80に少し下げて余裕を持たせる

                // 黒の縁取り
                g2.setColor(Color.BLACK);
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2.drawString(text, x + dx, y + dy);
                        }
                    }
                }

                // 中央の白文字
                g2.setColor(Color.WHITE);
                g2.drawString(text, x, y);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(600, 180);  // 150→180にさらに大きくして文字が切れないように
            }
        };
        titlePanel.setOpaque(false);
        add(titlePanel, BorderLayout.NORTH);

        // ボタンパネル（透明）
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        buttonPanel.add(Box.createVerticalStrut(150)); // ボタン位置はそのまま

        int btnWidth = 300;
        int btnHeight = 60;

        JButton registerBtn = createCuteButton("新規登録", "register", btnWidth, btnHeight);
        registerBtn.setAlignmentX(CENTER_ALIGNMENT);
        JButton loginBtn = createCuteButton("ログイン", "login", btnWidth, btnHeight);
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);

        buttonPanel.add(registerBtn);
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(loginBtn);

        add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(240, 248, 255));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private JButton createCuteButton(String text, String panelName, int width, int height) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(170, 210, 255), 0, getHeight(), new Color(70, 140, 255));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                GradientPaint highlight = new GradientPaint(0, 0, new Color(255, 255, 255, 160), 0, getHeight() / 2, new Color(255, 255, 255, 0));
                g2.setPaint(highlight);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 30, 30);

                g2.dispose();

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 4;

                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(getText(), x + 2, y + 2);

                g2d.setColor(new Color(245, 245, 255));
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }

            @Override
            public void setContentAreaFilled(boolean b) {}
        };

        btn.setFocusPainted(false);
        btn.setFont(new Font("Meiryo", Font.BOLD, 26));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setOpaque(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setCursor(Cursor.getDefaultCursor());
            }
        });

        btn.addActionListener(e -> {
            mainFrame.showPanel(panelName);
        });

        return btn;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 400);
    }
}
