package jp.ac.kcs.swing.library;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ListPanel extends JPanel {
	DB dbcon = new DB();
    public ListPanel(MainFrame frame) {
    	
        //レイアウト追加
        setLayout(new BorderLayout());
        
        JTable table;
        DefaultTableModel tableModel;
        JButton BackTop = new JButton("トップへ戻る");
        
        //表示フィールド追加
        tableModel = new DefaultTableModel(new String[] {"タイトル"}, 0);
        table = new JTable(tableModel);
        
        
        //Listを作成string型
        List<String> books = dbcon.getLenBooks();
        
        for(String title : books) {
        	tableModel.addRow(new Object[] {title});
        }
       
        //TOP戻るボタン
        BackTop.addActionListener(e -> frame.showPanel("TOP"));
        //パネルに部品の追加
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(BackTop, BorderLayout.SOUTH);

    }
}
