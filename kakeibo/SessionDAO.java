import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class SessionDAO {

    // セッション生成＋DB登録し、sessionIdを返す
    public String createSession(String userId) {
        String sessionId = UUID.randomUUID().toString();
        String sql = "INSERT INTO Session (SessionId, UserId, LoginTime) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            stmt.setString(2, userId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
            return sessionId;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // セッション有効チェック
    public boolean isSessionValid(String sessionId) {
        String sql = "SELECT 1 FROM Session WHERE session_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // セッションIDからユーザーID取得
    public String getUserIdBySession(String sessionId) {
        String sql = "SELECT user_id FROM Session WHERE session_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // セッション削除（ログアウト）
    public boolean deleteSession(String sessionId) {
        String sql = "DELETE FROM Session WHERE session_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
