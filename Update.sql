-- ★ MySQLクライアントやアプリ側で必須：接続時の文字コード指定
-- これをSQLの最初に入れておくと、クライアント側がutf8mb4を使うようになります
SET NAMES utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_database = utf8mb4;
SET character_set_server = utf8mb4;

-- データベース初期化
DROP DATABASE IF EXISTS kakeibo;
CREATE DATABASE kakeibo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kakeibo;

-- ユーザーテーブル
CREATE TABLE user (
    UserId VARCHAR(50) PRIMARY KEY,
    HashedPassword VARCHAR(255) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ソルトテーブル
CREATE TABLE salt (
    UserId VARCHAR(50) PRIMARY KEY,
    Salt VARCHAR(255) NOT NULL,
    FOREIGN KEY (UserId) REFERENCES user(UserId)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- セッションテーブル
CREATE TABLE session (
    SessionId VARCHAR(255) PRIMARY KEY,
    UserId VARCHAR(50),
    LoginTime DATETIME NOT NULL,
    FOREIGN KEY (UserId) REFERENCES user(UserId)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- カテゴリテーブル
CREATE TABLE category (
    CategoryId INT AUTO_INCREMENT PRIMARY KEY,
    CategoryName VARCHAR(100) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- レコードテーブル
CREATE TABLE record (
    RecordId INT AUTO_INCREMENT PRIMARY KEY,
    UserId VARCHAR(50),
    Date DATE NOT NULL,
    CategoryId INT,
    Type VARCHAR(10),
    Amount INT,
    Memo TEXT,
    FOREIGN KEY (UserId) REFERENCES user(UserId),
    FOREIGN KEY (CategoryId) REFERENCES category(CategoryId)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- グループテーブル
CREATE TABLE grouptable (
    GroupId INT AUTO_INCREMENT PRIMARY KEY,
    GroupName VARCHAR(100) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- グループメンバーテーブル
CREATE TABLE groupmember (
    GroupId INT,
    UserId VARCHAR(50),
    PRIMARY KEY (GroupId, UserId),
    FOREIGN KEY (GroupId) REFERENCES grouptable(GroupId),
    FOREIGN KEY (UserId) REFERENCES user(UserId)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
