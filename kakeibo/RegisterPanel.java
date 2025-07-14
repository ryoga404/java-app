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
    //UserDADのインスタンス
    private UserDAO userDAO;
    private JTextField userIDField;
    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userDAO = new UserDAO(); 
        // 縦方向に整列
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //UserIDを入力
        JTextField UserID = new JTextField(20);
        //passwaord を入力
        pass = new JPasswordField(20);
        //二段階確認様
        pass2 = new JPasswordField(20);
        //登録ボタン
        JButton tButton = new JButton("登録");
        //元の画面に戻る
        JButton TOPButton = new JButton("TOP");
        //ボタンが押された動作
        tButton.addActionListener(this);
        
        userIDField = new JTextField(20);
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
    public void actionPerformed (ActionEvent e)	{
    	//ユーザーIDの入力値を取得（前後の空白を除去）。
    	 String userId = this.userIDField.getText().trim();
    	 //パスワードと確認パスワードを取得。
    	 String pass = new String(this.pass.getPassword());
         String pass2 = new String(this.pass2.getPassword());
         //入力されたユーザーIDまたはパスワードが空だったらエラーメッセージを表示し、処理を中断
         if (userId.isEmpty() || pass.isEmpty()) {
             JOptionPane.showMessageDialog(this, "ユーザーIDとパスワードは必須です", "エラー", JOptionPane.ERROR_MESSAGE);
             return;
         }
         //パスワードと確認パスワードが一致しない場合はエラーを表示し、処理を終了。
         if (!pass.equals(pass2)) {
             JOptionPane.showMessageDialog(this, "パスワードが一致しません", "エラー", JOptionPane.ERROR_MESSAGE);
             return;
         }
         /*UserDAO クラスの addUser() メソッドを呼び出して、実際に登録処理を実行。
		 成功したかどうかを boolean 値で受け取ります。
         */
         boolean success = userDAO.addUser(userId, pass);
         if (success) {
             JOptionPane.showMessageDialog(this, "登録成功！");
         } else {
             JOptionPane.showMessageDialog(this, "登録に失敗しました", "エラー", JOptionPane.ERROR_MESSAGE);
         }
     }
         /*if (pass.equals(pass2)) {
             // パスワード一致処理
             JOptionPane.showMessageDialog(this, "登録完了！");
             // 登録処理をここに追加（例: データベースへの保存など）
         } else {
             JOptionPane.showMessageDialog(this, "パスワードが一致しません", "エラー", JOptionPane.ERROR_MESSAGE);
         }
    }*/
		
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

