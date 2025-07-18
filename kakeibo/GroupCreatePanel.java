package kakeibo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class GroupCreatePanel extends JFrame {

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

    // 仮のログインID
    private String loginId = "User123";

    // カテゴリーごとの項目リストマップ
    private Map<String, java.util.List<String>> categoryItems = new HashMap<>();

    public GroupCreatePanel() {
        setTitle("✨ 家計簿グループ共有アプリ (Premium Edition) ✨");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640);
        setLocationRelativeTo(null);

        // カスタムフォントとテーマカラー
        Font titleFont = new Font("Dialog", Font.BOLD, 22);
        Font normalFont = new Font("Dialog", Font.PLAIN, 14);
        Color primaryColor = new Color(40, 116, 166);   // ネイビー系
        Color secondaryColor = new Color(238, 238, 238); // 薄グレー
        Color accentColor = new Color(255, 183, 77);     // オレンジ系

        // カテゴリーごとの項目を設定
        categoryItems.put("食費", java.util.Arrays.asList("スーパー", "レストラン", "コンビニ"));
        categoryItems.put("娯楽", java.util.Arrays.asList("映画", "ゲーム", "カラオケ", "雑貨"));
        categoryItems.put("交通", java.util.Arrays.asList("電車", "バス", "タクシー"));
        categoryItems.put("光熱費", java.util.Arrays.asList("電気", "ガス", "水道"));
        categoryItems.put("医療", java.util.Arrays.asList("医療"));
        categoryItems.put("その他", java.util.Arrays.asList("その他"));

        // 全体パネル
        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(new EmptyBorder(15,15,15,15));
        mainPanel.setBackground(secondaryColor);
        setContentPane(mainPanel);

        // --- 上部パネルにログインID表示 ---
        JLabel loginLabel = new JLabel("ログインID: " + loginId);
        loginLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        loginLabel.setForeground(primaryColor);
        loginLabel.setBorder(new EmptyBorder(0,0,10,0));
        mainPanel.add(loginLabel, BorderLayout.NORTH);

        // 左ペイン (グループ一覧 + 作成 + 参加)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(230, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,0,2, primaryColor),
                new EmptyBorder(10,10,10,10)));

        JLabel groupTitle = new JLabel("グループ一覧");
        groupTitle.setFont(titleFont);
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
                if (selected != null) {
                    currentGroup = selected;
                    loadGroupRecords(selected);
                }
            }
        });

        JScrollPane groupScroll = new JScrollPane(groupList);
        groupScroll.setBorder(BorderFactory.createEmptyBorder());
        leftPanel.add(groupScroll, BorderLayout.CENTER);

        // 作成＆参加ボタンパネル
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
        createGroupBtn.setFocusPainted(false);
        createGroupBtn.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        createGroupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createGroupBtn.addActionListener(e -> createGroup());

        joinGroupBtn = new JButton("＋ グループに参加");
        joinGroupBtn.setFont(normalFont);
        joinGroupBtn.setBackground(new Color(100, 181, 246));  // 水色系
        joinGroupBtn.setForeground(Color.WHITE);
        joinGroupBtn.setFocusPainted(false);
        joinGroupBtn.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        joinGroupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        joinGroupBtn.addActionListener(e -> joinGroup());

        gbc.gridx = 0;
        gbc.gridy = 0;
        btnPanel.add(createGroupBtn, gbc);
        gbc.gridy = 1;
        btnPanel.add(joinGroupBtn, gbc);

        leftPanel.add(btnPanel, BorderLayout.SOUTH);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // 中央ペイン (家計簿記録 + 入力フォーム)
        JPanel centerPanel = new JPanel(new BorderLayout(0,10));
        centerPanel.setBackground(secondaryColor);

        JLabel centerTitle = new JLabel("家計簿記録");
        centerTitle.setFont(titleFont);
        centerTitle.setForeground(primaryColor);
        centerTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        centerPanel.add(centerTitle, BorderLayout.NORTH);

        // テーブル設定
        String[] columns = {"ユーザー", "カテゴリー", "項目", "金額", "日付", "メモ"};
        tableModel = new DefaultTableModel(columns, 0) {
            // 編集不可に
            @Override
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
        recordTable.setSelectionBackground(accentColor);
        recordTable.setSelectionForeground(Color.WHITE);
        JScrollPane tableScroll = new JScrollPane(recordTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(primaryColor, 2));
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        // グループ未選択時のメッセージラベル
        JLabel noGroupLabel = new JLabel("※グループに参加していません。左の「グループに参加」ボタンから参加してください。");
        noGroupLabel.setFont(normalFont);
        noGroupLabel.setForeground(Color.RED);
        noGroupLabel.setHorizontalAlignment(JLabel.CENTER);
        noGroupLabel.setBorder(new EmptyBorder(12,0,12,0));
        centerPanel.add(noGroupLabel, BorderLayout.SOUTH);

        // 入力フォームパネル
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(secondaryColor);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primaryColor, 2),
                new EmptyBorder(12,12,12,12)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,8,6,8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("ユーザー:");
        userLabel.setFont(normalFont);
        userLabel.setForeground(primaryColor);
        c.gridx = 0; c.gridy = 0; inputPanel.add(userLabel, c);

        userField = new JTextField(8);
        c.gridx = 1; c.gridy = 0; inputPanel.add(userField, c);

        // カテゴリーラベルとコンボボックス追加
        JLabel categoryLabel = new JLabel("カテゴリー:");
        categoryLabel.setFont(normalFont);
        categoryLabel.setForeground(primaryColor);
        c.gridx = 2; c.gridy = 0; inputPanel.add(categoryLabel, c);

        categoryCombo = new JComboBox<>();
        for (String category : categoryItems.keySet()) {
            categoryCombo.addItem(category);
        }
        categoryCombo.setFont(normalFont);
        c.gridx = 3; c.gridy = 0; inputPanel.add(categoryCombo, c);

        // 項目ラベルとコンボボックス
        JLabel itemLabel = new JLabel("項目:");
        itemLabel.setFont(normalFont);
        itemLabel.setForeground(primaryColor);
        c.gridx = 0; c.gridy = 1; inputPanel.add(itemLabel, c);

        itemCombo = new JComboBox<>();
        itemCombo.setFont(normalFont);
        c.gridx = 1; c.gridy = 1; inputPanel.add(itemCombo, c);

        // カテゴリー選択時に項目コンボを更新
        categoryCombo.addActionListener(e -> {
            String selectedCategory = (String) categoryCombo.getSelectedItem();
            itemCombo.removeAllItems();
            if (selectedCategory != null) {
                for (String item : categoryItems.get(selectedCategory)) {
                    itemCombo.addItem(item);
                }
            }
        });
        // 最初のカテゴリーに合わせて項目セット
        categoryCombo.setSelectedIndex(0);

        JLabel amountLabel = new JLabel("金額:");
        amountLabel.setFont(normalFont);
        amountLabel.setForeground(primaryColor);
        c.gridx = 2; c.gridy = 1; inputPanel.add(amountLabel, c);

        amountField = new JTextField(8);
        c.gridx = 3; c.gridy = 1; inputPanel.add(amountField, c);

        JLabel memoLabel = new JLabel("メモ:");
        memoLabel.setFont(normalFont);
        memoLabel.setForeground(primaryColor);
        c.gridx = 0; c.gridy = 2; inputPanel.add(memoLabel, c);

        memoField = new JTextField(12);
        c.gridx = 1; c.gridy = 2;
        c.gridwidth = 3;
        inputPanel.add(memoField, c);
        c.gridwidth = 1;

        addRecordBtn = new JButton("＋ 追加");
        addRecordBtn.setFont(normalFont);
        addRecordBtn.setBackground(primaryColor);
        addRecordBtn.setForeground(Color.WHITE);
        addRecordBtn.setFocusPainted(false);
        addRecordBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        addRecordBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addRecordBtn.addActionListener(e -> addRecord());

        c.gridx = 0; c.gridy = 3; c.gridwidth = 4;
        c.anchor = GridBagConstraints.CENTER;
        inputPanel.add(addRecordBtn, c);
        c.gridwidth = 1;

        centerPanel.add(inputPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 初期状態：グループ未所属（グループリスト空）
        updateGroupListDisplay();

        // グループ未所属時は記録追加フォームは無効化
        setInputEnabled(false);

        setVisible(true);
    }

    private void updateGroupListDisplay() {
        // 仮: groupDataのキーがグループ名
        groupListModel.clear();
        for (String groupName : groupData.keySet()) {
            groupListModel.addElement(groupName);
        }
    }

    private void loadGroupRecords(String groupName) {
        // テーブルをクリア
        tableModel.setRowCount(0);

        java.util.List<BudgetRecord> records = groupData.get(groupName);
        if (records == null) return;

        for (BudgetRecord r : records) {
            Object[] row = {
                r.user, r.category, r.item, r.amount, r.date, r.memo
            };
            tableModel.addRow(row);
        }
        setInputEnabled(true);
    }

    private void setInputEnabled(boolean enabled) {
        userField.setEnabled(enabled);
        categoryCombo.setEnabled(enabled);
        itemCombo.setEnabled(enabled);
        amountField.setEnabled(enabled);
        memoField.setEnabled(enabled);
        addRecordBtn.setEnabled(enabled);
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

        if (user.isEmpty() || category == null || item == null || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ユーザー・カテゴリー・項目・金額は必須です。");
            return;
        }

        int amount = 0;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "金額は0以上の整数で入力してください。");
            return;
        }

        BudgetRecord newRecord = new BudgetRecord(user, category, item, amount, Utils.getTodayDate(), memo);

        groupData.get(currentGroup).add(newRecord);
        tableModel.addRow(new Object[] {user, category, item, amount, newRecord.date, memo});

        // 入力クリア
        amountField.setText("");
        memoField.setText("");
    }

    private void createGroup() {
        String groupName = JOptionPane.showInputDialog(this, "新しいグループ名を入力してください。", "グループ作成", JOptionPane.PLAIN_MESSAGE);
        if (groupName == null || groupName.trim().isEmpty()) {
            return;
        }
        groupName = groupName.trim();

        if (groupData.containsKey(groupName)) {
            JOptionPane.showMessageDialog(this, "同じ名前のグループが既に存在します。");
            return;
        }

        groupData.put(groupName, new ArrayList<>());
        updateGroupListDisplay();
        groupList.setSelectedValue(groupName, true);
        JOptionPane.showMessageDialog(this, "グループ「" + groupName + "」を作成しました。");
    }

    private void joinGroup() {
        String groupName = JOptionPane.showInputDialog(this, "参加するグループ名を入力してください。", "グループ参加", JOptionPane.PLAIN_MESSAGE);
        if (groupName == null || groupName.trim().isEmpty()) {
            return;
        }
        groupName = groupName.trim();

        if (!groupData.containsKey(groupName)) {
            JOptionPane.showMessageDialog(this, "その名前のグループは存在しません。");
            return;
        }

        if (groupListModel.contains(groupName)) {
            JOptionPane.showMessageDialog(this, "既に参加済みのグループです。");
            return;
        }

        groupListModel.addElement(groupName);
        groupList.setSelectedValue(groupName, true);
        JOptionPane.showMessageDialog(this, "グループ「" + groupName + "」に参加しました。");
    }

    // 内部クラスで家計簿記録を表現
    private static class BudgetRecord {
        String user;
        String category;
        String item;
        int amount;
        String date;
        String memo;

        BudgetRecord(String user, String category, String item, int amount, String date, String memo) {
            this.user = user;
            this.category = category;
            this.item = item;
            this.amount = amount;
            this.date = date;
            this.memo = memo;
        }
    }

    // ユーティリティクラス（簡易版）
    private static class Utils {
        // 今日の日付を yyyy/MM/dd 形式で返す
        public static String getTodayDate() {
            java.time.LocalDate today = java.time.LocalDate.now();
            return today.toString().replace("-", "/");
        }
    }

    // 動作テスト用main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GroupCreatePanel());
    }
}
