package jp.ac.kcs.swing.library;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BookPanel extends JPanel {
	//DB操作クラスのインスタンス（メソッドを使うため）
	DB dbcon = new DB();
	
    public BookPanel(MainFrame frame) {
        //レイアウトに新しいGridLayout(3, 2)
    	setLayout(new GridLayout(3, 2));
    	//本のコードを入力
    	JTextField BookCode = new JTextField(20);
    	//本の名前を入力
    	JTextField BookName = new JTextField(30);
    	//登録ボタン
    	JButton insertBook = new JButton("本登録");
    	
    	//登録ボタンが押されたらIDとタイトル
    	insertBook.addActionListener(e -> 
    			dbcon.insertBook(BookCode.getText(), BookName.getText()));
    	//TOPに戻るボタン
    	
    	JButton BackTop = new JButton("トップへ戻る");
    	
    	BackTop.addActionListener(e -> frame.showPanel("TOP"));
    	//パネルに部品追加
    	
    	add(BookCode);
    	add(BookName);
    	add(insertBook);
    	add(BackTop);
    	
    }
}
