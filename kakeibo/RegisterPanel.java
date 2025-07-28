import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class RegisterPanel extends JPanel implements ActionListener {
    private JPasswordField pass;
    private JPasswordField pass2;
    private static MainFrame mainFrame;
    private UserDAO userDAO;
    private JTextField userIDField;
    private BufferedImage backgroundImage;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userDAO = new UserDAO();

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

        Font labelFont = new Font("Meiryo", Font.BOLD, 20);
        Color labelColor = Color.BLACK;
        Font fieldFont = new Font("Meiryo", Font.PLAIN, 14);
        Font buttonFont = new Font("Meiryo", Font.BOLD, 18);

        Dimension fieldSize = new Dimension(200, 28);

        JLabel titleLabel = new JLabel("新規登録");
        titleLabel.setFont(new Font("Meiryo", Font.BOLD, 28));
        titleLabel.setForeground(labelColor);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleLabel.getPreferredSize().height));

        JLabel userLabel = new JLabel("ユーザー名（英数字8〜20文字）");
        userLabel.setFont(labelFont);
        userLabel.setForeground(labelColor);
        userLabel.setAlignmentX(LEFT_ALIGNMENT);
        userLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, userLabel.getPreferredSize().height));

        userIDField = new JTextField();
        userIDField.setFont(fieldFont);
        userIDField.setBackground(Color.WHITE);
        userIDField.setMaximumSize(fieldSize);
        userIDField.setAlignmentX(LEFT_ALIGNMENT);

        // パスワード設定ラベル（改行なし、一行）
        JLabel passLabel = new JLabel("パスワード設定");
        passLabel.setFont(labelFont);
        passLabel.setForeground(labelColor);
        passLabel.setAlignmentX(LEFT_ALIGNMENT);
        passLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, passLabel.getPreferredSize().height));

        pass = new JPasswordField();
        pass.setFont(fieldFont);
        pass.setBackground(Color.WHITE);
        pass.setMaximumSize(fieldSize);
        pass.setAlignmentX(LEFT_ALIGNMENT);

        JLabel pass2Label = new JLabel("もう一度入力");
        pass2Label.setFont(labelFont);
        pass2Label.setForeground(labelColor);
        pass2Label.setAlignmentX(LEFT_ALIGNMENT);
        pass2Label.setMaximumSize(new Dimension(Integer.MAX_VALUE, pass2Label.getPreferredSize().height));

        pass2 = new JPasswordField();
        pass2.setFont(fieldFont);
        pass2.setBackground(Color.WHITE);
        pass2.setMaximumSize(fieldSize);
        pass2.setAlignmentX(LEFT_ALIGNMENT);

        JButton tButton = new JButton("登録");
        tButton.setFont(buttonFont);
        tButton.setMaximumSize(new Dimension(120, 40));
        tButton.setAlignmentX(LEFT_ALIGNMENT);
        // ボタンの境界線を消す設定
        tButton.setOpaque(true);
        tButton.setContentAreaFilled(true);
        tButton.setFocusPainted(false);
        tButton.setBorder(BorderFactory.createEmptyBorder());
        tButton.addActionListener(this);

        JButton TOPButton = new JButton("TOP");
        TOPButton.setFont(buttonFont);
        TOPButton.setMaximumSize(new Dimension(120, 40));
        TOPButton.setAlignmentX(LEFT_ALIGNMENT);
        // ボタンの境界線を消す設定
        TOPButton.setOpaque(true);
        TOPButton.setContentAreaFilled(true);
        TOPButton.setFocusPainted(false);
        TOPButton.setBorder(BorderFactory.createEmptyBorder());
        TOPButton.addActionListener(e -> {
            if (mainFrame != null) mainFrame.showPanel("top");
        });

        // パスワード条件ラベル（ウィンドウ下部に表示）
        JLabel passConditionLabel = new JLabel("<html><i>※パスワードは8文字以上、大文字・小文字・数字・特殊文字を含む。特殊文字は (!#@_ )</i></html>");
        passConditionLabel.setFont(new Font("Meiryo", Font.PLAIN, 17));
        passConditionLabel.setForeground(Color.DARK_GRAY);
        passConditionLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 15)));

        add(userLabel);
        add(userIDField);
        add(Box.createRigidArea(new Dimension(0, 15)));

        add(passLabel);
        add(passConditionLabel);
        add(pass);
        add(Box.createRigidArea(new Dimension(0, 15)));

        add(pass2Label);
        add(pass2);
        add(Box.createRigidArea(new Dimension(10, 15)));
        
        add(tButton);
        add(Box.createRigidArea(new Dimension(10, 15)));
        add(TOPButton);

        // 下にスペースを作ってパスワード条件を配置
        add(Box.createVerticalGlue());
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String userId = userIDField.getText().trim();
        String pw1 = new String(pass.getPassword());
        String pw2 = new String(pass2.getPassword());

        String userError = CheckUserName.validate(userId);
        if (userError != null) {
            JOptionPane.showMessageDialog(this, userError, "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String pwError = CheckPassword.validate(pw1, userId);
        if (pwError != null) {
            JOptionPane.showMessageDialog(this, pwError, "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!pw1.equals(pw2)) {
            JOptionPane.showMessageDialog(this, "パスワードが一致しません", "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = userDAO.addUser(userId, pw1);
        if (success) {
            JOptionPane.showMessageDialog(this, "登録成功！");
            if (mainFrame != null) {
                mainFrame.showPanel("login"); // ログイン画面へ遷移
            }
        } else {
            JOptionPane.showMessageDialog(this, "登録に失敗しました", "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            float alpha = 0.3f;  // 背景画像の透明度30%
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        } else {
            g.setColor(new Color(240, 248, 255));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
