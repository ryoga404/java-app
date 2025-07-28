import java.awt.BorderLayout;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class GraphPanel extends JFrame {
    private final JComboBox<Integer> yearCombo = new JComboBox<>();
    private final JComboBox<Integer> monthCombo = new JComboBox<>();
    private final JButton refreshButton = new JButton("グラフ更新");
    private final JButton exportPNGButton = new JButton("PNG保存");
    private final JButton exportCSVButton = new JButton("CSV保存");
    private final ChartPanel chartPanel;

    // モード切り替え（個人 or グループ）
    private final boolean useGroupMode = false; // true:グループ, false:個人
    private final int groupId = 1;
    private final String userId = "user01";

    // 現在表示中のデータセット（エクスポート用）
    private DefaultPieDataset currentDataset;

    public GraphPanel() {
        setTitle("家計簿円グラフ");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 年月選択パネル
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("年："));
        topPanel.add(yearCombo);
        topPanel.add(new JLabel("月："));
        topPanel.add(monthCombo);
        topPanel.add(refreshButton);
        topPanel.add(exportPNGButton);
        topPanel.add(exportCSVButton);
        add(topPanel, BorderLayout.NORTH);

        // 初期年月設定
        initDateSelectors();

        // グラフパネル（最初は空のグラフ）
        chartPanel = new ChartPanel(null);
        add(chartPanel, BorderLayout.CENTER);

        // イベント設定
        refreshButton.addActionListener(e -> updateChart());
        exportPNGButton.addActionListener(e -> exportChartAsPNG());
        exportCSVButton.addActionListener(e -> exportDataAsCSV());

        // 最初のグラフ表示
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

        String sqlGroup =
            "SELECT c.CategoryName, SUM(r.Amount) AS Total " +
            "FROM record r JOIN category c ON r.CategoryId = c.CategoryId " +
            "JOIN groupmember gm ON r.UserId = gm.UserId " +
            "WHERE gm.GroupId = ? AND r.Date >= ? AND r.Date < ? AND c.CategoryType = 'OUT' " +
            "GROUP BY c.CategoryName";

        String sqlUser =
            "SELECT c.CategoryName, SUM(r.Amount) AS Total " +
            "FROM record r JOIN category c ON r.CategoryId = c.CategoryId " +
            "WHERE r.UserId = ? AND r.Date >= ? AND r.Date < ? AND c.CategoryType = 'OUT' " +
            "GROUP BY c.CategoryName";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(useGroupMode ? sqlGroup : sqlUser)) {

            if (useGroupMode) {
                stmt.setInt(1, groupId);
                stmt.setDate(2, Date.valueOf(startDate));
                stmt.setDate(3, Date.valueOf(endDate));
            } else {
                stmt.setString(1, userId);
                stmt.setDate(2, Date.valueOf(startDate));
                stmt.setDate(3, Date.valueOf(endDate));
            }

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
        return ChartFactory.createPieChart(title, currentDataset, true, true, false);
    }

    // PNG保存
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
                // JFreeChart 1.0.19 では ChartUtilities を使用
                ChartUtilities.saveChartAsPNG(file, chartPanel.getChart(), chartPanel.getWidth(), chartPanel.getHeight());
                JOptionPane.showMessageDialog(this, "PNG画像を保存しました:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "PNG画像の保存に失敗しました:\n" + ex.getMessage());
            }
        }
    }

    // CSV保存
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
                    Comparable key = currentDataset.getKey(i);
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
