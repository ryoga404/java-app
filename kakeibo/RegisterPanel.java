import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class RegisterPanel extends JPanel implements ActionListener {
    private JPasswordField pass;
    private JPasswordField pass2;
    private static MainFrame mainFrame;
    private UserDAO userDAO;
    private JTextField userIDField;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userDAO = new UserDAO();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        userIDField = new JTextField(20);
        pass = new JPasswordField(20);
        pass2 = new JPasswordField(20);

        JButton tButton = new JButton("登録");
        JButton TOPButton = new JButton("TOP");

        tButton.addActionListener(this);
        TOPButton.addActionListener(e -> {
            if (mainFrame != null) mainFrame.showPanel("TOP");
        });

        add(new JLabel("新規登録"));
        add(new JLabel("ユーザー名（英数字8〜20文字）"));
        add(userIDField);
        add(new JLabel("パスワード設定（8文字以上、大文字・小文字・数字を含む）"));
        add(pass);
        add(new JLabel("もう一度入力"));
        add(pass2);
        add(tButton);
        add(TOPButton);
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
                mainFrame.showPanel("LOGIN"); // ログイン画面へ遷移
            }
        } else {
            JOptionPane.showMessageDialog(this, "登録に失敗しました", "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("新規登録画面");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            RegisterPanel panel = new RegisterPanel(null);
            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}
