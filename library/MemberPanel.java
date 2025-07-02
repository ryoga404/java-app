package jp.ac.kcs.swing.library;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MemberPanel extends JPanel {
	DB dbcon = new DB();
    public MemberPanel(MainFrame frame) {
        //レイアウトに新しいGridLayout(3, 2)
        setLayout(new GridLayout(3, 2));
        //IDのテキストフィールド
       
    	JTextField id = new JTextField(20);
    	//名前のテキストフィールド
    	JTextField name = new JTextField(30);
    	//登録ボタン
    	JButton insertMember = new JButton("会員登録");
        //TOPに戻るボタン
    	JButton BackTop = new JButton("トップへ戻る");
    	//登録ボタンリスナーで押されたらDBのインサート発動
    	
    	insertMember.addActionListener(e -> {
    			dbcon.insertMember(id.getText(), name.getText());
        		JOptionPane.showMessageDialog(this, "会員登録が完了しました。");
    	});

        
        //TOPに戻るボタン
        BackTop.addActionListener(e -> frame.showPanel("TOP"));
        
        //カードに部品を追加
        add(id);
        add(name);
        add(insertMember);
        add(BackTop);

    }
}
