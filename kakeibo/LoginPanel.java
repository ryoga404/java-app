import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
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