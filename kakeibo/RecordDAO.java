import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecordDAO {

    private SessionDAO sessionDAO = new SessionDAO();

    // 家計簿レコード追加（セッション有効チェックあり）
    public boolean addRecord(String sessionId, Date date, int categoryId, String type, int amount, String memo) {
        // セッションが有効かチェック
        if (!sessionDAO.isSessionValid(sessionId)) {
            System.out.println("セッションが無効です。ログインしてください。");
            return false;
        }

        // sessionIdからuserIdを取得
        String userId = sessionDAO.getUserIdBySession(sessionId);
        if (userId == null) {
            System.out.println("ユーザーIDが取得できませんでした。");
            return false;
        }

        String sql = "INSERT INTO Record (UserId, Date, CategoryId, Type, Amount, Memo) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setDate(2, date);
            stmt.setInt(3, categoryId);
            stmt.setString(4, type);
            stmt.setInt(5, amount);
            stmt.setString(6, memo);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ユーザーの全レコード取得
    public List<Record> getRecordsByUser(String userId) {
        List<Record> records = new ArrayList<>();
        String sql = "SELECT r.RecordId, r.Date, r.CategoryId, c.CategoryName, r.Type, r.Amount, r.Memo " +
                     "FROM Record r LEFT JOIN Category c ON r.CategoryId = c.CategoryId " +
                     "WHERE r.UserId = ? ORDER BY r.Date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Record record = new Record();
                record.setRecordId(rs.getInt("RecordId"));
                record.setDate(rs.getDate("Date"));
                record.setCategoryId(rs.getInt("CategoryId"));
                record.setCategoryName(rs.getString("CategoryName"));
                record.setType(rs.getString("Type"));
                record.setAmount(rs.getInt("Amount"));
                record.setMemo(rs.getString("Memo"));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    // Record クラス（DTO）
    public static class Record {
        private int recordId;
        private Date date;
        private int categoryId;
        private String categoryName;
        private String type;  // "In" or "Out"
        private int amount;
        private String memo;

        // getter/setter省略（必要に応じて実装してください）
        public int getRecordId() { return recordId; }
        public void setRecordId(int recordId) { this.recordId = recordId; }
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
        public int getCategoryId() { return categoryId; }
        public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
        public String getMemo() { return memo; }
        public void setMemo(String memo) { this.memo = memo; }
    }
}
