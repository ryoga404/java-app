import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

// パスワード処理
public class PassToHash {

    // ランダムソルト生成メソッド
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // パスワードとソルトを使ってハッシュ（1000回ストレッチング）
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combined = password + salt;
            byte[] hash = combined.getBytes();

            for (int i = 0; i < 1000; i++) {
                hash = md.digest(hash);
            }

            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("ハッシュアルゴリズムエラー:", e);
        }
    }

    // ソルトを作成してハッシュを生成
    public static HashedResult createHashedPassword(String password) {
        String salt = generateSalt();
        String hashed = hashPassword(password, salt);
        return new HashedResult(salt, hashed);
    }

    // ソルトとハッシュ結果を保持するクラス
    public static class HashedResult {
        public final String salt;
        public final String hashedPassword;

        public HashedResult(String salt, String hashedPassword) {
            this.salt = salt;
            this.hashedPassword = hashedPassword;
        }
    }
    
 // 既存のソルトを使ってハッシュを再生成（ログイン用）
    public static String hashWithSalt(String password, String salt) {
        return hashPassword(password, salt); // 内部の既存メソッドを再利用
    }

}

/*
 * 呼び出し方
 * PassToHash.HashResult result = PassToHash.createHashPassword(password);
 *
 *(以下をDB関連のユーザテーブル追加メソッド・ソルト追加メソッドへ渡す)
 * String salt = result.salt;
 * String hashedPassword = result.hashedPassword;
 */
