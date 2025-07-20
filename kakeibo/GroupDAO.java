import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupDAO {

    // グループを作成
    public boolean createGroup(String groupName) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO grouptable (GroupName) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, groupName);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // グループを削除
    public boolean deleteGroup(String groupName) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "DELETE FROM grouptable WHERE GroupName = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, groupName);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // グループに参加
    public boolean joinGroup(String userId, String groupName) {
        try (Connection conn = DBUtil.getConnection()) {
            String groupId = getGroupId(groupName);
            if (groupId == null) {
                return false; // グループが存在しない
            }

            String sql = "INSERT INTO groupmember (GroupId, UserId) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, groupId);
                stmt.setString(2, userId);
                stmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // グループから脱退
    public boolean leaveGroup(String userId, String groupName) {
        try (Connection conn = DBUtil.getConnection()) {
            String groupId = getGroupId(groupName);
            if (groupId == null) {
                return false; // グループが存在しない
            }

            String sql = "DELETE FROM groupmember WHERE GroupId = ? AND UserId = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, groupId);
                stmt.setString(2, userId);
                stmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // グループIDを取得
    public String getGroupId(String groupName) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT GroupId FROM grouptable WHERE GroupName = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, groupName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("GroupId");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ←★追加：ユーザーIDから所属しているグループ名を取得
    public String getGroupNameByUserId(String userId) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT g.GroupName FROM grouptable g " +
                         "JOIN groupmember gm ON g.GroupId = gm.GroupId " +
                         "WHERE gm.UserId = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("GroupName");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
