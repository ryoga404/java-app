import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AddRecordPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField dateField;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> typeCombo;
    private JTextField amountField;
    private JTextField memoField;

    private Map<String, Integer> categoryMap = new LinkedHashMap<>();
    private RecordDAO recordDAO = new RecordDAO();

    public AddRecordPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 日付
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("日付 (yyyy-MM-dd):"), gbc);
        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().toString());
        gbc.gridx = 1;
        add(dateField, gbc);

        // カテゴリ
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("カテゴリ:"), gbc);
        categoryCombo = new JComboBox<>();
        gbc.gridx = 1;
        add(categoryCombo, gbc);

        // タイプ
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("タイプ:"), gbc);
        typeCombo = new JComboBox<>(new String[]{"In", "Out"});
        gbc.gridx = 1;
        add(typeCombo, gbc);

        // 金額
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("金額:"), gbc);
        amountField = new JTextField(10);
        gbc.gridx = 1;
        add(amountField, gbc);

        // メモ
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("メモ:"), gbc);
        memoField = new JTextField(20);
        gbc.gridx = 1;
        add(memoField, gbc);

        // 登録ボタン
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JButton addButton = new JButton("登録");
        add(addButton, gbc);

        addButton.addActionListener(e -> {
            try {
                addRecord();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "入力エラー: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
            }
        });

        loadCategoriesFromDB();
    }

    private void loadCategoriesFromDB() {
        categoryCombo.removeAllItems();
        categoryMap.clear();

        String sql = "SELECT categoryId, categoryName FROM Category ORDER BY categoryName";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("categoryId");
                String name = rs.getString("categoryName");
                categoryCombo.addItem(name);
                categoryMap.put(name, id);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "カテゴリの読み込みに失敗しました。\n" + e.getMessage(), "DBエラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRecord() throws Exception {
        String dateStr = dateField.getText().trim();
        String categoryName = (String) categoryCombo.getSelectedItem();
        String type = (String) typeCombo.getSelectedItem();
        String amountStr = amountField.getText().trim();
        String memo = memoField.getText().trim();

        // 入力チェック
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("日付の形式が不正です。yyyy-MM-dd形式で入力してください。");
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("金額は正の整数で入力してください。");
        }

        if (categoryName == null || !categoryMap.containsKey(categoryName)) {
            throw new IllegalArgumentException("カテゴリが選択されていないか無効です。");
        }
        int categoryId = categoryMap.get(categoryName);

        // セッションID取得
        String sessionId = mainFrame.getSessionId();

        // 登録実行
        boolean success = recordDAO.addRecord(
                sessionId, Date.valueOf(date), categoryId, type, amount, memo);

        if (success) {
            JOptionPane.showMessageDialog(this, "登録成功！");
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "登録に失敗しました。", "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        dateField.setText(LocalDate.now().toString());
        if (categoryCombo.getItemCount() > 0) categoryCombo.setSelectedIndex(0);
        typeCombo.setSelectedIndex(0);
        amountField.setText("");
        memoField.setText("");
    }
}
