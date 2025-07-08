//package jp.ac.kcs.swing.library;
package library;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DB {
	//DBアクセス＆ユーザー名とパスワード
	private static final String DATABASE_NAME = "library";
	private static final String PROPATIES = "?characterEncoding=UTF-8&&useSSL=false";
	private static final String URL = "jdbc:mysql://localhost/" + DATABASE_NAME + PROPATIES;
	private static final String USER = "root";
	private static final String PASS = "";
	
	Connection conn = null;
	PreparedStatement pstmt = null;
	
	//本登録
	void insertBook(String code, String title) {
		String sql = "INSERT INTO book(code, title, is_lent) VALUES(?, ?, false)";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USER, PASS);
			//System.out.println("接続成功");
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, code);
			pstmt.setString(2, title);
			pstmt.executeUpdate();
		}catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			
		}
	}
	
	//会員の登録
	void insertMember(String id, String name) {
		String sql = "INSERT INTO member(id, name) VALUES(?, ?)";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USER, PASS);
			//System.out.println("接続成功");
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, name);
			pstmt.executeUpdate();
		}catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			
		}
	}
	
	//本を貸出状態に更新する処理 
	void lendBook(String memberId, String bookCode) {
		String sql = "UPDATE book SET is_lent = true WHERE code = ?";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USER, PASS);
			//System.out.println("接続成功");
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, bookCode);
			pstmt.executeUpdate();
		}catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			
		}
	}

    //本の返却済み
	void returnBook(String code) {
		String sql = "UPDATE book SET is_lent = false WHERE code = ?";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USER, PASS);
			//System.out.println("接続成功");
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,code);
			pstmt.executeUpdate();
		}catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			
		}
	}
	
	//貸し出し中の本リスト
	List<String> getLenBooks() {
		List<String> books = new ArrayList<>();
		String sql = "select title from book where is_lent=true";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USER, PASS);
			
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				books.add(rs.getString("title"));
			}
		}catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			
		}
		return books;
	}

}
