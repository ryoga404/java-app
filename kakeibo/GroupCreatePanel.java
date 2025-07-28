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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

    private Map<String, List<BudgetRecord>> groupData = new HashMap<>();
    private DefaultListModel<String> groupListModel = new DefaultListModel<>();
    private JList<String> groupList = new JList<>(groupListModel);

    private DefaultTableModel tableModel;
    private JTable recordTable;

    private JTextField amountField, memoField;
    private JButton addRecordBtn, createGroupBtn;

    private JComboBox<String> typeCombo;       // 支出・収入
    private JComboBox<String> categoryCombo;   // カテゴリー
    private JComboBox<String> memberCombo;     // メンバー選択用コンボボックス

    private String currentGroup = null;

    private MainFrame mainFrame;

    public GroupCreatePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        Font titleFont = new Font("Dialog", Font.BOLD, 22);
        Font normalFont = new Font("Dialog", Font.PLAIN, 14);
        Color primaryColor = new Color(40, 116, 166);
        Color secondaryColor = new Color(238, 238, 238);
        Color accentColor = new Color(255, 183, 77);

        setLayout(new BorderLayout(10, 10));
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
        leftPanel.setPreferredSize(new Dimension(230, 0));
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
                updateMemberCombo();  // メンバーコンボ更新
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

        // チェックボックス列追加、ユーザー名列追加
        String[] columns = {"", "ユーザー名", "タイプ / カテゴリー", "金額", "日付", "メモ"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;  // チェックボックス列はBoolean型
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // チェックボックス列は編集可、他は不可
                return column == 0;
            }
        };

        recordTable = new JTable(tableModel);
        recordTable.setFont(normalFont);
        recordTable.setRowHeight(28);
        recordTable.getTableHeader().setFont(new Font("", Font.BOLD, 16));
        recordTable.getTableHeader().setBackground(primaryColor);
        recordTable.getTableHeader().setForeground(Color.WHITE);

        // チェックボックス列幅を小さく調整
        TableColumn checkCol = recordTable.getColumnModel().getColumn(0);
        checkCol.setPreferredWidth(30);
        checkCol.setMaxWidth(30);
        checkCol.setMinWidth(30);

        JScrollPane tableScroll = new JScrollPane(recordTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(primaryColor, 2));
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        // メンバー選択コンボボックスの作成と配置
        memberCombo = new JComboBox<>();
        memberCombo.addItem("すべてのメンバー");
        memberCombo.setFont(normalFont);
        memberCombo.addActionListener(e -> filterByMember());

        JPanel memberPanel = new JPanel(new BorderLayout());
        memberPanel.setBackground(secondaryColor);
        memberPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        memberPanel.add(new JLabel("メンバー選択:"), BorderLayout.WEST);
        memberPanel.add(memberCombo, BorderLayout.CENTER);

        // 入力パネルは一番下に置くため別に配置
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(secondaryColor);
        southPanel.add(memberPanel, BorderLayout.NORTH);
        southPanel.add(createInputPanel(primaryColor, normalFont), BorderLayout.SOUTH);

        centerPanel.add(southPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createInputPanel(Color primaryColor, Font normalFont) {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primaryColor, 2),
                new EmptyBorder(12, 12, 12, 12)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        // タイププルダウン（支出・収入）
        typeCombo = new JComboBox<>();
        typeCombo.addItem("支出");
        typeCombo.addItem("収入");

        // カテゴリープルダウン（食費など）
        categoryCombo = new JComboBox<>();
        categoryCombo.addItem("食費");
        categoryCombo.addItem("娯楽");
        categoryCombo.addItem("交通");
        categoryCombo.addItem("光熱費");
        categoryCombo.addItem("医療");
        categoryCombo.addItem("その他");

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
        deleteBtn.setBackground(new Color(192, 57, 43)); // 赤系
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> deleteSelectedRecords());

        // 1行目：タイプ & カテゴリー
        c.gridx = 0;
        c.gridy = 0;
        inputPanel.add(new JLabel("タイプ:"), c);
        c.gridx = 1;
        inputPanel.add(typeCombo, c);
        c.gridx = 2;
        inputPanel.add(new JLabel("カテゴリー:"), c);
        c.gridx = 3;
        inputPanel.add(categoryCombo, c);

        // 2行目：金額 & メモ
        c.gridx = 0;
        c.gridy = 1;
        inputPanel.add(new JLabel("金額:"), c);
        c.gridx = 1;
        inputPanel.add(amountField, c);
        c.gridx = 2;
        inputPanel.add(new JLabel("メモ:"), c);
        c.gridx = 3;
        c.gridwidth = 3;
        inputPanel.add(memoField, c);

        // 3行目：ボタン配置（追加ボタンと削除ボタン）
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.CENTER;
        inputPanel.add(addRecordBtn, c);

        c.gridx = 3;
        c.gridwidth = 3;
        inputPanel.add(deleteBtn, c);

        return inputPanel;
    }

    private void setInputEnabled(boolean enabled) {
        typeCombo.setEnabled(enabled);
        categoryCombo.setEnabled(enabled);
        amountField.setEnabled(enabled);
        memoField.setEnabled(enabled);
        addRecordBtn.setEnabled(enabled);
        memberCombo.setEnabled(enabled);
    }

    private void loadGroupRecords(String groupName) {
        tableModel.setRowCount(0);
        List<BudgetRecord> records = groupData.get(groupName);
        if (records != null) {
            for (BudgetRecord r : records) {
                String typeCategory = r.type + " / " + r.category;
                tableModel.addRow(new Object[]{false, r.user, typeCategory, r.amount, r.date, r.memo});
            }
            setInputEnabled(true);
        } else {
            setInputEnabled(false);
        }
        updateMemberCombo();
        memberCombo.setSelectedIndex(0);  // 「すべてのメンバー」選択状態に
    }

    private void updateGroupListDisplay() {
        groupListModel.clear();
        for (String name : groupData.keySet()) {
            groupListModel.addElement(name);
        }
    }

    private void updateMemberCombo() {
        memberCombo.removeAllItems();
        memberCombo.addItem("すべてのメンバー");
        if (currentGroup == null) return;
        List<BudgetRecord> records = groupData.get(currentGroup);
        if (records == null) return;

        // ユニークなユーザー名を収集して昇順でソート
        Set<String> users = new TreeSet<>();
        for (BudgetRecord r : records) {
            users.add(r.user);
        }
        for (String user : users) {
            memberCombo.addItem(user);
        }
    }

    private void filterByMember() {
        if (currentGroup == null) return;

        String selectedMember = (String) memberCombo.getSelectedItem();
        if (selectedMember == null) return;

        List<BudgetRecord> allRecords = groupData.get(currentGroup);
        if (allRecords == null) return;

        tableModel.setRowCount(0);

        for (BudgetRecord r : allRecords) {
            if ("すべてのメンバー".equals(selectedMember) || r.user.equals(selectedMember)) {
                String typeCategory = r.type + " / " + r.category;
                tableModel.addRow(new Object[]{false, r.user, typeCategory, r.amount, r.date, r.memo});
            }
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

        if (!groupDAO.saveBudgetRecord(currentGroup, r)) {
            JOptionPane.showMessageDialog(this, "データベースへの保存に失敗しました。");
            return;
        }

        groupData.computeIfAbsent(currentGroup, k -> new ArrayList<>()).add(r);

        // 追加後は現在のメンバー選択に合わせて再表示
        filterByMember();

        amountField.setText("");
        memoField.setText("");
        updateMemberCombo();  // 新規ユーザー追加時のため更新
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

        groupData.put(groupName, new ArrayList<>());
        updateGroupListDisplay();
        groupList.setSelectedValue(groupName, true);
        JOptionPane.showMessageDialog(this, "グループ「" + groupName + "」を作成しました。");

        groupDAO.joinGroup(loginId, groupName);
    }

    private void deleteSelectedRecords() {
        int rowCount = tableModel.getRowCount();
        List<Integer> rowsToDelete = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked != null && checked) {
                rowsToDelete.add(i);
            }
        }
        if (rowsToDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "削除する行をチェックしてください。");
            return;
        }

        List<BudgetRecord> records = groupData.get(currentGroup);
        if (records == null) return;

        rowsToDelete.sort((a, b) -> b - a);
        for (int rowIndex : rowsToDelete) {
            String user = (String) tableModel.getValueAt(rowIndex, 1);
            String typeCategory = (String) tableModel.getValueAt(rowIndex, 2);
            int slashPos = typeCategory.indexOf(" / ");
            String type = typeCategory.substring(0, slashPos);
            String category = typeCategory.substring(slashPos + 3);
            String amountStr = (String) tableModel.getValueAt(rowIndex, 3);
            int amount = Integer.parseInt(amountStr);
            String date = (String) tableModel.getValueAt(rowIndex, 4);
            String memo = (String) tableModel.getValueAt(rowIndex, 5);

            BudgetRecord r = new BudgetRecord(user, type, category, amount, date, memo);

            if (!groupDAO.deleteBudgetRecord(currentGroup, r)) {
                JOptionPane.showMessageDialog(this, "データベースからの削除に失敗しました。");
                continue;
            }

            records.removeIf(record -> record.user.equals(user) && record.type.equals(type) && record.category.equals(category)
                    && record.amount == amount && record.date.equals(date) && record.memo.equals(memo));
            tableModel.removeRow(rowIndex);
        }
        updateMemberCombo();
        filterByMember();
    }
    // BudgetRecordクラス
    private static class BudgetRecord {
        String user;
        String type;
        String category;
        int amount;
        String date;
        String memo;

        BudgetRecord(String user, String type, String category, int amount, String date, String memo) {
            this.user = user;
            this.type = type;
            this.category = category;
            this.amount = amount;
            this.date = date;
            this.memo = memo;
        }
    }

    // ダミーDAO（本物のDB連携に置き換えてください）
    private static class GroupDAO {
        public boolean createGroup(String groupName) {
            // グループ作成処理（ダミー）
            return true;
        }

        public boolean deleteBudgetRecord(String currentGroup, GroupCreatePanel.BudgetRecord r) {
			// TODO 自動生成されたメソッド・スタブ
			return false;
		}

		public boolean saveBudgetRecord(String currentGroup, GroupCreatePanel.BudgetRecord r) {
			// TODO 自動生成されたメソッド・スタブ
			return false;
		}

		public boolean joinGroup(String loginId, String groupName) {
            // グループ参加処理（ダミー）
            return true;
        }
    }

}
