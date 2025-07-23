import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ExportPanel extends JPanel {

    private MainFrame mainFrame;

    public ExportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton exportButton = new JButton("エクスポート");
        exportButton.addActionListener(this::handleExport);

        add(exportButton);
    }

    private void handleExport(ActionEvent e) {
        String userId = mainFrame.getCurrentUserId();

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

            // ★ ここでXMLパース（読み取り）テストだけ実施（結果はコンソールに表示）
            parseXml(file);
        }
    }

    // XMLファイルからRecordを読み取る（テスト用）
    private void parseXml(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList recordList = doc.getElementsByTagName("record");
            System.out.println("[XML パース結果] " + recordList.getLength() + " 件読み込み");

            for (int i = 0; i < recordList.getLength(); i++) {
                Element element = (Element) recordList.item(i);
                String id = getText(element, "id");
                String userId = getText(element, "userId");
                String category = getText(element, "category");
                int amount = Integer.parseInt(getText(element, "amount"));
                String date = getText(element, "date");
                String memo = getText(element, "memo");

                System.out.printf("Record[%d]: id=%s, userId=%s, category=%s, amount=%d, date=%s, memo=%s%n",
                        i + 1, id, userId, category, amount, date, memo);
            }
        } catch (Exception e) {
            System.err.println("XMLの読み取りに失敗: " + e.getMessage());
        }
    }

    private String getText(Element parent, String tag) {
        NodeList nodeList = parent.getElementsByTagName(tag);
        if (nodeList.getLength() == 0) return "";
        return nodeList.item(0).getTextContent();
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
