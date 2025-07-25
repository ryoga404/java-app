import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class EditRecordPanel extends JPanel {

    private MainFrame mainFrame;
    private JTable table;
    private RecordTableModel tableModel;
    private JButton prevButton, nextButton, saveButton, deleteButton, filterButton;
    private int currentPage = 0;
    private final int pageSize = 10;
    private List<RecordDAO.Record> fullRecords;

    private final Map<String, Integer> inCategoryMap = new HashMap<>();
    private final Map<String, Integer> outCategoryMap = new HashMap<>();

    private final String[] types = {"収入", "支出"};

    // 追加：TableRowSorterでソート＆フィルター管理
    private TableRowSorter<RecordTableModel> sorter;

    // フィルター条件セット（タイプとカテゴリの表示中セット）
    private Set<String> visibleTypes = new HashSet<>();
    private Set<String> visibleCategories = new HashSet<>();

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

        // フィルター初期値セット（全部表示）
        visibleTypes.add("収入");
        visibleTypes.add("支出");
        visibleCategories.addAll(inCategoryMap.keySet());
        visibleCategories.addAll(outCategoryMap.keySet());

        // --- テーブル構築 ---
        tableModel = new RecordTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // TableRowSorterをセット（これでヘッダークリックでソート可能）
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // 列幅調整
        int[] columnWidths = {50, 90, 60, 100, 80, 200};
        for (int i = 0; i < columnWidths.length; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(columnWidths[i]);
        }

        // チェックボックス（選択列）を表示
        table.getColumnModel().getColumn(0).setCellRenderer(table.getDefaultRenderer(Boolean.class));

        // セル中央寄せ（チェックと日付）
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(table.getDefaultRenderer(Boolean.class));
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // タイプ列にコンボボックス
        TableColumn typeColumn = table.getColumnModel().getColumn(2);
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeColumn.setCellEditor(new DefaultCellEditor(typeCombo));

        // カテゴリ列にカスタムエディタ
        TableColumn categoryColumn = table.getColumnModel().getColumn(3);
        categoryColumn.setCellEditor(new CategoryCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- ボタン ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevButton = new JButton("前へ");
        nextButton = new JButton("次へ");
        saveButton = new JButton("更新");
        deleteButton = new JButton("削除");
        filterButton = new JButton("フィルター設定"); // 追加

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

        deleteButton.addActionListener(e -> {
            List<RecordDAO.Record> selected = tableModel.getSelectedRecords();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "削除するレコードを選択してください。");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    selected.size() + "件のレコードを削除しますか？", "確認", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean allDeleted = true;
            for (RecordDAO.Record record : selected) {
                boolean deleted = new RecordDAO().deleteRecord(record.getRecordId());
                if (!deleted) allDeleted = false;
            }

            if (allDeleted) {
                JOptionPane.showMessageDialog(this, "削除が完了しました。");
            } else {
                JOptionPane.showMessageDialog(this, "一部のレコードの削除に失敗しました。", "エラー", JOptionPane.ERROR_MESSAGE);
            }

            loadData();
        });

        // フィルター設定ボタン押下時の処理
        filterButton.addActionListener(e -> openFilterDialog());

        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(filterButton); // 追加
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshUserInfo() {
        String userId = mainFrame.getCurrentUserId();
        System.out.println("EditRecordPanel.refreshUserInfo() called. Current user: " + userId);
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

        // ページング後にフィルター適用
        applyFilter();
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

    // フィルター適用
    private void applyFilter() {
        sorter.setRowFilter(new RowFilter<RecordTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends RecordTableModel, ? extends Integer> entry) {
                RecordTableModel model = entry.getModel();
                int modelRow = entry.getIdentifier();
                if (modelRow < 0 || modelRow >= model.getRecords().size()) return false;
                RecordDAO.Record record = model.getRecords().get(modelRow);

                return visibleTypes.contains(record.getType()) &&
                       visibleCategories.contains(record.getCategoryName());
            }
        });
    }

    // フィルター設定ダイアログ
    private void openFilterDialog() {
        JDialog dialog = new JDialog(mainFrame, "フィルター設定", true);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));

        // タイプのチェックボックス
        JPanel typePanel = new JPanel();
        typePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("タイプ"));
        JCheckBox incomeCheck = new JCheckBox("収入", visibleTypes.contains("収入"));
        JCheckBox expenseCheck = new JCheckBox("支出", visibleTypes.contains("支出"));
        typePanel.add(incomeCheck);
        typePanel.add(expenseCheck);
        panel.add(typePanel);

        // カテゴリのチェックボックス（スクロール付き）
        JPanel categoryPanel = new JPanel();
        categoryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("カテゴリ"));
        categoryPanel.setLayout(new javax.swing.BoxLayout(categoryPanel, javax.swing.BoxLayout.Y_AXIS));

        // すべてのカテゴリ名をリスト化
        List<String> allCategories = new ArrayList<>();
        allCategories.addAll(inCategoryMap.keySet());
        allCategories.addAll(outCategoryMap.keySet());

        Map<String, JCheckBox> categoryCheckMap = new HashMap<>();
        for (String cat : allCategories) {
            JCheckBox cb = new JCheckBox(cat, visibleCategories.contains(cat));
            categoryCheckMap.put(cat, cb);
            categoryPanel.add(cb);
        }

        JScrollPane scrollPane = new JScrollPane(categoryPanel);
        scrollPane.setPreferredSize(new java.awt.Dimension(200, 300));
        panel.add(scrollPane);

        dialog.add(panel, BorderLayout.CENTER);

        // OK/キャンセルボタン
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("キャンセル");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(e -> {
            visibleTypes.clear();
            if (incomeCheck.isSelected()) visibleTypes.add("収入");
            if (expenseCheck.isSelected()) visibleTypes.add("支出");

            visibleCategories.clear();
            for (Map.Entry<String, JCheckBox> entry : categoryCheckMap.entrySet()) {
                if (entry.getValue().isSelected()) visibleCategories.add(entry.getKey());
            }

            applyFilter();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // --- Table Model ---
    private class RecordTableModel extends AbstractTableModel {
        private final String[] columnNames = {"選択", "日付", "タイプ", "カテゴリ", "金額", "メモ"};
        private final Class<?>[] columnClasses = {Boolean.class, String.class, String.class, String.class, Integer.class, String.class};
        private List<RecordDAO.Record> records = new ArrayList<>();
        private List<Boolean> selected = new ArrayList<>();

        public void setRecords(List<RecordDAO.Record> records) {
            this.records = records;
            this.selected = new ArrayList<>();
            for (int i = 0; i < records.size(); i++) selected.add(false);
            fireTableDataChanged();
        }

        public List<RecordDAO.Record> getRecords() {
            return records;
        }

        public List<RecordDAO.Record> getSelectedRecords() {
            List<RecordDAO.Record> result = new ArrayList<>();
            for (int i = 0; i < records.size(); i++) {
                if (selected.get(i)) result.add(records.get(i));
            }
            return result;
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
            RecordDAO.Record r = records.get(row);
            return switch (col) {
                case 0 -> selected.get(row);
                case 1 -> r.getDate().toString();
                case 2 -> r.getType();
                case 3 -> r.getCategoryName();
                case 4 -> r.getAmount();
                case 5 -> r.getMemo();
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            RecordDAO.Record r = records.get(row);
            switch (col) {
                case 0 -> selected.set(row, (Boolean) value);
                case 1 -> { // 日付編集対応
                    String dateStr = value.toString();
                    try {
                        java.sql.Date sqlDate = java.sql.Date.valueOf(dateStr); // yyyy-MM-dd形式のみ有効
                        r.setDate(sqlDate);

                        // 日付変更時に即DB更新
                        boolean updated = RecordDAO.updateRecordWithDate(r);
                        if (!updated) {
                            JOptionPane.showMessageDialog(null, "日付の保存に失敗しました。", "DBエラー", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(null, "日付は「yyyy-MM-dd」形式で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
                        fireTableCellUpdated(row, col);
                    }
                }
                case 2 -> {
                    String newType = value.toString();
                    r.setType(newType);
                    r.setCategoryName("収入".equals(newType) ?
                        inCategoryMap.keySet().iterator().next() :
                        outCategoryMap.keySet().iterator().next());
                    fireTableCellUpdated(row, 3);
                }
                case 3 -> r.setCategoryName(value.toString());
                case 4 -> {
                    try {
                        int amount = Integer.parseInt(value.toString());
                        r.setAmount(amount);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "金額は整数で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
                    }
                }
                case 5 -> r.setMemo(value.toString());
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            // 0列（選択列）以外は編集可能に（日付も含む）
            return true;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return columnClasses[col];
        }
    }

    // --- カテゴリエディタ ---
    private class CategoryCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JComboBox<String> comboBox = new JComboBox<>();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            comboBox.removeAllItems();
            String type = (String) table.getValueAt(row, 2); // "タイプ"列
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
