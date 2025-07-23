import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginPanel extends JPanel {
    private MainFrame mainFrame;
    private BufferedImage backgroundImage;

    private JTextField idField;        // フィールド化
    private JPasswordField passField;  // フィールド化

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 背景画像読み込み
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/resources/image.jpg"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("背景画像が見つかりません: " + e.getMessage());
            backgroundImage = null;
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));
        setOpaque(false);

        // ログイン画面ラベル
        JLabel titleLabel = new JLabel("ログイン画面");
        titleLabel.setFont(new Font("Meiryo", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 30)));

        Font labelFont = new Font("Meiryo", Font.BOLD, 20);
        Font fieldFont = new Font("Meiryo", Font.PLAIN, 14);
        Font buttonFont = new Font("Meiryo", Font.BOLD, 18);

        Dimension labelSize = new Dimension(110, 28);
        Dimension fieldSize = new Dimension(280, 28);

        // ユーザーIDパネル
        JPanel idPanel = new JPanel();
        idPanel.setOpaque(false);
        idPanel.setLayout(new BoxLayout(idPanel, BoxLayout.X_AXIS));

        JLabel idLabel = new JLabel("ユーザーID");
        idLabel.setForeground(Color.BLACK);
        idLabel.setFont(labelFont);
        idLabel.setPreferredSize(labelSize);
        idLabel.setMinimumSize(labelSize);
        idLabel.setMaximumSize(labelSize);

        idField = new JTextField();  // フィールドにセット
        idField.setFont(fieldFont);
        idField.setBackground(Color.WHITE);
        idField.setPreferredSize(fieldSize);
        idField.setMaximumSize(fieldSize);
        idField.setMinimumSize(fieldSize);

        idPanel.add(idLabel);
        idPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        idPanel.add(idField);

        // パスワードパネル
        JPanel passPanel = new JPanel();
        passPanel.setOpaque(false);
        passPanel.setLayout(new BoxLayout(passPanel, BoxLayout.X_AXIS));

        JLabel passLabel = new JLabel("パスワード");
        passLabel.setForeground(Color.BLACK);
        passLabel.setFont(labelFont);
        passLabel.setPreferredSize(labelSize);
        passLabel.setMinimumSize(labelSize);
        passLabel.setMaximumSize(labelSize);

        passField = new JPasswordField();  // フィールドにセット
        passField.setFont(fieldFont);
        passField.setBackground(Color.WHITE);
        passField.setPreferredSize(fieldSize);
        passField.setMaximumSize(fieldSize);
        passField.setMinimumSize(fieldSize);

        passPanel.add(passLabel);
        passPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        passPanel.add(passField);

        // ボタンパネル
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));

        JButton loginBtn = new JButton("ログイン");
        loginBtn.setFont(buttonFont);
        loginBtn.setPreferredSize(new Dimension(130, 45));
        loginBtn.setMaximumSize(new Dimension(130, 45));

        JButton backBtn = new JButton("TOPへ戻る");
        backBtn.setFont(buttonFont);
        backBtn.setPreferredSize(new Dimension(130, 45));
        backBtn.setMaximumSize(new Dimension(130, 45));

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(loginBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        buttonPanel.add(backBtn);
        buttonPanel.add(Box.createHorizontalGlue());

        // ログインボタン処理
        loginBtn.addActionListener(e -> {
            String userId = idField.getText().trim();
            String password = new String(passField.getPassword());

            UserDAO userDAO = new UserDAO();

            boolean isValid = userDAO.checkCredentials(userId, password);
            if (!isValid) {
                JOptionPane.showMessageDialog(this, "ユーザーIDまたはパスワードが正しくありません。");
                return;
            }

            PuzzleCaptchaDialog captcha = new PuzzleCaptchaDialog((JFrame) SwingUtilities.getWindowAncestor(this));
            captcha.setVisible(true);

            if (!captcha.isAuthenticated()) {
                JOptionPane.showMessageDialog(this, "二段階認証に失敗しました。");
                return;
            }

            String sessionId = userDAO.loginAfterCaptcha(userId);
            if (sessionId != null) {
                if (mainFrame != null) {
                    mainFrame.setCurrentUserId(userId);
                    mainFrame.setSessionId(sessionId);
                    mainFrame.showPanel("home");
                }
                JOptionPane.showMessageDialog(this, "ログイン成功！");
                clearInputFields();  // 成功後に入力欄クリアしても良い
            } else {
                JOptionPane.showMessageDialog(this, "セッションの作成に失敗しました。");
            }
        });

        backBtn.addActionListener(e -> {
            if (mainFrame != null) {
                clearInputFields();  // 戻る時もクリア
                mainFrame.showPanel("top");
            } else {
                JOptionPane.showMessageDialog(this, "TOPへ戻ります（ダミー）");
            }
        });

        // パネル配置
        add(idPanel);
        add(Box.createRigidArea(new Dimension(0, 18)));
        add(passPanel);
        add(buttonPanel);
    }

    /** 入力欄を空にするメソッド */
    public void clearInputFields() {
        idField.setText("");
        passField.setText("");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            float alpha = 0.3f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        } else {
            g.setColor(new Color(240, 248, 255));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}

