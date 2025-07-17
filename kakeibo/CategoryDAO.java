import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryDAO {

    /**
     * 指定したタイプ（収入 or 支出）のカテゴリを取得する
     * @param type "収入" または "支出"
     * @return カテゴリ名→IDのMap
     */
    public Map<String, Integer> getCategoriesByType(String type) {
        // DB enum値に変換
        String dbType;
        if ("収入".equals(type)) {
            dbType = "IN";
        } else if ("支出".equals(type)) {
            dbType = "OUT";
        } else {
            // 収入・支出以外は空返却
            return new LinkedHashMap<>();
        }

        Map<String, Integer> categoryMap = new LinkedHashMap<>();
        String sql = "SELECT CategoryId, CategoryName FROM Category WHERE CategoryType = ? ORDER BY CategoryName";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dbType);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("CategoryId");
                    String name = rs.getString("CategoryName");
                    categoryMap.put(name, id);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryMap;
    }
}
