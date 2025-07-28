import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    // グループ作成（GroupNameが一意）
    public boolean createGroup(String groupName) {
        String sql = "INSERT INTO grouptable (GroupName) VALUES (?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
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

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
