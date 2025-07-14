import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class TopPanel extends JPanel {
    private MainFrame mainFrame;

    public TopPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        int buttonMaxWidth = 300; // 仮の最大幅（必要に応じて調整可）

        JButton loginBtn = new JButton("ログイン");
        loginBtn.setAlignmentX(JButton.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(buttonMaxWidth, 40));
        loginBtn.setPreferredSize(new Dimension(buttonMaxWidth, 40));
        loginBtn.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(this, "ログイン画面へ");
        });

        JButton registerBtn = new JButton("新規登録");
        registerBtn.setAlignmentX(JButton.CENTER_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(buttonMaxWidth, 40));
        registerBtn.setPreferredSize(new Dimension(buttonMaxWidth, 40));
        registerBtn.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(this, "登録画面へ");
        });

        add(Box.createVerticalStrut(30));
        add(loginBtn);
        add(Box.createVerticalStrut(20));
        add(registerBtn);
        add(Box.createVerticalGlue());
    }
}