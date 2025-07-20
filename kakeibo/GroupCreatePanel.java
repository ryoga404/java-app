import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class GroupCreatePanel extends JPanel {

    private GroupDAO groupDAO = new GroupDAO();

    private Map<String, java.util.List<BudgetRecord>> groupData = new HashMap<>();
    private DefaultListModel<String> groupListModel = new DefaultListModel<>();
    private JList<String> groupList = new JList<>(groupListModel);

    private DefaultTableModel tableModel;
    private JTable recordTable;

    private JTextField userField, amountField, memoField;
    private JButton addRecordBtn, createGroupBtn, joinGroupBtn;

    private JComboBox<String> categoryCombo;
    private JComboBox<String> itemCombo;

    private String currentGroup = null;

    private Map<String, java.util.List<String>> categoryItems = new HashMap<>();

    private MainFrame mainFrame;

    public GroupCreatePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        Font titleFont = new Font("Dialog", Font.BOLD, 22);
        Font normalFont = new Font("Dialog", Font.PLAIN, 14);
        Color primaryColor = new Color(40, 116, 166);
        Color secondaryColor = new Color(238, 238, 238);
        Color accentColor = new Color(255, 183, 77);

        categoryItems.put("食費", Arrays.asList("スーパー", "レストラン", "コンビニ"));
        categoryItems.put("娯楽", Arrays.asList("映画", "ゲーム", "カラオケ", "雑貨"));
        categoryItems.put("交通", Arrays.asList("電車", "バス", "タクシー"));
        categoryItems.put("光熱費", Arrays.asList("電気", "ガス", "水道"));
        categoryItems.put("医療", Arrays.asList("医療"));
        categoryItems.put("その他", Arrays.asList("その他"));

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(secondaryColor);

        JLabel loginLabel = new JLabel("ログインID: " + mainFrame.getCurrentUserId());
        loginLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        loginLabel.setForeground(primaryColor);
        loginLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(loginLabel, BorderLayout.NORTH);

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

        joinGroupBtn = new JButton("＋ グループに参加");
        joinGroupBtn.setFont(normalFont);
        joinGroupBtn.setBackground(new Color(100, 181, 246));
        joinGroupBtn.setForeground(Color.WHITE);
        joinGroupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        joinGroupBtn.addActionListener(e -> joinGroup());
        gbc.gridy = 1;
        btnPanel.add(joinGroupBtn, gbc);

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

        String[] columns = {"ユーザー", "カテゴリー", "項目", "金額", "日付", "メモ"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recordTable = new JTable(tableModel);
        recordTable.setFont(normalFont);
        recordTable.setRowHeight(28);
        recordTable.getTableHeader().setFont(new Font("", Font.BOLD, 16));
        recordTable.getTableHeader().setBackground(primaryColor);
        recordTable.getTableHeader().setForeground(Color.WHITE);
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
                new EmptyBorder(12, 12, 12, 12)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        userField = new JTextField(8);
        userField.setText(mainFrame.getCurrentUserId());
        userField.setEnabled(false);

        categoryCombo = new JComboBox<>();
        for (String cat : categoryItems.keySet()) categoryCombo.addItem(cat);

        itemCombo = new JComboBox<>();
        categoryCombo.addActionListener(e -> {
            itemCombo.removeAllItems();
            String selected = (String) categoryCombo.getSelectedItem();
            for (String item : categoryItems.get(selected)) itemCombo.addItem(item);
        });
        categoryCombo.setSelectedIndex(0);

        amountField = new JTextField(8);
        memoField = new JTextField(12);

        addRecordBtn = new JButton("＋ 追加");
        addRecordBtn.setFont(normalFont);
        addRecordBtn.setBackground(primaryColor);
        addRecordBtn.setForeground(Color.WHITE);
        addRecordBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addRecordBtn.addActionListener(e -> addRecord());

        addInputRow(inputPanel, c, 0, "ユーザー:", userField, "カテゴリー:", categoryCombo);
        addInputRow(inputPanel, c, 1, "項目:", itemCombo, "金額:", amountField);
        addInputRow(inputPanel, c, 2, "メモ:", memoField, null, null);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 4;
        c.anchor = GridBagConstraints.CENTER;
        inputPanel.add(addRecordBtn, c);

        return inputPanel;
    }

    private void addInputRow(JPanel panel, GridBagConstraints c, int y, String label1, JComponent field1, String label2, JComponent field2) {
        c.gridx = 0; c.gridy = y; panel.add(new JLabel(label1), c);
        c.gridx = 1; panel.add(field1, c);
        if (label2 != null && field2 != null) {
            c.gridx = 2; panel.add(new JLabel(label2), c);
            c.gridx = 3; panel.add(field2, c);
        }
    }

    private void setInputEnabled(boolean enabled) {
        categoryCombo.setEnabled(enabled);
        itemCombo.setEnabled(enabled);
        amountField.setEnabled(enabled);
        memoField.setEnabled(enabled);
        addRecordBtn.setEnabled(enabled);
    }

    private void loadGroupRecords(String groupName) {
        tableModel.setRowCount(0);
        java.util.List<BudgetRecord> records = groupData.get(groupName);
        if (records != null) {
            for (BudgetRecord r : records) {
                tableModel.addRow(new Object[]{r.user, r.category, r.item, r.amount, r.date, r.memo});
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

        String user = userField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        String item = (String) itemCombo.getSelectedItem();
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

        BudgetRecord r = new BudgetRecord(user, category, item, amount, Utils.getTodayDate(), memo);
        groupData.computeIfAbsent(currentGroup, k -> new ArrayList<>()).add(r);
        tableModel.addRow(new Object[]{user, category, item, amount, r.date, memo});
        amountField.setText("");
        memoField.setText("");
    }

    private void createGroup() {
        String groupName = JOptionPane.showInputDialog(this, "新しいグループ名を入力してください。");
        if (groupName == null || groupName.trim().isEmpty()) return;
        groupName = groupName.trim();

        // 現在ログイン中のユーザーIDを mainFrame から取得
        String loginId = mainFrame.getCurrentUserId();

        if (!groupDAO.createGroup(groupName)) {
            JOptionPane.showMessageDialog(this, "グループ作成に失敗しました。");
            return;
        }

        groupData.put(groupName, new ArrayList<>());
        updateGroupListDisplay();
        groupList.setSelectedValue(groupName, true);
        JOptionPane.showMessageDialog(this, "グループ「" + groupName + "」を作成しました。");

        // 必要に応じて、グループに作成者を参加させる処理もここに書けます。
        groupDAO.joinGroup(loginId, groupName);
    }

    private void joinGroup() {
        String groupName = JOptionPane.showInputDialog(this, "参加するグループ名を入力してください。");
        if (groupName == null || groupName.trim().isEmpty()) return;
        groupName = groupName.trim();

        // 現在ログイン中のユーザーIDを mainFrame から取得
        String loginId = mainFrame.getCurrentUserId();

        if (!groupDAO.joinGroup(loginId, groupName)) {
            JOptionPane.showMessageDialog(this, "グループ参加に失敗しました。");
            return;
        }

        groupData.putIfAbsent(groupName, new ArrayList<>());
        updateGroupListDisplay();
        groupList.setSelectedValue(groupName, true);
        JOptionPane.showMessageDialog(this, "グループ「" + groupName + "」に参加しました。");
    }

    private static class BudgetRecord {
        String user, category, item, date, memo;
        int amount;

        BudgetRecord(String u, String c, String i, int a, String d, String m) {
            user = u;
            category = c;
            item = i;
            amount = a;
            date = d;
            memo = m;
        }
    }

    private static class Utils {
        public static String getTodayDate() {
            return java.time.LocalDate.now().toString().replace("-", "/");
        }
    }
}
