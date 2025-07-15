import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

public class CheckUserName {

    private static final Set<String> forbiddenHashes = Set.of(
        "4813494d137e1631bba301d5acab6e7bb7aa74ce1185d456565ef51d737677b2", 
        "fc6f856465a388f8ae3530e3bf7564792f8e4816bdc54963f718720782586109",
        "4194d1706ed1f408d5e02d672777019f4d5385c766a8c6ca8acba3167d36a7b9", 
        "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",
    	"6b3a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e"
    );
    
    private static final List<Character> forbiddenChars = List.of(
        '=', '!', '\'', '"', '?', '@','&','|'
    );

    /**
     * ユーザー名をチェックし、問題があればエラーメッセージを返す（正常時はnull）
     */
    public static String validate(String userName) {
        // 1. null・空文字チェック
        if (userName == null || userName.isEmpty()) {
            return "ユーザー名を入力してください。";
        }

        // 2. 禁止文字チェック（記号など）
        for (char c : forbiddenChars) {
            if (userName.indexOf(c) != -1) {
                return "ユーザー名に禁止文字 '" + c + "' は使用できません。";
            }
        }

        // 3. 正規表現チェック（英数字8〜20文字）
        if (!userName.matches("^[a-zA-Z0-9]{8,20}$")) {
            return "ユーザー名は英数字8〜20文字で入力してください。";
        }

        // 4. 禁止ハッシュチェック（特定の単語のハッシュと照合）
        String hash = sha256(userName);
        if (forbiddenHashes.contains(hash)) {
            return "このユーザー名は使用できません。別の名前を選んでください。";
        }

        return null; // 問題なし
    }


    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256が利用できません", e);
        }
    }
}
