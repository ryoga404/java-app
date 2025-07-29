import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.toedter.calendar.JCalendar;

public class AddRecordPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField dateField, amountField, memoField;
    private JComboBox<String> categoryCombo, typeCombo;
    private Map<String, Integer> categoryMap = new LinkedHashMap<>();

    private RecordDAO recordDAO = new RecordDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();

    private JButton calendarButton;

    public AddRecordPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("日付 (yyyy-MM-dd):"), gbc);

        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().toString());

        calendarButton = new JButton("📅");
        calendarButton.setMargin(new Insets(2, 6, 2, 6));

        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.add(dateField, BorderLayout.CENTER);
        datePanel.add(calendarButton, BorderLayout.EAST);

        gbc.gridx = 1;
        formPanel.add(datePanel, gbc);

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

        calendarButton.addActionListener(e -> openCalendarDialog());
    }

    private void openCalendarDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog;

        if (owner instanceof java.awt.Frame) {
            dialog = new JDialog((java.awt.Frame) owner, "カレンダー選択", true);
        } else if (owner instanceof java.awt.Dialog) {
            dialog = new JDialog((java.awt.Dialog) owner, "カレンダー選択", true);
        } else {
            dialog = new JDialog((java.awt.Frame) null, "カレンダー選択", true);
        }

        JCalendar calendar = new JCalendar();

        try {
            LocalDate ld = LocalDate.parse(dateField.getText());
            calendar.setDate(java.sql.Date.valueOf(ld));
        } catch (Exception ignored) {}

        dialog.getContentPane().add(calendar);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        calendar.getDayChooser().addPropertyChangeListener("day", evt -> {
            java.util.Date selected = calendar.getDate();
            LocalDate ld = selected.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            dateField.setText(ld.toString());
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    public void refreshUserInfo() {
        // ユーザー情報更新処理があればここに
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
            if (date.isBefore(today.minusDays(3600)) || date.isAfter(today.plusDays(3600))) {
                throw new IllegalArgumentException("日付は過去未来１０年の範囲で入力してください。");
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

        boolean success = recordDAO.addRecord(sessionId, java.sql.Date.valueOf(date), categoryId, type, amount, memo);
        if (success) {
            AutoCloseDialog.showAutoCloseMessage(SwingUtilities.getWindowAncestor(this), "登録成功！", 500);
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

    // 自動で2秒後に閉じるダイアログクラス
    public static class AutoCloseDialog extends JDialog {

        public AutoCloseDialog(Window owner, String message, int autoCloseMillis) {
            super(owner, "通知", ModalityType.MODELESS);

            JLabel label = new JLabel(message, SwingConstants.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            add(label);

            setSize(250, 100);
            setLocationRelativeTo(owner);

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    // 閉じない
                }
            });

            setVisible(true);

            new Timer(autoCloseMillis, e -> dispose()) {{
                setRepeats(false);
                start();
            }};
        }

        public static void showAutoCloseMessage(Window owner, String message, int millis) {
            new AutoCloseDialog(owner, message, millis);
        }
    }
}
