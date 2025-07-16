import javax.swing.*;
import java.awt.*;

public class PuzzleCaptchaDialog extends JDialog {
    private PuzzleCaptchaPanel puzzlePanel;
    private boolean authenticated = false;

    public PuzzleCaptchaDialog(Frame owner) {
        super(owner, "パズル認証", true);

        puzzlePanel = new PuzzleCaptchaPanel();

        JButton verifyBtn = new JButton("認証する");
        verifyBtn.addActionListener(e -> {
            if (puzzlePanel.checkPuzzlePosition()) {
                authenticated = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "認証失敗。もう一度試してください。", "エラー", JOptionPane.ERROR_MESSAGE);
            }
        });

        setLayout(new BorderLayout());
        add(puzzlePanel, BorderLayout.CENTER);
        add(verifyBtn, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
