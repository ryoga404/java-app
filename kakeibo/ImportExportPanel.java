import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImportExportPanel extends JPanel {

    private MainFrame mainFrame;
    private JButton importButton;
    private JButton exportButton;

    public ImportExportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // ヘッダーラベル
        JLabel label = new JLabel("インポート / エクスポート画面", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setForeground(new Color(80, 80, 80));
        add(label, BorderLayout.NORTH);

        // ボタンパネル
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 0)); // 2列
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // インポートボタン
        importButton = new JButton("インポート");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performImport();
            }
        });

        // エクスポートボタン
        exportButton = new JButton("エクスポート");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performExport();
            }
        });

        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);

        add(buttonPanel, BorderLayout.CENTER);
    }

    // エクスポート処理
    private void performExport() {
        String sessionId = mainFrame.getSessionId(); // セッションIDを取得
        String userId = getUserIdBySession(sessionId); // セッションIDを渡してユーザIDを取得
        if (userId == null) {
            JOptionPane.showMessageDialog(this, "ログインしていません。", "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "SELECT r.Date, c.CategoryType, c.CategoryName, r.Amount, r.Memo " +
                       "FROM Record r " +
                       "JOIN Category c ON r.CategoryId = c.CategoryId " +
                       "WHERE r.UserId = ? " +
                       "ORDER BY r.Date;";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("エクスポート先のCSVファイルを選択");
        fileChooser.setSelectedFile(new File("exported_data.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSVファイル (*.csv)", "csv"));
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return; // ユーザーがキャンセルした場合
        }

        File selectedFile = fileChooser.getSelectedFile();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(selectedFile), "UTF-8"))) {
                // CSVヘッダー
                writer.write("Date,CategoryType,CategoryName,Amount,Memo\n");

                // 結果セットからデータをCSVに書き込む
                while (rs.next()) {
                    String date = rs.getString("Date");
                    String categoryType = rs.getString("CategoryType");
                    String categoryName = rs.getString("CategoryName");
                    String amount = rs.getString("Amount");
                    String memo = rs.getString("Memo");

                    // メモのカンマや改行の処理
                    memo = memo.replace(",", " ").replace("\n", " ").replace("\r", " ");
                    writer.write(String.format("%s,%s,%s,%s,%s\n", date, categoryType, categoryName, amount, memo));
                }

                JOptionPane.showMessageDialog(this, "エクスポートが完了しました。");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "ファイルへの書き込みエラー", "エラー", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "データベースエラー", "エラー", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
 // インポート処理
    private void performImport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("インポートするCSVファイルを選択");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSVファイル (*.csv)", "csv"));
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return; // ユーザーがキャンセルした場合
        }

        File selectedFile = fileChooser.getSelectedFile();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(selectedFile), "UTF-8"))) {
            String line;
            reader.readLine(); // ヘッダー行をスキップ

            String insertRecordSQL = "INSERT INTO Record (UserId, Date, CategoryId, Amount, Memo, Type) VALUES (?, ?, ?, ?, ?, ?)";
            String getCategoryIdSQL = "SELECT CategoryId FROM Category WHERE CategoryName = ? AND CategoryType = ?";

            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmtRecord = conn.prepareStatement(insertRecordSQL);
                 PreparedStatement stmtCategory = conn.prepareStatement(getCategoryIdSQL)) {

                String sessionId = mainFrame.getSessionId(); // セッションIDを取得
                String userId = getUserIdBySession(sessionId); // セッションIDを渡してユーザIDを取得

                if (userId == null) {
                    JOptionPane.showMessageDialog(this, "ログインしていません。", "エラー", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",");

                    String date = columns[0];
                    String categoryType = columns[1];
                    String categoryName = columns[2];
                    String amount = columns[3];
                    String memo = columns.length > 4 ? columns[4] : ""; // Memoが空の場合は空文字をセット

                    // Memoが空の場合は空文字をセット
                    if (memo == null || memo.isEmpty()) {
                        memo = "";
                    }

                    // CategoryIdを取得、なければ新規作成
                    stmtCategory.setString(1, categoryName);
                    stmtCategory.setString(2, categoryType);
                    ResultSet rs = stmtCategory.executeQuery();

                    int categoryId;
                    if (rs.next()) {
                        categoryId = rs.getInt("CategoryId");
                    } else {
                        // カテゴリが存在しない場合、新規登録
                        String insertCategorySQL = "INSERT INTO Category (CategoryName, CategoryType) VALUES (?, ?)";
                        try (PreparedStatement stmtInsertCategory = conn.prepareStatement(insertCategorySQL, Statement.RETURN_GENERATED_KEYS)) {
                            stmtInsertCategory.setString(1, categoryName);
                            stmtInsertCategory.setString(2, categoryType);
                            stmtInsertCategory.executeUpdate();

                            // 新規IDを取得
                            ResultSet generatedKeys = stmtInsertCategory.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                categoryId = generatedKeys.getInt(1);
                            } else {
                                throw new SQLException("カテゴリの登録に失敗しました");
                            }
                        }
                    }

                    // Typeを設定: CategoryTypeが"IN"なら"収入"、"OUT"なら"支出"とする
                    String type = categoryType.equals("IN") ? "収入" : categoryType.equals("OUT") ? "支出" : "その他";

                    // レコードをINSERT
                    stmtRecord.setString(1, userId);
                    stmtRecord.setString(2, date);
                    stmtRecord.setInt(3, categoryId);
                    stmtRecord.setString(4, amount);
                    stmtRecord.setString(5, memo);  // 空文字を代入したmemoをセット
                    stmtRecord.setString(6, type);  // Type列に値をセット
                    stmtRecord.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "インポートが完了しました。");

            } catch (SQLException | IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "インポート中にエラーが発生しました。", "エラー", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "ファイル読み込みエラー", "エラー", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }





    // セッションIDを渡してユーザIDを取得
    private String getUserIdBySession(String sessionId) {
        RecordDAO recordDAO = new RecordDAO();
        return recordDAO.getUserId(sessionId); // 新しく追加した public メソッドを呼び出す
    }
}
