import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class RegisterPanel extends JPanel {
	
    private static MainFrame mainFrame;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        // 縦方向に整列
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //UserIDを入力
        JTextField UserID = new JTextField(20);
        //passwaord を入力
        JTextField pass = new JTextField(20);
        //二段階確認様
        JTextField pass2 = new JTextField(20);
        //登録ボタン
        JButton tButton = new JButton("登録");
        //元の画面に戻る
        JButton TOPButton = new JButton("TOP");
        
        //ボタンが押された動作
        tButton.addActionListener(null);
        TOPButton.addActionListener(null);
        //frameに追加
        add(new JLabel("新規登録"));
        add(new JLabel("User名"));
        add(UserID);
        add(new JLabel("パスワード設定"));
        add(pass);
        add(new JLabel("もう一度入力"));
        add(pass2);
        add(tButton);
        add(TOPButton);
    }
    public static void main(String[] args) {
        // JFrame（ウィンドウ）を作って RegisterPanel を表示
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("新規登録画面");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);

            RegisterPanel panel = new RegisterPanel(null); // mainFrame が不要なら null でOK
            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}

