// パッケージがあるならここに package 文を記載

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class GroupCreatePanel extends JPanel {

    private GroupDAO groupDAO = new GroupDAO();

    private Map<String, java.util.List<BudgetRecord>> groupData = new HashMap<>();
    private DefaultListModel<String> groupListModel = new DefaultListModel<>();
    private JList<String> groupList = new JList<>(groupListModel);

    private DefaultTableModel tableModel;
    private JTable recordTable;

    private JTextField amountField, memoField;
    private JButton addRecordBtn, createGroupBtn;

    private JComboBox<String> typeCombo;
    private JComboBox<String> categoryCombo;

    private String currentGroup = null;

    private MainFrame mainFrame;

    public GroupCreatePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        Font titleFont = new Font("Dialog", Font.BOLD, 22);
        Font normalFont = new Font("Dialog", Font.PLAIN, 14);
        Color primaryColor = new Color(40, 116, 166);
        Color secondaryColor = new Color(238, 238, 238);
        Color accentColor = new Color(255, 183, 77);

        // ここで左右の隙間を0に変更（上下は10のまま）
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(secondaryColor);

        setupLeftPanel(primaryColor, normalFont, accentColor);
        setupCenterPanel(primaryColor, secondaryColor, normalFont);
        updateGroupListDisplay();

        if (!groupListModel.isEmpty()) {
            groupList.setSelectedIndex(0);
        } else {
            setInputEnabled(false);
        }
    }

    private void setupLeftPanel(Color primaryColor, Font normalFont, Color accentColor) {
        JPanel leftPanel = new JPanel(new BorderLayout());
        // 幅を230から180に縮小
        leftPanel.setPreferredSize(new Dimension(180, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 2, primaryColor),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel groupTitle = new JLabel("グループ一覧");
        groupTitle.setFont(new Font("Dialog", Font.BOLD, 22));
        groupTitle.setForeground(primaryColor);
        groupTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        leftPanel.add(groupTitle, BorderLayout.NORTH);

        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.setFont(normalFont);
        groupList.setBackground(new Color(245, 248, 250));
        groupList.setForeground(primaryColor);
        groupList.setFixedCellHeight(30);
        groupList.setBorder(BorderFactory.createLineBorder(primaryColor));
        groupList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = groupList.getSelectedValue();
                currentGroup = selected;
                loadGroupRecords(selected);
            }
        });

        JScrollPane groupScroll = new JScrollPane(groupList);
        groupScroll.setBorder(BorderFactory.createEmptyBorder());
        leftPanel.add(groupScroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        createGroupBtn = new JButton("＋ 新規グループ作成");
        createGroupBtn.setFont(normalFont);
        createGroupBtn.setBackground(accentColor);
        createGroupBtn.setForeground(Color.WHITE);
        createGroupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createGroupBtn.addActionListener(e -> createGroup());
        btnPanel.add(createGroupBtn, gbc);

        leftPanel.add(btnPanel, BorderLayout.SOUTH);
        add(leftPanel, BorderLayout.WEST);
    }

    private void setupCenterPanel(Color primaryColor, Color secondaryColor, Font normalFont) {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(secondaryColor);

        JLabel centerTitle = new JLabel("家計簿記録");
        centerTitle.setFont(new Font("Dialog", Font.BOLD, 22));
        centerTitle.setForeground(primaryColor);
        centerTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        centerPanel.add(centerTitle, BorderLayout.NORTH);

        String[] columns = {"", "ユーザー名", "タイプ / カテゴリー", "金額", "日付", "メモ"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
            @Override public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        recordTable = new JTable(tableModel);
        recordTable.setFont(normalFont);
        recordTable.setRowHeight(28);
        recordTable.getTableHeader().setFont(new Font("", Font.BOLD, 16));
        recordTable.getTableHeader().setBackground(primaryColor);
        recordTable.getTableHeader().setForeground(Color.WHITE);

        TableColumn checkCol = recordTable.getColumnModel().getColumn(0);
        checkCol.setPreferredWidth(30);
        checkCol.setMaxWidth(30);
        checkCol.setMinWidth(30);

        JScrollPane tableScroll = new JScrollPane(recordTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(primaryColor, 2));
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        centerPanel.add(createInputPanel(primaryColor, normalFont), BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createInputPanel(Color primaryColor, Font normalFont) {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primaryColor, 2),
                new EmptyBorder(3, 12, 3, 12)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        typeCombo = new JComboBox<>(new String[]{"支出", "収入"});
        categoryCombo = new JComboBox<>(new String[]{"食費", "娯楽", "交通", "光熱費", "医療", "その他"});

        amountField = new JTextField(8);
        memoField = new JTextField(12);

        addRecordBtn = new JButton("＋ 追加");
        addRecordBtn.setFont(normalFont);
        addRecordBtn.setBackground(primaryColor);
        addRecordBtn.setForeground(Color.WHITE);
        addRecordBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addRecordBtn.addActionListener(e -> addRecord());

        JButton deleteBtn = new JButton("－ 削除");
        deleteBtn.setFont(normalFont);
        deleteBtn.setBackground(new Color(192, 57, 43));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> deleteSelectedRecords());

        c.gridx = 0; c.gridy = 0; inputPanel.add(new JLabel("タイプ:"), c);
        c.gridx = 1; inputPanel.add(typeCombo, c);
        c.gridx = 2; inputPanel.add(new JLabel("カテゴリー:"), c);
        c.gridx = 3; inputPanel.add(categoryCombo, c);

        c.gridx = 0; c.gridy = 1; inputPanel.add(new JLabel("金額:"), c);
        c.gridx = 1; inputPanel.add(amountField, c);
        c.gridx = 2; inputPanel.add(new JLabel("メモ:"), c);
        c.gridx = 3; c.gridwidth = 3; inputPanel.add(memoField, c);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 3; inputPanel.add(addRecordBtn, c);
        c.gridx = 3; inputPanel.add(deleteBtn, c);

        return inputPanel;
    }

    private void setInputEnabled(boolean enabled) {
        typeCombo.setEnabled(enabled);
        categoryCombo.setEnabled(enabled);
        amountField.setEnabled(enabled);
        memoField.setEnabled(enabled);
        addRecordBtn.setEnabled(enabled);
    }

    private void loadGroupRecords(String groupName) {
        tableModel.setRowCount(0);
        java.util.List<BudgetRecord> records = groupData.get(groupName);
        if (records != null) {
            for (BudgetRecord r : records) {
                tableModel.addRow(new Object[]{false, r.user, r.type + " / " + r.category, r.amount, r.date, r.memo});
            }
            setInputEnabled(true);
        } else {
            setInputEnabled(false);
        }
    }

    private void updateGroupListDisplay() {
        groupListModel.clear();
        for (String name : groupData.keySet()) {
            groupListModel.addElement(name);
        }
    }

    private void addRecord() {
        if (currentGroup == null) {
            JOptionPane.showMessageDialog(this, "グループを選択してください。");
            return;
        }

        String user = mainFrame.getCurrentUserId();
        String type = (String) typeCombo.getSelectedItem();
        String category = (String) categoryCombo.getSelectedItem();
        String amountStr = amountField.getText().trim();
        String memo = memoField.getText().trim();

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "金額は0以上の整数で入力してください。");
            return;
        }

        String today = LocalDate.now().toString();
        BudgetRecord r = new BudgetRecord(user, type, category, amount, today, memo);

        groupData.computeIfAbsent(currentGroup, k -> new ArrayList<>()).add(r);
        tableModel.addRow(new Object[]{false, user, type + " / " + category, amount, today, memo});

        amountField.setText("");
        memoField.setText("");
    }

    private void createGroup() {
        String groupName = JOptionPane.showInputDialog(this, "新しいグループ名を入力してください。");
        if (groupName == null || groupName.trim().isEmpty()) return;
        groupName = groupName.trim();

        String loginId = mainFrame.getCurrentUserId();

        if (!groupDAO.createGroup(groupName)) {
            JOptionPane.showMessageDialog(this, "グループ作成に失敗しました。");
            return;
        }

        groupDAO.joinGroup(loginId, groupName);
        groupData.put(groupName, new ArrayList<>());
        updateGroupListDisplay();
        groupList.setSelectedValue(groupName, true);
        JOptionPane.showMessageDialog(this, "グループ「" + groupName + "」を作成しました。");
    }

    public String getCurrentGroupName() {
        return currentGroup;
    }

    public void setCurrentGroupName(String groupName) {
        this.currentGroup = groupName;
    }
    private void deleteSelectedRecords() {
        int rowCount = tableModel.getRowCount();
        java.util.List<Integer> rowsToDelete = new ArrayList<>();

        for (int i = rowCount - 1; i >= 0; i--) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked != null && checked) {
                rowsToDelete.add(i);
            }
        }

        if (rowsToDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "削除する項目が選択されていません。");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "選択した項目を削除しますか？", "確認", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        for (int row : rowsToDelete) {
            tableModel.removeRow(row);
            java.util.List<BudgetRecord> records = groupData.get(currentGroup);
            if (records != null && row < records.size()) {
                records.remove(row);
            }
        }
    }

    public static class BudgetRecord {
        public String user, type, category, date, memo;
        public int amount;

        public BudgetRecord(String user, String type, String category, int amount, String date, String memo) {
            this.user = user;
            this.type = type;
            this.category = category;
            this.amount = amount;
            this.date = date;
            this.memo = memo;
        }
    }

    public interface MainFrame {
        String getCurrentUserId();
    }

    public static class GroupDAO {
        public boolean createGroup(String groupName) {
            // 仮の処理、DB接続など必要なら追加
            return true;
        }

        public boolean joinGroup(String userId, String groupName) {
            return true;
        }
    }
}
