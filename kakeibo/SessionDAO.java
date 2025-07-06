import java.sql.*;

public class SessionDAO {

    //セッションを保存
    public boolean createSession(String sessionId, String userId) {
        String sql = "INSERT INTO Session(session_id, user_id) VALUES(?, ?)";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt =conn.prepareStatement(sql)) {
                pstmt.setString(1, sessionId);
                pstmt.setString(2, userId);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
    }
}
