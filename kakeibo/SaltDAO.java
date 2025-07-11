import java.sql.*;

public class SaltDAO {

    // Saltを登録
    public boolean insertSalt(String userId, String salt) {
        String sql = "INSERT INTO Salt (UserId, Salt) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, salt);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Saltを取得（ログイン用）
    public String getSalt(String userId) {
        String sql = "SELECT Salt FROM Salt WHERE UserId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Salt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
