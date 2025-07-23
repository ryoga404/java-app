import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ImportPanel extends JPanel {

    private MainFrame mainFrame;

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
            JOptionPane.showMessageDialog(this, "ファイルが選択されました: \n" + file.getAbsolutePath(), "情報", JOptionPane.INFORMATION_MESSAGE);

            try {
                List<Record> records = parseXml(file);
                if (records.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "インポートするデータがありません。", "情報", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // RecordDAOにインポートされたレコードを保存する処理
                for (Record record : records) {
                    RecordDAO.addRecord(record);
                }

                JOptionPane.showMessageDialog(this, "インポートが完了しました。", "完了", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "インポートに失敗しました: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private List<Record> parseXml(File file) throws Exception {
        // XMLパース処理
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new Exception("XMLパーサーの初期化に失敗しました: " + ex.getMessage());
        }

        Document document;
        try (FileReader reader = new FileReader(file)) {
            document = builder.parse(new org.xml.sax.InputSource(reader));
        } catch (SAXException | java.io.IOException ex) {
            throw new Exception("XMLのパースに失敗しました: " + ex.getMessage());
        }

        NodeList recordNodes = document.getElementsByTagName("record");
        if (recordNodes.getLength() == 0) {
            return List.of();  // レコードがない場合は空リストを返す
        }

        List<Record> records = new java.util.ArrayList<>();
        for (int i = 0; i < recordNodes.getLength(); i++) {
            Element recordElement = (Element) recordNodes.item(i);
            String id = recordElement.getElementsByTagName("id").item(0).getTextContent();
            String userId = recordElement.getElementsByTagName("userId").item(0).getTextContent();
            String category = recordElement.getElementsByTagName("category").item(0).getTextContent();
            double amount = Double.parseDouble(recordElement.getElementsByTagName("amount").item(0).getTextContent());
            String date = recordElement.getElementsByTagName("date").item(0).getTextContent();
            String memo = recordElement.getElementsByTagName("memo").item(0).getTextContent();

            // Recordオブジェクトに変換してリストに追加
            Record record = new Record(id, userId, category, amount, date, memo);
            records.add(record);
        }

        return records;
    }
}
