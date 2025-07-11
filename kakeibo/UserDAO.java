import java.sql.*;

public class UserDAO {

    // ユーザー登録
    public boolean addUser(String userId, String password) {
        try (Connection conn = DBUtil.getConnection()) {
            PassToHash.HashedResult pth = PassToHash.createHashedPassword(password);
            String salt = pth.salt;
            String hashedPassword = pth.hashedPassword;

            // Userテーブル登録
            String userSql = "INSERT INTO User (UserId, HashedPassword) VALUES (?, ?)";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, userId);
                userStmt.setString(2, hashedPassword);
                userStmt.executeUpdate();
            }

            // Saltテーブル登録
            String saltSql = "INSERT INTO Salt (UserId, Salt) VALUES (?, ?)";
            try (PreparedStatement saltStmt = conn.prepareStatement(saltSql)) {
                saltStmt.setString(1, userId);
                saltStmt.setString(2, salt);
                saltStmt.executeUpdate();
            }

            System.out.println("登録完了しました！");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("登録できませんでした！");
            return false;
        }
    }

    // ログイン認証
    public boolean login(String userId, String password) {
        try (Connection conn = DBUtil.getConnection()) {
            String salt = null;
            String saltSql = "SELECT Salt FROM Salt WHERE UserId = ?";
            try (PreparedStatement stmt = conn.prepareStatement(saltSql)) {
                stmt.setString(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    salt = rs.getString("Salt");
                } else {
                    System.out.println("ユーザーが存在しません");
                    return false;
                }
            }

            String hashedPassword = PassToHash.hashWithSalt(password, salt);

            String userSql = "SELECT * FROM User WHERE UserId = ? AND HashedPassword = ?";
            try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                stmt.setString(1, userId);
                stmt.setString(2, hashedPassword);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("ログイン成功！");
                    return true;
                } else {
                    System.out.println("パスワードが一致しません");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("ログイン処理中にエラーが発生しました");
            return false;
        }
    }
}
