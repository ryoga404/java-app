import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class CategorySelector extends JFrame {
	private JComboBox<String> categoryComboBox;
	private JLabel idLabel;
	private Map<String, Integer> categoryMap = new HashMap<>();
	
	public CategorySelector() {
		setTitle("カテゴリ選択");
		setSize(700, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new FlowLayout());
		
		categoryComboBox = new JComboBox<>();
		JButton button = new JButton("実行");
		idLabel = new JLabel("categoryId:");
		
		add(categoryComboBox);
		add(button);
		add(idLabel);
		
		loadCategories();
		
		button.addActionListener(e -> {
			String selected = (String) categoryComboBox.getSelectedItem();
			Integer id = categoryMap.get(selected);
			idLabel.setText("categoryId: " + (id != null ? id: "不明"));
		});
	}
	
	private void loadCategories() {
    String sql = "SELECT categoryId, categoryName FROM Category";
    try (Connection conn = DBUtil.getConnection()) {
        System.out.println("✅ DB接続成功");

        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        System.out.println("✅ SQL実行成功");

        while (rs.next()) {
            int id = rs.getInt("categoryId");
            String name = rs.getString("categoryName");
            System.out.println("取得: " + id + " - " + name); // ← 追加
            categoryComboBox.addItem(name);
            categoryMap.put(name, id);
        }
        System.out.println("✅ カテゴリ読み込み完了");

    } catch (SQLException e) {
        System.out.println("❌ SQLエラー発生");
        e.printStackTrace();
    }
}


	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new CategorySelector().setVisible(true));
	}
}