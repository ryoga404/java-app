import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

//ユーザ名とパスワードを入力するフォームとボタン

public class LoginPanel extends JPanel {
    private MainFrame mainFrame;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
   
    	setLayout(new GridLayout(3, 2));
    	//IDのテキストフィールド
    	JTextField idField = new JTextField();
    	//パスワードのテキストフィールド
    	JTextField passField = new JTextField();
    	
        //登録ボタン
    	JButton  loginBtn = new JButton("ログイン");
        //TOPに戻るボタン
    	JButton  backBtn = new JButton("TOPへ戻る");//TOPに戻るボタン
    	
    	//ログインボタンが押されたら
    	loginBtn.addActionListener(e -> {
    		String userId = idField.getText();
            String password = passField.getText();

            // ユーザー認証
            UserDAO userDAO = new UserDAO();
            String isLoginSuccess = userDAO.login(userId, password);

            if (isLoginSuccess != null) {
                // セッション生成
                SessionDAO sessionDAO = new SessionDAO();
                String sessionId = sessionDAO.createSession(userId);

                if (sessionId != null) {
                    mainFrame.setSessionId(sessionId);
                    mainFrame.showPanel("home");
                } else {
                    JOptionPane.showMessageDialog(this, "セッションの作成に失敗しました。");
                }
            } else {
                JOptionPane.showMessageDialog(this, "ログイン失敗。ユーザーIDまたはパスワードが正しくありません。");
            }
        });
    	
    	//TOPに戻るボタン
    	backBtn.addActionListener(e -> mainFrame.showPanel("TOP"));
    	
    	//カードに部品を追加
    	add(new JLabel("ユーザーID"));
        add(idField);
        add(new JLabel("パスワード"));
        add(passField);
        add(loginBtn);
        add(backBtn);
    }
}