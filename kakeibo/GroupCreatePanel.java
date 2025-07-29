import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class GroupCreatePanel extends JPanel {

    private GroupDAO groupDAO = new GroupDAO();

    private String currentGroup = null;
    private MainFrame mainFrame;

    // 引数1つのコンストラクター（HomePanelなどから呼び出される想定）
    public GroupCreatePanel(MainFrame mainFrame) {
        this(mainFrame, null);
    }

    // 引数2つのコンストラクター（グループ名指定あり）
    public GroupCreatePanel(MainFrame mainFrame, String groupName) {
        this.mainFrame = mainFrame;
        this.currentGroup = groupName;

        Font titleFont = new Font("Dialog", Font.BOLD, 22);
        Color secondaryColor = new Color(238, 238, 238);
        Color accentColor = new Color(255, 183, 77);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(secondaryColor);

        // 左パネル削除 → 中央にボタンを1つ配置
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(secondaryColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JButton createGroupBtn = new JButton("＋ 新規グループ作成");
        createGroupBtn.setBackground(accentColor);
        createGroupBtn.setForeground(Color.WHITE);
        createGroupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createGroupBtn.setPreferredSize(new Dimension(200, 40));
        createGroupBtn.addActionListener(e -> createGroup());

        centerPanel.add(createGroupBtn, gbc);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void createGroup() {
        String groupName = JOptionPane.showInputDialog(this, "新しいグループ名を入力してください。");
        if (groupName == null || groupName.trim().isEmpty()) return;
        groupName = groupName.trim();

        String loginId = mainFrame.getCurrentUserId();

        boolean created = groupDAO.createGroup(groupName);
        if (!created) {
            JOptionPane.showMessageDialog(this, "グループ作成に失敗しました。");
            return;
        }

        boolean joined = groupDAO.joinGroup(loginId, groupName);
        if (!joined) {
            JOptionPane.showMessageDialog(this, "グループへの参加に失敗しました。");
            return;
        }

        currentGroup = groupName;
        JOptionPane.showMessageDialog(this, "グループ「" + groupName + "」を作成し、参加しました。");
    }

    // 実際のDB処理を行うGroupDAOクラスに差し替え
    public static class GroupDAO {

        public boolean createGroup(String groupName) {
            String sql = "INSERT INTO grouptable (GroupName) VALUES (?)";
            try (var conn = DBUtil.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                ps.setString(1, groupName);
                ps.executeUpdate();
                return true;
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                // 重複しても成功とみなす
                return true;
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean joinGroup(String userId, String groupName) {
            String sql = """
                INSERT INTO groupmember (GroupId, UserId)
                SELECT g.GroupId, ?
                FROM grouptable g
                WHERE g.GroupName = ?
            """;
            try (var conn = DBUtil.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setString(2, groupName);
                ps.executeUpdate();
                return true;
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                // 既に参加済みの場合も成功とする
                return true;
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    // mainFrame からログイン中ユーザIDを取得するためのインターフェース
    public interface MainFrame {
        String getCurrentUserId();
    }
}
