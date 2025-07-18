import java.sql.Connection;
import java.sql.PreparedStatement;
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
	public void deleteGroup(String GroupName) {
		
	}
	
	//JoinGroupPanelクラスからグループ名を受け取ってグループ参加させる
	public void joinGroup(String userId,String GroupName) {
		
	}
	
	//グループから抜けるメソッド
	//public void　Group(String GroupName){
}
