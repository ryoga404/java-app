import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ImportPanel extends JPanel {

    private MainFrame mainFrame;
    private RecordDAO recordDAO = new RecordDAO();

    public ImportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton importButton = new JButton("CSVインポート");
        importButton.addActionListener(this::handleImport);
        add(importButton);
    }

    private void handleImport(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("CSVファイルを選択");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                importFromCsv(file);
                JOptionPane.showMessageDialog(this, "インポートが完了しました。", "完了", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "インポートに失敗しました: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void importFromCsv(File file) throws Exception {
        String userId = mainFrame.getCurrentUserId(); // ログイン中のユーザーID（String型）

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false); // トランザクション開始

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = parseCsvLine(line);
                if (parts.length < 5) continue;

                String dateStr = parts[0].trim();
                String categoryType = parts[1].trim().toUpperCase();
                String categoryName = parts[2].trim();
                String amountStr = parts[3].trim();
                String memo = parts[4].trim();

                if (!categoryType.equals("IN") && !categoryType.equals("OUT")) {
                    System.err.println("無効なカテゴリタイプ（スキップ）: " + categoryType);
                    continue;
                }

                int amount = Integer.parseInt(amountStr);
                Date date = Date.valueOf(dateStr);
                int categoryId = findOrCreateCategory(conn, categoryName, categoryType);

                boolean success = recordDAO.addRecordByUserId(userId, date, categoryId, categoryType, amount, memo);
                if (!success) {
                    throw new Exception("レコード追加に失敗しました");
                }
            }

            conn.commit();

        } catch (Exception ex) {
            throw new Exception("CSVインポート中にエラー: " + ex.getMessage(), ex);
        }
    }

    // カテゴリIDを取得または新規作成
    private int findOrCreateCategory(Connection conn, String name, String type) throws Exception {
        String selectSql = "SELECT CategoryId FROM Category WHERE CategoryName = ? AND CategoryType = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, name);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CategoryId");
                }
            }
        }

        String insertSql = "INSERT INTO Category (CategoryName, CategoryType) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, type);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new Exception("カテゴリの作成に失敗しました: " + name + " / " + type);
    }

    // カンマとダブルクォートに対応したCSVパース
    private String[] parseCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }
}
