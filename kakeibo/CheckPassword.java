import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class CheckPassword {

    private static final Set<String> forbiddenHashes = Set.of(
        "4813494d137e1631bba301d5acab6e7bb7aa74ce1185d456565ef51d737677b2", 
        "fc6f856465a388f8ae3530e3bf7564792f8e4816bdc54963f718720782586109",
        "4194d1706ed1f408d5e02d672777019f4d5385c766a8c6ca8acba3167d36a7b9", 
        "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",
        "6b3a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e"
    );

    private static final Set<Character> specialChars = Set.of('!', '#', '_', '@');

    // 禁止文字（例: ' " ” ）
    private static final Set<Character> forbiddenChars = Set.of('\'', '\"', '”');

    public static String validate(String password, String userName) {
        if (password == null || password.isEmpty()) {
            return "パスワードを入力してください。";
        }

        if (password.length() < 8 || password.length() > 20) {
            return "パスワードは8～20文字で入力してください。";
        }

        if (containsForbiddenChar(password)) {
            return "パスワードに使用できない文字が含まれています（例: ' \" ”）。";
        }

        if (password.equals(userName)) {
            return "パスワードはユーザー名と異なるものにしてください。";
        }

        if (hasThreeOrMoreConsecutiveSameChars(password)) {
            return "同じ文字を3回以上連続して使用できません。";
        }

        String hash = sha256(password);
        if (forbiddenHashes.contains(hash)) {
            return "そのパスワードは使用できません。別のものを選んでください。";
        }

        if (!containsUpperLowerDigit(password)) {
            return "パスワードは英大文字・小文字・数字を１つ以上含んでください。";
        }

        if (!containsSpecialChar(password)) {
            return "パスワードは特殊文字（例: !#@_）を１つ以上含んでください。";
        }

        return null; // 問題なし
    }

    // 同じ文字が3回以上連続しているかチェック
    private static boolean hasThreeOrMoreConsecutiveSameChars(String s) {
        int count = 1;
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) {
                count++;
                if (count >= 3) return true;
            } else {
                count = 1;
            }
        }
        return false;
    }

    // 英大文字・小文字・数字のすべてを含むか
    public static boolean containsUpperLowerDigit(String password) {
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;

            if (hasUpper && hasLower && hasDigit) {
                return true;
            }
        }

        return false;
    }

    // 特殊文字を含むかチェック
    private static boolean containsSpecialChar(String password) {
        for (char c : password.toCharArray()) {
            if (specialChars.contains(c)) {
                return true;
            }
        }
        return false;
    }

    // 禁止文字を含むかチェック
    private static boolean containsForbiddenChar(String password) {
        for (char c : password.toCharArray()) {
            if (forbiddenChars.contains(c)) {
                return true;
            }
        }
        return false;
    }

    // SHA-256ハッシュ計算
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
