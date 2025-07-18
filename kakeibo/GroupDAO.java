import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupDAO {
	
	
	/*・主にGroupCreatePanelから呼び出される
	　　・createGroupメソッドでは
	　　グループを作成（IDはインクリメントなので
	　　Values(null, groupname)みたいになる。）
	
	GroupCreatePanelクラスからグループ名を受けとってグループ作成
	*/
	public boolean createGroup(String GroupName) {
		try (Connection conn = DBUtil.getConnection()) {

            // grouptable テーブルに登録
            String userSql = "INSERT INTO grouptable (GroupId, GroupName) VALUES (?, ?)";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, null);
                userStmt.setString(2, GroupName);
                userStmt.executeUpdate();
            }

            //System.out.println("グループを作成しました！");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            //System.out.println("グループを作成できませんでした！");
            return false;
        }
    }
	
	
	//グループ削除
	public boolean deleteGroup(String GroupName) {
		try (Connection conn = DBUtil.getConnection()) {

            // grouptable テーブルに登録
            String userSql = "DELETE FROM grouptable WHERE GroupName = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, GroupName);
                userStmt.executeUpdate();
            }

            //System.out.println("グループを削除しました！");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            //System.out.println("グループを削除できませんでした！");
            return false;
        }
	}
	
	//JoinGroupPanelクラスからグループ名を受け取ってグループ参加させる
	public boolean joinGroup(String userId,String GroupName) {
		try (Connection conn = DBUtil.getConnection()) {

			String GroupId = getGroupId(GroupName);//GroupNameからGroupIdを取得
			
            // grouptable テーブルに登録
            String userSql = "INSERT INTO groupmember (GroupId, UserId) VALUES (?, ?)";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, GroupId);
                userStmt.setString(2, userId);
                userStmt.executeUpdate();
            }

            //System.out.println("グループに参加しました！");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            //System.out.println("グループに参加できませんでした！");
            return false;
        }
	}
	
	//グループから抜けるメソッド
	public boolean Group(String userId, String GroupName){
		try (Connection conn = DBUtil.getConnection()) {

			String GroupId = getGroupId(GroupName);//GroupNameからGroupIdを取得
			
            // grouptable テーブルに登録
            String userSql = "DELETE FROM groupmember WHERE GroupId = ? AND UserId = ? ";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, GroupId);
                userStmt.setString(2, userId);
                userStmt.executeUpdate();
            }

            //System.out.println("グループから抜けましました！");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            //System.out.println("グループから抜けれませんでした！");
            return false;
        }
	}
	
	
	//GroupNameからGroupIdを取得
	public String getGroupId(String GroupName) {
		try (Connection conn = DBUtil.getConnection()) {

            // GroupIdを取得
            String saltSql = "SELECT GroupId FROM grouptable WHERE GroupName = ?";
            try (PreparedStatement stmt = conn.prepareStatement(saltSql)) {
                stmt.setString(1, GroupName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                	return rs.getString("GroupId");
                } else {
                    return null; // ユーザーが存在しない
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
	}
}
