import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecordDAO {

    private SessionDAO sessionDAO = new SessionDAO();

    // セッションIDからユーザーIDを取得しつつ有効性チェック
    private String getUserIdBySession(String sessionId) {
        if (sessionId == null || !sessionDAO.isSessionValid(sessionId)) {
            System.out.println("セッションが無効またはnullです。");
            return null;
        }
        return sessionDAO.getUserIdBySession(sessionId);
    }

    // セッションIDをもとにレコード追加
    public boolean addRecord(String sessionId, Date date, int categoryId, String type, int amount, String memo) {
        String userId = getUserIdBySession(sessionId);
        if (userId == null) return false;
        return addRecordByUserId(userId, date, categoryId, type, amount, memo);
    }

    // ユーザーIDを指定してレコード追加
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

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // セッションIDをもとにレコード一覧取得
    public List<Record> getRecordsBySession(String sessionId) {
        String userId = getUserIdBySession(sessionId);
        if (userId == null) return new ArrayList<>();
        return getRecordsByUser(userId);
    }

    // ユーザーIDを指定してレコード一覧取得
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

    // レコード更新
    public boolean updateRecordBySession(String sessionId, Record record) {
        String userId = getUserIdBySession(sessionId);
        if (userId == null) return false;

        if (!isRecordOwner(userId, record.getRecordId())) {
            System.out.println("更新権限がありません。");
            return false;
        }

        return updateRecord(record);
    }

    // レコード更新（内部用）
    public static boolean updateRecord(Record record) {
        String sql = "UPDATE Record SET CategoryId = ?, Type = ?, Amount = ?, Memo = ? WHERE RecordId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, record.getCategoryId());
            stmt.setString(2, record.getType());
            stmt.setInt(3, record.getAmount());
            stmt.setString(4, record.getMemo());
            stmt.setInt(5, record.getRecordId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean updateRecordWithDate(Record record) {
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

    // レコード削除
    public boolean deleteRecordBySession(String sessionId, int recordId) {
        String userId = getUserIdBySession(sessionId);
        if (userId == null) return false;

        if (!isRecordOwner(userId, recordId)) {
            System.out.println("削除権限がありません。");
            return false;
        }

        return deleteRecord(recordId);
    }

    // レコード削除（内部用）
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

    // 所有権チェック
    private boolean isRecordOwner(String userId, int recordId) {
        String sql = "SELECT UserId FROM Record WHERE RecordId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String ownerId = rs.getString("UserId");
                return userId.equals(ownerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 外部から使う用
    public static List<Record> getRecordsByUserId(String userId) {
        RecordDAO dao = new RecordDAO();
        return dao.getRecordsByUser(userId);
    }

    // Record DTOクラス
    public static class Record {
    	
    	private String userName;

    	public String getUserName() { return userName; }
    	public void setUserName(String userName) { this.userName = userName; }

    	
    	private String userId;

    	public String getUserId() { return userId; }
    	public void setUserId(String userId) { this.userId = userId; }

        private int recordId;
        private Date date;
        private int categoryId;
        private String categoryName;
        private String type;  // "収入" or "支出"
        private int amount;
        private String memo;

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
