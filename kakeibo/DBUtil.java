//このクラスをインスタンス化してDB接続を行う

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
<<<<<<< HEAD
    private static final String URL = "jdbc:mysql://localhost:3306/kakeibo?useSSL=false&characterEncoding=utf8"; //接続のためのURL
=======
    private static final String URL = "jdbc:mysql://localhost:3306/kakeibo?useSSL=false&characterEncoding=UTF-8"; //接続のためのURL
>>>>>>> eb5ec34247fe4e8cf77565586b12ecfa73389425
    private static final String USER = "root"; //ユーザ名
    private static final String PASSWORD = ""; //パスワードなし

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL JDBCドライバ読み込み
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}