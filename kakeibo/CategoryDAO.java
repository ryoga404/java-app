import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryDAO {

    /**
     * 指定したタイプ（収入 or 支出）のカテゴリを取得する
     * @param type "収入" または "支出"
     * @return カテゴリ名→IDのMap
     */
    public Map<String, Integer> getCategoriesByType(String type) {
        Map<String, Integer> categoryMap = new LinkedHashMap<>();
        String sql = "SELECT CategoryId, CategoryName FROM Category WHERE CategoryType = ? ORDER BY CategoryName";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type);
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
