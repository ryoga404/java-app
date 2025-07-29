-- 既存データベース削除（存在する場合）
DROP DATABASE IF EXISTS kakeibo;

-- データベース作成（UTF8MB4 + general_ci）
CREATE DATABASE kakeibo
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE kakeibo;

-- userテーブル
CREATE TABLE user (
  UserId VARCHAR(50) NOT NULL PRIMARY KEY,
  HashedPassword VARCHAR(255) NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- saltテーブル
CREATE TABLE salt (
  UserId VARCHAR(50) NOT NULL PRIMARY KEY,
  Salt VARCHAR(255) NOT NULL,
  FOREIGN KEY (UserId) REFERENCES user(UserId) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- sessionテーブル
CREATE TABLE session (
  SessionId VARCHAR(255) NOT NULL PRIMARY KEY,
  UserId VARCHAR(50),
  LoginTime DATETIME NOT NULL,
  FOREIGN KEY (UserId) REFERENCES user(UserId) ON DELETE SET NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- grouptableテーブル
CREATE TABLE grouptable (
  GroupId INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  GroupName VARCHAR(100) NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- groupmemberテーブル
CREATE TABLE groupmember (
  GroupId INT(11) NOT NULL,
  UserId VARCHAR(50) NOT NULL,
  PRIMARY KEY (GroupId, UserId),
  FOREIGN KEY (GroupId) REFERENCES grouptable(GroupId) ON DELETE CASCADE,
  FOREIGN KEY (UserId) REFERENCES user(UserId) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- categoryテーブル
CREATE TABLE category (
  CategoryId INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  CategoryName VARCHAR(100) NOT NULL,
  CategoryType ENUM('IN', 'OUT') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- recordテーブル
CREATE TABLE record (
  RecordId INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  UserId VARCHAR(50),
  Date DATE NOT NULL,
  CategoryId INT(11),
  Type VARCHAR(10),
  Amount INT(11),
  Memo TEXT,
  FOREIGN KEY (UserId) REFERENCES user(UserId) ON DELETE SET NULL,
  FOREIGN KEY (CategoryId) REFERENCES category(CategoryId) ON DELETE SET NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 念のため、全テーブルの文字コードを再設定（保険的処理）
ALTER TABLE `category`     CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `groupmember`  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `grouptable`   CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `record`       CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `salt`         CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `session`      CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `user`         CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 初期データ挿入
INSERT INTO category (CategoryId, CategoryName, CategoryType) VALUES
  (1, '家賃', 'OUT'),
  (2, '電気代', 'OUT'),
  (3, '水道代', 'OUT'),
  (4, 'ガス代', 'OUT'),
  (5, '交通費', 'OUT'),
  (6, '食費', 'OUT'),
  (7, '通信費', 'OUT'),
  (8, '被服費', 'OUT'),
  (9, '医療費', 'OUT'),
  (10, 'その他_支出', 'OUT'),
  (11, '給与', 'IN'),
  (12, '臨時収入', 'IN'),
  (13, 'その他_収入', 'IN');
