package jp.ac.kcs.swing.library;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LendPanel extends JPanel {
	DB dbcon = new DB();
    public LendPanel(MainFrame frame) {
    	setLayout(new GridLayout(3,2));
        //ボタンとかフィールド作成
    	JTextField id = new JTextField(20); 
    	JTextField code = new JTextField(30);
    	JButton update = new JButton("貸出");
    	JButton BackTop = new JButton("トップへ戻る");
    	//DB実行
    	
    	update.addActionListener(e -> {
    		dbcon.lendBook(id.getText(), code.getText());
    	});
    	
        //TOP戻る
    	BackTop.addActionListener(e -> frame.showPanel("TOP"));
        //部品追加
    	
    	add(id);
    	add(code);
    	add(update);
    	add(BackTop);

    }
}
