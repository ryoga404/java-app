import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {
<<<<<<< HEAD
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
=======

    // グループ作成（GroupNameが一意）
    public boolean createGroup(String groupName) {
        String sql = "INSERT INTO grouptable (GroupName) VALUES (?)";
        try (Connection conn = DBUtil.getConnection();
>>>>>>> c79f88f2ff1ded1b8abd69085663733a7280ad28
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
<<<<<<< HEAD
            return true; // 既に存在していたらOK
=======
            // 既に存在していた場合でもOKとする
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // グループ参加（groupmember テーブルに追加）
    public boolean joinGroup(String userId, String groupName) {
        String sql = """
            INSERT INTO groupmember (GroupId, UserId)
            SELECT g.GroupId, ?
            FROM grouptable g
            WHERE g.GroupName = ?
        """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, groupName);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            // 既に参加済みの場合でもOKとする
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // グループに紐づくユーザーの全レコードを取得（ユーザ名も含む）
    public List<RecordDAO.Record> getGroupRecords(String groupName) {
        List<RecordDAO.Record> list = new ArrayList<>();
        String sql = """
            SELECT r.RecordId, r.UserId, u.UserName, r.Date, r.CategoryId, c.CategoryName, r.Type, r.Amount, r.Memo
            FROM grouptable g
            JOIN groupmember gm ON g.GroupId = gm.GroupId
            JOIN record r ON gm.UserId = r.UserId
            JOIN users u ON r.UserId = u.UserId
            LEFT JOIN category c ON r.CategoryId = c.CategoryId
            WHERE g.GroupName = ?
            ORDER BY r.Date DESC
        """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RecordDAO.Record record = new RecordDAO.Record();
                record.setRecordId(rs.getInt("RecordId"));
                record.setUserId(rs.getString("UserId"));
                record.setUserName(rs.getString("UserName"));
                record.setDate(rs.getDate("Date"));
                record.setCategoryId(rs.getInt("CategoryId"));
                record.setCategoryName(rs.getString("CategoryName"));
                record.setType(rs.getString("Type"));
                record.setAmount(rs.getInt("Amount"));
                record.setMemo(rs.getString("Memo"));

                list.add(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // グループに紐づくレコードを保存（RecordDAO.Record 使用）
    public boolean saveGroupRecord(String groupName, RecordDAO.Record r, String userId) {
        String sql = "INSERT INTO record (UserId, Date, CategoryId, Type, Amount, Memo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setDate(2, r.getDate());
            ps.setInt(3, r.getCategoryId());
            ps.setString(4, r.getType());
            ps.setInt(5, r.getAmount());
            ps.setString(6, r.getMemo());

            int affected = ps.executeUpdate();
            return affected > 0;

>>>>>>> c79f88f2ff1ded1b8abd69085663733a7280ad28
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

<<<<<<< HEAD
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
=======
    // グループに紐づくレコード削除（完全一致で1件限定）
    public boolean deleteGroupRecord(String groupName, RecordDAO.Record r, String userId) {
        String sql = """
            DELETE FROM record
            WHERE UserId = ? AND Date = ? AND CategoryId = ? AND Type = ? AND Amount = ? AND Memo = ?
            LIMIT 1
        """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setDate(2, r.getDate());
            ps.setInt(3, r.getCategoryId());
            ps.setString(4, r.getType());
            ps.setInt(5, r.getAmount());
            ps.setString(6, r.getMemo());

            int affected = ps.executeUpdate();
            return affected > 0;

>>>>>>> c79f88f2ff1ded1b8abd69085663733a7280ad28
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
