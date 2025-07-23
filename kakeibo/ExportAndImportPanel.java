import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ExportAndImportPanel extends JPanel {

    private MainFrame mainFrame;

    public ExportAndImportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton exportButton = new JButton("エクスポート");
        JButton importButton = new JButton("インポート");

        exportButton.addActionListener(this::handleExport);
        importButton.addActionListener(this::handleImport); // ← 中身はファイルを開くだけ

        add(exportButton);
        add(importButton);
    }

    private void handleExport(ActionEvent e) {
        String userId = mainFrame.getCurrentUserId(); // ログイン中のユーザーID取得

        if (userId == null || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ユーザーがログインしていません。", "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Record> records = RecordDAO.getRecordsByUserId(userId);
        if (records.isEmpty()) {
            JOptionPane.showMessageDialog(this, "エクスポートするデータがありません。", "情報", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存先を選択してください");
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".xml")) {
                file = new File(file.getAbsolutePath() + ".xml");
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(generateXml(records));
                JOptionPane.showMessageDialog(this, "エクスポートが完了しました。", "完了", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存に失敗しました: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void handleImport(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("インポートするXMLファイルを選択");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this, "ファイルが選択されました: \n" + file.getAbsolutePath(), "情報", JOptionPane.INFORMATION_MESSAGE);

            // パース処理はまだしない（ここで止めてOK）
            // 今後、パースして RecordDAO に登録したい場合はここに追加
        }
    }

    private String generateXml(List<Record> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<records>\n");
        for (Record r : records) {
            sb.append("  <record>\n");
            sb.append("    <id>").append(r.getId()).append("</id>\n");
            sb.append("    <userId>").append(escapeXml(r.getUserId())).append("</userId>\n");
            sb.append("    <category>").append(escapeXml(r.getCategory())).append("</category>\n");
            sb.append("    <amount>").append(r.getAmount()).append("</amount>\n");
            sb.append("    <date>").append(escapeXml(r.getDate())).append("</date>\n");
            sb.append("    <memo>").append(escapeXml(r.getMemo())).append("</memo>\n");
            sb.append("  </record>\n");
        }
        sb.append("</records>\n");
        return sb.toString();
    }

    private String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
}
