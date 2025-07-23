import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ImportPanel extends JPanel {

    private MainFrame mainFrame;
    private RecordDAO recordDAO = new RecordDAO(); // レコード管理用DAO

    public ImportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton importButton = new JButton("インポート");
        importButton.addActionListener(this::handleImport);

        add(importButton);
    }

    private void handleImport(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("インポートするXMLファイルを選択");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (FileReader reader = new FileReader(file)) {
                // XMLファイルをパースしてレコードをインポート
                importFromXml(file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "インポートに失敗しました: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void importFromXml(File file) throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList recordNodes = doc.getElementsByTagName("record");
            if (recordNodes.getLength() == 0) {
                JOptionPane.showMessageDialog(this, "インポートするデータがありません。", "情報", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // レコードをデータベースにインポート
            for (int i = 0; i < recordNodes.getLength(); i++) {
                Element element = (Element) recordNodes.item(i);

                String id = getText(element, "id");
                String userId = getText(element, "userId");
                String category = getText(element, "category");
                int amount = Integer.parseInt(getText(element, "amount"));
                String date = getText(element, "date");
                String memo = getText(element, "memo");

                // レコードをデータベースに追加
                boolean success = recordDAO.addRecordByUserId(userId, java.sql.Date.valueOf(date), 0, category, amount, memo);
                if (!success) {
                    throw new Exception("レコードのインポートに失敗しました: " + id);
                }
            }

            JOptionPane.showMessageDialog(this, "インポートが完了しました。", "完了", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "インポート中にエラーが発生しました: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // XMLノードからタグの値を取得するヘルパーメソッド
    private String getText(Element parent, String tag) {
        NodeList nodeList = parent.getElementsByTagName(tag);
        if (nodeList.getLength() == 0) return "";
        return nodeList.item(0).getTextContent();
    }
}
