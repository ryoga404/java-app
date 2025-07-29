import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class GroupDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/kakeibo";
    private static final String USER = "your_db_user";
    private static final String PASS = "your_db_password";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // グループ作成（重複チェック付き）
    public boolean createGroup(String groupName) {
        String sql = "INSERT INTO grouptable (GroupName) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            return true; // 既に存在していたらOK
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // グループ参加
    public boolean joinGroup(String loginId, String groupName) {
        String sql = "INSERT INTO groupmember (GroupName, User) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            ps.setString(2, loginId);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            return true; // すでに参加済み
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 家計簿登録
    public boolean saveBudgetRecord(String currentGroup, String user, String type,
            String category, int amount, String date, String memo) {
String sql = "INSERT INTO kakeibo (group_name, user, type, category, amount, date, memo) VALUES (?, ?, ?, ?, ?, ?, ?)";
try (Connection conn = getConnection();
PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setString(1, currentGroup);
ps.setString(2, user);
ps.setString(3, type);
ps.setString(4, category);
ps.setInt(5, amount);
ps.setDate(6, Date.valueOf(date));
ps.setString(7, memo);
ps.executeUpdate();
return true;
} catch (SQLException e) {
e.printStackTrace();
return false;
}
}


    // 家計簿削除
    public boolean deleteBudgetRecord(String currentGroup, String user, String type,
            String category, int amount, String date, String memo) {
String sql = "DELETE FROM kakeibo WHERE group_name = ? AND user = ? AND type = ? AND category = ? AND amount = ? AND date = ? AND memo = ? LIMIT 1";
try (Connection conn = getConnection();
PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setString(1, currentGroup);
ps.setString(2, user);
ps.setString(3, type);
ps.setString(4, category);
ps.setInt(5, amount);
ps.setDate(6, Date.valueOf(date));
ps.setString(7, memo);
int affected = ps.executeUpdate();
return affected > 0;
} catch (SQLException e) {
e.printStackTrace();
return false;
}
}


    // グループが存在するか確認
    public boolean groupExists(String groupName) {
        String sql = "SELECT GroupId FROM grouptable WHERE GroupName = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
