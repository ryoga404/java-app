import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // ユーザー登録処理
    public boolean addUser(String userId, String password) {
        try (Connection conn = DBUtil.getConnection()) {
            PassToHash.HashedResult pth = PassToHash.createHashedPassword(password);
            String salt = pth.salt;
            String hashedPassword = pth.hashedPassword;

            // Userテーブルに登録
            String userSql = "INSERT INTO User (UserId, HashedPassword) VALUES (?, ?)";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, userId);
                userStmt.setString(2, hashedPassword);
                userStmt.executeUpdate();
            }

            // Saltテーブルに登録
            String saltSql = "INSERT INTO Salt (UserId, Salt) VALUES (?, ?)";
            try (PreparedStatement saltStmt = conn.prepareStatement(saltSql)) {
                saltStmt.setString(1, userId);
                saltStmt.setString(2, salt);
                saltStmt.executeUpdate();
            }

            //System.out.println("登録完了しました！");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            //System.out.println("登録できませんでした！");
            return false;
        }
    }

    // ID・パスワードの正当性のみ確認（ログイン処理ではない）
    public boolean checkCredentials(String userId, String password) {
        try (Connection conn = DBUtil.getConnection()) {
            String salt = null;

            // Saltを取得
            String saltSql = "SELECT Salt FROM Salt WHERE UserId = ?";
            try (PreparedStatement stmt = conn.prepareStatement(saltSql)) {
                stmt.setString(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    salt = rs.getString("Salt");
                } else {
                    return false; // ユーザーが存在しない
                }
            }

            // 入力されたパスワードをハッシュ化して一致確認
            String hashedPassword = PassToHash.hashWithSalt(password, salt);
            String userSql = "SELECT * FROM User WHERE UserId = ? AND HashedPassword = ?";
            try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                stmt.setString(1, userId);
                stmt.setString(2, hashedPassword);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // CAPTCHA通過後のセッション作成用
    public String loginAfterCaptcha(String userId) {
        SessionDAO sessionDAO = new SessionDAO();
        return sessionDAO.createSession(userId);
    }
}
