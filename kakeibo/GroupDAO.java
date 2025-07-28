import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

private static class GroupDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/kakeibo";
    private static final String USER = "your_db_user";
    private static final String PASS = "your_db_password";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public boolean createGroup(String groupName) {
        // グループテーブルがあればINSERTする想定
        // 例: groupsテーブルにgroupNameがなければ登録する
        String sql = "INSERT INTO groups (group_name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            // すでにある場合はOKとする
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveBudgetRecord(String currentGroup, BudgetRecord r) {
        String sql = "INSERT INTO kakeibo (group_name, user, type, category, amount, date, memo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentGroup);
            ps.setString(2, r.user);
            ps.setString(3, r.type);
            ps.setString(4, r.category);
            ps.setInt(5, r.amount);
            ps.setDate(6, Date.valueOf(r.date));
            ps.setString(7, r.memo);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBudgetRecord(String currentGroup, BudgetRecord r) {
        // idがない場合は複数条件で該当レコードを特定して削除（複数マッチの可能性あり）
        String sql = "DELETE FROM kakeibo WHERE group_name = ? AND user = ? AND type = ? AND category = ? AND amount = ? AND date = ? AND memo = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentGroup);
            ps.setString(2, r.user);
            ps.setString(3, r.type);
            ps.setString(4, r.category);
            ps.setInt(5, r.amount);
            ps.setDate(6, Date.valueOf(r.date));
            ps.setString(7, r.memo);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean joinGroup(String loginId, String groupName) {
        // 参加メンバー管理テーブルがあればINSERTする想定
        String sql = "INSERT INTO group_members (group_name, user) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            ps.setString(2, loginId);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            // すでに参加済みの場合OKとする
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
