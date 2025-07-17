import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

public class EditRecordPanel extends JPanel {

    private MainFrame mainFrame;
    private JTable table;
    private RecordTableModel tableModel;
    private JButton prevButton, nextButton, saveButton;
    private int currentPage = 0;
    private int pageSize = 10;
    private List<RecordDAO.Record> fullRecords;

    private Map<String, Integer> inCategoryMap = new HashMap<>();
    private Map<String, Integer> outCategoryMap = new HashMap<>();

    private final String[] types = {"収入", "支出"};

    // 左メニュー用
    private HashMap<String, JButton> menuButtons = new HashMap<>();
    private Color selectedColor = new Color(200, 220, 240);
    private Color normalColor = Color.WHITE;

    private JLabel userInfoLabel;

    public EditRecordPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // --- ヘッダー ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        headerPanel.setBackground(new Color(60, 141, 188));

        userInfoLabel = new JLabel("ログイン中: 未ログイン");
        userInfoLabel.setForeground(Color.WHITE);
        userInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton logoutButton = new JButton("ログアウト");
        logoutButton.setFocusable(false);
        logoutButton.addActionListener(e -> mainFrame.logout());

        headerPanel.add(userInfoLabel);
        headerPanel.add(logoutButton);

        add(headerPanel, BorderLayout.NORTH);

        // --- 左メニュー ---
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(245, 245, 245));
        menuPanel.setPreferredSize(new Dimension(220, 600));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        String[][] menuItems = {
            {"📋データ登録", "addRecord"},
            {"🗓データ抽出（カレンダー）", "calendar"},
            {"📁インポート / エクスポート", "importexport"},
            {"👥グループ管理", "group"},
            {"✏️編集", "editRecord"}
        };

        Font btnFont = new Font("SansSerif", Font.BOLD, 12);
        for (String[] item : menuItems) {
            String label = item[0];
            String name = item[1];

            JButton btn = new JButton(label);
            btn.setFont(btnFont);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btn.setBackground(name.equals("editRecord") ? selectedColor : normalColor);
            btn.setFocusPainted(false);

            btn.addActionListener(e -> {
             
                mainFrame.showPanel(name);
            });

            menuButtons.put(name, btn);
            menuPanel.add(btn);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        add(menuPanel, BorderLayout.WEST);

        // --- テーブル・ボタン部分 ---
        JPanel centerPanel = new JPanel(new BorderLayout());

        // カテゴリマップ初期化（DBからもらう想定、簡略化）
        inCategoryMap.put("給与", 11);
        inCategoryMap.put("臨時収入", 12);
        inCategoryMap.put("その他_収入", 13);

        outCategoryMap.put("家賃", 1);
        outCategoryMap.put("電気代", 2);
        outCategoryMap.put("水道代", 3);
        outCategoryMap.put("ガス代", 4);
        outCategoryMap.put("交通費", 5);
        outCategoryMap.put("食費", 6);
        outCategoryMap.put("通信費", 7);
        outCategoryMap.put("被服費", 8);
        outCategoryMap.put("医療費", 9);
        outCategoryMap.put("その他_支出", 10);

        tableModel = new RecordTableModel();
        table = new JTable(tableModel);

        // タイプ列にコンボボックスエディタ設定
        TableColumn typeColumn = table.getColumnModel().getColumn(1);
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeColumn.setCellEditor(new DefaultCellEditor(typeCombo));

        // カテゴリ列は動的切替のカスタムエディタ
        TableColumn categoryColumn = table.getColumnModel().getColumn(2);
        categoryColumn.setCellEditor(new CategoryCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        prevButton = new JButton("前へ");
        nextButton = new JButton("次へ");
        saveButton = new JButton("保存");

        prevButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateTableData();
            }
        });

        nextButton.addActionListener(e -> {
            if (fullRecords != null && (currentPage + 1) * pageSize < fullRecords.size()) {
                currentPage++;
                updateTableData();
            }
        });

        saveButton.addActionListener(e -> saveChanges());

        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(saveButton);

        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
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

    public void loadData() {
        String userId = mainFrame.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            System.out.println("ユーザーIDがありません。");
            fullRecords = new ArrayList<>();
            return;
        }
        fullRecords = RecordDAO.getRecordsByUserId(userId);
        currentPage = 0;
        System.out.println("取得したレコード数: " + fullRecords.size());
    }

    public void updateTableData() {
        if (fullRecords == null) {
            tableModel.setRecords(new ArrayList<>());
            return;
        }
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullRecords.size());
        if (start > end) {
            tableModel.setRecords(new ArrayList<>());
            return;
        }
        List<RecordDAO.Record> pageRecords = fullRecords.subList(start, end);
        tableModel.setRecords(pageRecords);
    }

    private void saveChanges() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        boolean allOk = true;

        for (RecordDAO.Record record : tableModel.getRecords()) {
            Map<String, Integer> catMap = "収入".equals(record.getType()) ? inCategoryMap : outCategoryMap;
            Integer catId = catMap.get(record.getCategoryName());
            if (catId == null) {
                JOptionPane.showMessageDialog(this,
                    "カテゴリ「" + record.getCategoryName() + "」は無効です。正しいカテゴリ名を選択してください。",
                    "エラー", JOptionPane.ERROR_MESSAGE);
                allOk = false;
                continue;
            }
            record.setCategoryId(catId);

            boolean updated = RecordDAO.updateRecord(record);
            if (!updated) {
                allOk = false;
            }
        }

        if (allOk) {
            JOptionPane.showMessageDialog(this, "保存が完了しました。");
        } else {
            JOptionPane.showMessageDialog(this, "一部の保存に失敗しました。", "エラー", JOptionPane.ERROR_MESSAGE);
        }
        loadData();
        updateTableData();
    }

    private class RecordTableModel extends AbstractTableModel {
        private final String[] columnNames = {"日付", "タイプ", "カテゴリ", "金額", "メモ"};
        private List<RecordDAO.Record> records = new ArrayList<>();

        public void setRecords(List<RecordDAO.Record> records) {
            this.records = records;
            fireTableDataChanged();
        }

        public List<RecordDAO.Record> getRecords() {
            return records;
        }

        @Override
        public int getRowCount() {
            return records.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            RecordDAO.Record record = records.get(row);
            switch (col) {
                case 0: return record.getDate().toString();
                case 1: return record.getType();
                case 2: return record.getCategoryName();
                case 3: return record.getAmount();
                case 4: return record.getMemo();
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 0; // 日付以外編集可
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            RecordDAO.Record record = records.get(row);
            switch (col) {
                case 1:
                    String newType = value.toString();
                    record.setType(newType);
                    // タイプ変更時にカテゴリを対応先の先頭に自動変更
                    if ("収入".equals(newType)) {
                        record.setCategoryName(inCategoryMap.keySet().iterator().next());
                    } else {
                        record.setCategoryName(outCategoryMap.keySet().iterator().next());
                    }
                    fireTableCellUpdated(row, 2);
                    break;
                case 2:
                    record.setCategoryName(value.toString());
                    break;
                case 3:
                    try {
                        int val = Integer.parseInt(value.toString());
                        record.setAmount(val);
                    } catch (NumberFormatException e) {
                        // 無効入力は無視
                    }
                    break;
                case 4:
                    record.setMemo(value.toString());
                    break;
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            if (col == 3) return Integer.class;
            return String.class;
        }
    }

    private class CategoryCellEditor extends AbstractCellEditor implements TableCellEditor {

        private JComboBox<String> comboBox;
        private int editingRow = -1;

        public CategoryCellEditor() {
            comboBox = new JComboBox<>();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            editingRow = row;

            String type = (String) table.getValueAt(row, 1);

            comboBox.removeAllItems();
            Map<String, Integer> catMap = "収入".equals(type) ? inCategoryMap : outCategoryMap;
            for (String catName : catMap.keySet()) {
                comboBox.addItem(catName);
            }

            comboBox.setSelectedItem(value);

            return comboBox;
        }

        @Override
        public Object getCellEditorValue() {
            return comboBox.getSelectedItem();
        }
    }
}
