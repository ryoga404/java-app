import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

public class AddRecordPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField dateField, amountField, memoField;
    private JComboBox<String> categoryCombo, typeCombo;
    private JLabel userInfoLabel;
    private Map<String, Integer> categoryMap = new LinkedHashMap<>();

    private RecordDAO recordDAO = new RecordDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();

    public AddRecordPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // --- ヘッダー ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        headerPanel.setBackground(new Color(60, 141, 188));

        userInfoLabel = new JLabel("ログイン中: 未ログイン");
        userInfoLabel.setForeground(Color.WHITE);
        userInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton backToHome = new JButton("ホームへ");
        backToHome.setFocusPainted(false);
        backToHome.setBackground(Color.WHITE);
        backToHome.setForeground(Color.BLACK);
        backToHome.setFont(new Font("SansSerif", Font.BOLD, 12));
        backToHome.addActionListener(e -> mainFrame.showPanel("home"));

        headerPanel.add(userInfoLabel);
        headerPanel.add(backToHome);
        add(headerPanel, BorderLayout.NORTH);

        // --- 入力フォーム ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("日付 (yyyy-MM-dd):"), gbc);
        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().toString());
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("タイプ:"), gbc);
        typeCombo = new JComboBox<>(new String[]{"収入", "支出"});
        gbc.gridx = 1;
        formPanel.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("カテゴリ:"), gbc);
        categoryCombo = new JComboBox<>();
        gbc.gridx = 1;
        formPanel.add(categoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("金額:"), gbc);
        amountField = new JTextField(10);
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("メモ:"), gbc);
        memoField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(memoField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JButton addButton = new JButton("登録");
        formPanel.add(addButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        typeCombo.addActionListener(e -> {
            String selectedType = (String) typeCombo.getSelectedItem();
            loadCategoriesByType(selectedType);
        });
        loadCategoriesByType((String) typeCombo.getSelectedItem());

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
        String userId = getUserIdFromSession(sessionId);
        userInfoLabel.setText("ログイン中: " + (userId == null ? "未ログイン" : userId));
    }

    private String getUserIdFromSession(String sessionId) {
        if (sessionId == null) return null;
        SessionDAO sessionDAO = new SessionDAO();
        return sessionDAO.getUserIdBySession(sessionId);
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
            if (date.isBefore(today.minusDays(30)) || date.isAfter(today.plusDays(30))) {
                throw new IllegalArgumentException("日付は過去30日～未来30日の範囲で入力してください。");
            }
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
        typeCombo.setSelectedIndex(0);
        if (categoryCombo.getItemCount() > 0) categoryCombo.setSelectedIndex(0);
        amountField.setText("");
        memoField.setText("");
    }
}
