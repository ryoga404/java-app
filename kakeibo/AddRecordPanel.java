import java.awt.*;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import java.sql.SQLException;


public class AddRecordPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField dateField;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> typeCombo;
    private JTextField amountField;
    private JTextField memoField;

    private JLabel userInfoLabel;
    private Map<String, Integer> categoryMap = new LinkedHashMap<>();

    private RecordDAO recordDAO = new RecordDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();

    public AddRecordPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // ヘッダー
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoLabel = new JLabel();
        JButton logoutButton = new JButton("ログアウト");
        JButton backToHome = new JButton("ホームへ");

        logoutButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.logout();
            }
        });

        backToHome.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel("home");
            }
        });

        headerPanel.add(userInfoLabel);
        headerPanel.add(logoutButton);
        headerPanel.add(backToHome);
        add(headerPanel, BorderLayout.NORTH);

        // 入力フォーム
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 日付
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("日付 (yyyy-MM-dd):"), gbc);
        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().toString());
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);

        // タイプ（収入 / 支出）
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("タイプ:"), gbc);
        typeCombo = new JComboBox<>(new String[]{"収入", "支出"});
        gbc.gridx = 1;
        formPanel.add(typeCombo, gbc);

        // カテゴリ
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("カテゴリ:"), gbc);
        categoryCombo = new JComboBox<>();
        gbc.gridx = 1;
        formPanel.add(categoryCombo, gbc);

        // 金額
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("金額:"), gbc);
        amountField = new JTextField(10);
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);

        // メモ
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("メモ:"), gbc);
        memoField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(memoField, gbc);

        // 登録ボタン
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JButton addButton = new JButton("登録");
        formPanel.add(addButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        // タイプ選択変更時にカテゴリ更新
        typeCombo.addActionListener(e -> {
            String selectedType = (String) typeCombo.getSelectedItem();
            loadCategoriesByType(selectedType);
        });

        // 初期カテゴリロード
        loadCategoriesByType((String) typeCombo.getSelectedItem());

        // 登録処理
        addButton.addActionListener(e -> {
            try {
                addRecord();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "入力エラー: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void refreshUserInfo() {
        String sessionId = mainFrame.getSessionId();
        String userName = getUserNameFromSession(sessionId);
        userInfoLabel.setText("ログイン中: " + userName);
    }

    private String getUserNameFromSession(String sessionId) {
        if (sessionId == null) return "未ログイン";

        SessionDAO sessionDAO = new SessionDAO();
        String userId = sessionDAO.getUserIdBySession(sessionId);
        if (userId == null) return "未ログイン";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT userName FROM User WHERE userId = ?")) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("userName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "未ログイン";
    }

    private void loadCategoriesByType(String type) {
        categoryCombo.removeAllItems();
        categoryMap.clear();

        Map<String, Integer> map = categoryDAO.getCategoriesByType(type);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            categoryCombo.addItem(entry.getKey());
            categoryMap.put(entry.getKey(), entry.getValue());
        }
    }

    private void addRecord() throws Exception {
        String dateStr = dateField.getText().trim();
        String categoryName = (String) categoryCombo.getSelectedItem();
        String type = (String) typeCombo.getSelectedItem();
        String amountStr = amountField.getText().trim();
        String memo = memoField.getText().trim();

        if (categoryName == null || !categoryMap.containsKey(categoryName)) {
            throw new IllegalArgumentException("カテゴリが選択されていないか無効です。");
        }

        int categoryId = categoryMap.get(categoryName);
        String sessionId = mainFrame.getSessionId();

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
            LocalDate today = LocalDate.now();
            LocalDate minDate = today.minusDays(30);
            LocalDate maxDate = today.plusDays(30);
            if (date.isBefore(minDate) || date.isAfter(maxDate)) {
                JOptionPane.showMessageDialog(this, "日付は過去30日～未来30日の範囲で入力してください。\n(許容範囲: " + minDate + " ～ " + maxDate + ")", "日付エラー", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "日付の形式が不正です。yyyy-MM-dd形式で入力してください。", "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("金額は正の整数で入力してください。");
        }

        boolean success = recordDAO.addRecord(sessionId, Date.valueOf(date), categoryId, type, amount, memo);

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
