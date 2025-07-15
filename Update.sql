-- データベースが存在する場合は削除
DROP DATABASE IF EXISTS kakeibo;

-- UTF-8（utf8mb4）でデータベース作成
CREATE DATABASE kakeibo CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 使用するデータベースを指定
USE kakeibo;

-- ユーザーテーブル
CREATE TABLE user (
    UserId VARCHAR(50) PRIMARY KEY,
    HashedPassword VARCHAR(255) NOT NULL
) CHARACTER SET utf8mb4;

-- ソルトテーブル
CREATE TABLE salt (
    UserId VARCHAR(50) PRIMARY KEY,
    Salt VARCHAR(255) NOT NULL,
    FOREIGN KEY (UserId) REFERENCES user(UserId)
) CHARACTER SET utf8mb4;

-- セッションテーブル
CREATE TABLE session (
    SessionId VARCHAR(255) PRIMARY KEY,
    UserId VARCHAR(50),
    LoginTime DATETIME NOT NULL,
    FOREIGN KEY (UserId) REFERENCES user(UserId)
) CHARACTER SET utf8mb4;

-- カテゴリテーブル
CREATE TABLE category (
    CategoryId INT AUTO_INCREMENT PRIMARY KEY,
    CategoryName VARCHAR(100) NOT NULL
) CHARACTER SET utf8mb4;

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
) CHARACTER SET utf8mb4;

-- グループテーブル
CREATE TABLE grouptable (
    GroupId INT AUTO_INCREMENT PRIMARY KEY,
    GroupName VARCHAR(100) NOT NULL
) CHARACTER SET utf8mb4;

-- グループメンバーテーブル
CREATE TABLE groupmember (
    GroupId INT,
    UserId VARCHAR(50),
    PRIMARY KEY (GroupId, UserId),
    FOREIGN KEY (GroupId) REFERENCES grouptable(GroupId),
    FOREIGN KEY (UserId) REFERENCES user(UserId)
) CHARACTER SET utf8mb4;

-- 初期カテゴリデータ挿入
INSERT INTO category (CategoryName) VALUES
('家賃'),
('電気代'),
('水道代'),
('ガス代'),
('食費'),
('雑費'),
('通信費'),
('その他');

-- 既存データの文字化けを直すためのUPDATE文（latin1 → utf8mb4変換）
-- ※ 新規作成直後は必要ありません。既存の古いデータがある場合のみ実行してください。

UPDATE `category` SET `CategoryName` = CONVERT(BINARY CONVERT(`CategoryName` USING latin1) USING utf8mb4);
UPDATE `groupmember` SET `UserId` = CONVERT(BINARY CONVERT(`UserId` USING latin1) USING utf8mb4);
UPDATE `grouptable` SET `GroupName` = CONVERT(BINARY CONVERT(`GroupName` USING latin1) USING utf8mb4);
UPDATE `record` SET `UserId` = CONVERT(BINARY CONVERT(`UserId` USING latin1) USING utf8mb4);
UPDATE `record` SET `Type` = CONVERT(BINARY CONVERT(`Type` USING latin1) USING utf8mb4);
UPDATE `record` SET `Memo` = CONVERT(BINARY CONVERT(`Memo` USING latin1) USING utf8mb4);
UPDATE `salt` SET `UserId` = CONVERT(BINARY CONVERT(`UserId` USING latin1) USING utf8mb4);
UPDATE `salt` SET `Salt` = CONVERT(BINARY CONVERT(`Salt` USING latin1) USING utf8mb4);
UPDATE `session` SET `SessionId` = CONVERT(BINARY CONVERT(`SessionId` USING latin1) USING utf8mb4);
UPDATE `session` SET `UserId` = CONVERT(BINARY CONVERT(`UserId` USING latin1) USING utf8mb4);
UPDATE `user` SET `UserId` = CONVERT(BINARY CONVERT(`UserId` USING latin1) USING utf8mb4);
UPDATE `user` SET `HashedPassword` = CONVERT(BINARY CONVERT(`HashedPassword` USING latin1) USING utf8mb4);

INSERT INTO category (CategoryName) VALUES
('家賃'),
('電気代'),
('水道代'),
('ガス代'),
('食費'),
('雑費'),
('通信費'),
('その他');
