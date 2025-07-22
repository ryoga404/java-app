import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
    private final int pageSize = 10;
    private List<RecordDAO.Record> fullRecords;

    private final Map<String, Integer> inCategoryMap = new HashMap<>();
    private final Map<String, Integer> outCategoryMap = new HashMap<>();

    private final String[] types = {"収入", "支出"};

    public EditRecordPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // --- カテゴリ初期化 ---
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

        // --- テーブル構築 ---
        tableModel = new RecordTableModel();
        table = new JTable(tableModel);

        // タイプ列にコンボボックス
        TableColumn typeColumn = table.getColumnModel().getColumn(1);
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeColumn.setCellEditor(new DefaultCellEditor(typeCombo));

        // カテゴリ列：タイプに応じたエディタ
        TableColumn categoryColumn = table.getColumnModel().getColumn(2);
        categoryColumn.setCellEditor(new CategoryCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- 下部ボタン ---
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
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * HomePanel などから呼ばれることを想定したユーザー情報更新メソッド
     */
    public void refreshUserInfo() {
        String userId = mainFrame.getCurrentUserId();
        // 必要に応じてUI要素を更新してください
        System.out.println("EditRecordPanel.refreshUserInfo() called. Current user: " + userId);
        // 例: ラベル表示の更新などがあればここで行う
    }

    public void loadData() {
        String userId = mainFrame.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ユーザーIDが見つかりません。", "エラー", JOptionPane.ERROR_MESSAGE);
            fullRecords = new ArrayList<>();
            return;
        }
        fullRecords = RecordDAO.getRecordsByUserId(userId);
        currentPage = 0;
        updateTableData();
    }

    public void updateTableData() {
        if (fullRecords == null || fullRecords.isEmpty()) {
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
                    "カテゴリ「" + record.getCategoryName() + "」は無効です。", "入力エラー", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "一部のレコードの保存に失敗しました。", "保存エラー", JOptionPane.ERROR_MESSAGE);
        }

        loadData();
    }

    // --- Table Model ---
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
            return switch (col) {
                case 0 -> record.getDate().toString();
                case 1 -> record.getType();
                case 2 -> record.getCategoryName();
                case 3 -> record.getAmount();
                case 4 -> record.getMemo();
                default -> "";
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 0;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            RecordDAO.Record record = records.get(row);
            switch (col) {
                case 1 -> {
                    String newType = value.toString();
                    record.setType(newType);
                    record.setCategoryName(
                        "収入".equals(newType) ?
                            inCategoryMap.keySet().iterator().next() :
                            outCategoryMap.keySet().iterator().next());
                    fireTableCellUpdated(row, 2);
                }
                case 2 -> record.setCategoryName(value.toString());
                case 3 -> {
                    try {
                        int amount = Integer.parseInt(value.toString());
                        record.setAmount(amount);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "金額は整数で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
                    }
                }
                case 4 -> record.setMemo(value.toString());
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == 3 ? Integer.class : String.class;
        }
    }

    // --- カテゴリエディタ ---
    private class CategoryCellEditor extends AbstractCellEditor implements TableCellEditor {

        private final JComboBox<String> comboBox = new JComboBox<>();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            comboBox.removeAllItems();
            String type = (String) table.getValueAt(row, 1);
            Map<String, Integer> catMap = "収入".equals(type) ? inCategoryMap : outCategoryMap;
            for (String cat : catMap.keySet()) {
                comboBox.addItem(cat);
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
