import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class TopPanel {

    public static void main(String[] args) {
        JFrame frame = new JFrame("家計簿アプリ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 250);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        
        int buttonMaxWidth = (int) (frame.getWidth() * 0.9);

        JButton loginBtn = new JButton("ログイン");
        loginBtn.setAlignmentX(JButton.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(buttonMaxWidth, 40));
        loginBtn.setPreferredSize(new Dimension(buttonMaxWidth, 40));
        loginBtn.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(frame, "ログイン画面へ");
        });

        JButton registerBtn = new JButton("新規登録");
        registerBtn.setAlignmentX(JButton.CENTER_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(buttonMaxWidth, 40));
        registerBtn.setPreferredSize(new Dimension(buttonMaxWidth, 40));
        registerBtn.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(frame, "登録画面へ");
        });

        
        panel.add(Box.createVerticalStrut(30));
        panel.add(loginBtn);
        panel.add(Box.createVerticalStrut(20));
        panel.add(registerBtn);
        panel.add(Box.createVerticalGlue());

        frame.add(panel);
        frame.setVisible(true);
    }
}
