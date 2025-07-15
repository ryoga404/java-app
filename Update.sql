-- データベースが存在しなければ作成（任意）
CREATE DATABASE IF NOT EXISTS kakeibo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- 使用するデータベース指定
USE kakeibo;

-- ユーザテーブル
CREATE TABLE IF NOT EXISTS User (
    UserId VARCHAR(20) NOT NULL PRIMARY KEY,
    Salt VARCHAR(64) NOT NULL,
    HashedPassword VARCHAR(64) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 家計簿レコードテーブル
CREATE TABLE IF NOT EXISTS Record (
    RecordId INT AUTO_INCREMENT PRIMARY KEY,
    UserId VARCHAR(20) NOT NULL,
    Date DATE NOT NULL,
    CategoryId INT NOT NULL,
    Type ENUM('INCOME', 'EXPENSE') NOT NULL,
    Amount INT NOT NULL,
    Memo VARCHAR(100),
    FOREIGN KEY (UserId) REFERENCES User(UserId)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- カテゴリテーブル
CREATE TABLE IF NOT EXISTS Category (
    CategoryId INT AUTO_INCREMENT PRIMARY KEY,
    CategoryName VARCHAR(50) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- グループテーブル
CREATE TABLE IF NOT EXISTS GroupTable (
    GroupId INT AUTO_INCREMENT PRIMARY KEY,
    GroupName VARCHAR(50) NOT NULL,
    OwnerUserId VARCHAR(20) NOT NULL,
    FOREIGN KEY (OwnerUserId) REFERENCES User(UserId)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- グループメンバーテーブル
CREATE TABLE IF NOT EXISTS GroupMember (
    GroupMemberId INT AUTO_INCREMENT PRIMARY KEY,
    GroupId INT NOT NULL,
    UserId VARCHAR(20) NOT NULL,
    FOREIGN KEY (GroupId) REFERENCES GroupTable(GroupId),
    FOREIGN KEY (UserId) REFERENCES User(UserId)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- もし salt や session テーブルも使っているなら仮に作成
CREATE TABLE IF NOT EXISTS Salt (
    UserId VARCHAR(20) NOT NULL PRIMARY KEY,
    SaltValue VARCHAR(64) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS Session (
    SessionId VARCHAR(64) PRIMARY KEY,
    UserId VARCHAR(20) NOT NULL,
    Expiry DATETIME NOT NULL,
    FOREIGN KEY (UserId) REFERENCES User(UserId)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 既存のテーブルがある場合も、文字コードを統一
ALTER DATABASE kakeibo CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

ALTER TABLE Category     CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE GroupMember  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE GroupTable   CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE Record       CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE Salt         CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE Session      CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE User         CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

