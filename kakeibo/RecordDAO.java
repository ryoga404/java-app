import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecordDAO {

    private SessionDAO sessionDAO = new SessionDAO();

    // セッションIDを元にレコード追加
    public boolean addRecord(String sessionId, Date date, int categoryId, String type, int amount, String memo) {
        if (!sessionDAO.isSessionValid(sessionId)) {
            System.out.println("セッションが無効です。ログインしてください。");
            return false;
        }

        String userId = sessionDAO.getUserIdBySession(sessionId);
        if (userId == null) {
            System.out.println("ユーザーIDが取得できませんでした。");
            return false;
        }

        return addRecordByUserId(userId, date, categoryId, type, amount, memo);
    }

    // ユーザーIDを指定してレコード追加（セッションチェック不要）
    public boolean addRecordByUserId(String userId, Date date, int categoryId, String type, int amount, String memo) {
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

    // 指定ユーザーのすべてのレコード取得
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

    // レコードの更新
    public boolean updateRecord(Record record) {
        String sql = "UPDATE Record SET Date = ?, CategoryId = ?, Type = ?, Amount = ?, Memo = ? WHERE RecordId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, record.getDate());
            stmt.setInt(2, record.getCategoryId());
            stmt.setString(3, record.getType());
            stmt.setInt(4, record.getAmount());
            stmt.setString(5, record.getMemo());
            stmt.setInt(6, record.getRecordId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // レコードの削除
    public boolean deleteRecord(int recordId) {
        String sql = "DELETE FROM Record WHERE RecordId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, recordId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // DTO: Recordクラス
    public static class Record {
        private int recordId;
        private Date date;
        private int categoryId;
        private String categoryName;
        private String type;  // "In" or "Out"
        private int amount;
        private String memo;

        // Getter / Setter
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
