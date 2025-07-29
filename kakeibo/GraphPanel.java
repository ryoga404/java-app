import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class GraphPanel extends JPanel {
    private final JComboBox<Integer> yearCombo = new JComboBox<>();
    private final JComboBox<Integer> monthCombo = new JComboBox<>();
    private final JButton refreshButton = new JButton("グラフ更新");
    private final JButton exportPNGButton = new JButton("PNG保存");
    private final JButton exportCSVButton = new JButton("CSV保存");
    private ChartPanel chartPanel;

    private String userId; // ログイン済みユーザーIDを格納
    private DefaultPieDataset currentDataset;
    private MainFrame mainFrame;

    public GraphPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userId = mainFrame.getCurrentUserId(); // ログイン済みユーザID取得
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setSize(800, 600);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("年："));
        topPanel.add(yearCombo);
        topPanel.add(new JLabel("月："));
        topPanel.add(monthCombo);
        topPanel.add(refreshButton);
        topPanel.add(exportPNGButton);
        topPanel.add(exportCSVButton);
        add(topPanel, BorderLayout.NORTH);

        initDateSelectors();

        chartPanel = new ChartPanel(null);
        add(chartPanel, BorderLayout.CENTER);

        refreshButton.addActionListener(e -> updateChart());
        exportPNGButton.addActionListener(e -> exportChartAsPNG());
        exportCSVButton.addActionListener(e -> exportDataAsCSV());

        updateChart();
    }

    private void initDateSelectors() {
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 5; y <= currentYear + 1; y++) {
            yearCombo.addItem(y);
        }
        yearCombo.setSelectedItem(currentYear);

        for (int m = 1; m <= 12; m++) {
            monthCombo.addItem(m);
        }
        monthCombo.setSelectedItem(LocalDate.now().getMonthValue());
    }

    private int getSelectedYear() {
        return (int) yearCombo.getSelectedItem();
    }

    private int getSelectedMonth() {
        return (int) monthCombo.getSelectedItem();
    }

    private void updateChart() {
        int year = getSelectedYear();
        int month = getSelectedMonth();
        JFreeChart chart = createChart(year, month);
        chartPanel.setChart(chart);
    }

    private JFreeChart createChart(int year, int month) {
        currentDataset = new DefaultPieDataset();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);

        // SQL文：テーブル名とカラム名をバッククォートで囲むことで大文字小文字や予約語を回避
        String sqlUser =
            "SELECT c.`CategoryName`, SUM(r.`Amount`) AS Total " +
            "FROM `Record` r JOIN `Category` c ON r.`CategoryId` = c.`CategoryId` " +
            "WHERE r.`UserId` = ? AND r.`Date` >= ? AND r.`Date` < ? AND c.`CategoryType` = 'OUT' " +
            "GROUP BY c.`CategoryName`";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlUser)) {

            stmt.setString(1, userId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("CategoryName");
                    int total = rs.getInt("Total");
                    currentDataset.setValue(category, total);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "データ取得エラー: " + e.getMessage());
        }

        String title = String.format("%d年%d月の支出カテゴリ別円グラフ", year, month);
        JFreeChart chart = ChartFactory.createPieChart(title, currentDataset, true, true, false);

        Font jpFont = new Font("Meiryo", Font.PLAIN, 14);
        chart.getTitle().setFont(jpFont);
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(jpFont);
        }
        if (chart.getPlot() instanceof PiePlot) {
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setLabelFont(jpFont);
        }

        return chart;
    }

    private void exportChartAsPNG() {
        if (chartPanel.getChart() == null) {
            JOptionPane.showMessageDialog(this, "グラフがありません。");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("PNGファイルの保存先を選択");
        chooser.setFileFilter(new FileNameExtensionFilter("PNG画像 (*.png)", "png"));
        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getParentFile(), file.getName() + ".png");
            }

            try {
                ChartUtilities.saveChartAsPNG(file, chartPanel.getChart(),
                        chartPanel.getWidth(), chartPanel.getHeight());
                JOptionPane.showMessageDialog(this, "PNG画像を保存しました:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "PNG画像の保存に失敗しました:\n" + ex.getMessage());
            }
        }
    }

    private void exportDataAsCSV() {
        if (currentDataset == null || currentDataset.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "エクスポートするデータがありません。");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("CSVファイルの保存先を選択");
        chooser.setFileFilter(new FileNameExtensionFilter("CSVファイル (*.csv)", "csv"));
        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("カテゴリ,合計金額\n");
                for (int i = 0; i < currentDataset.getItemCount(); i++) {
                    Comparable<?> key = currentDataset.getKey(i);
                    Number value = currentDataset.getValue(i);
                    writer.write(String.format("%s,%s\n", key.toString(), value.toString()));
                }
                JOptionPane.showMessageDialog(this, "CSVファイルを保存しました:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "CSV保存に失敗しました:\n" + ex.getMessage());
            }
        }
    }
}
