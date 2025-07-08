//package jp.ac.kcs.swing.library;
package library;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ReturnPanel extends JPanel {
	DB dbcon = new DB();
    public ReturnPanel(MainFrame frame) {
    	setLayout(new GridLayout(3,2));
        //ボタンとかフィールド作成
    	JTextField code = new JTextField("返却する本コード",30);
    	JButton returnBook = new JButton("返却");
    	JButton BackTop = new JButton("トップへ戻る");
    	//DB実行
    	
    	returnBook.addActionListener(e -> {
    		dbcon.returnBook(code.getText());
    	});
    	
        //TOP戻る
    	BackTop.addActionListener(e -> frame.showPanel("TOP"));
        //部品追加
    	
    	add(code);
    	add(returnBook);
    	add(BackTop);

    }
}
