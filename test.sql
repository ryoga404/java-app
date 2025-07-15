-- ユーザテーブル
CREATE TABLE User (
    UserId VARCHAR(20) NOT NULL PRIMARY KEY,
    Salt VARCHAR(64) NOT NULL,
    HashedPassword VARCHAR(64) NOT NULL
);

-- 家計簿データテーブル（簡易版）
CREATE TABLE Record (
    RecordId INT AUTO_INCREMENT PRIMARY KEY,
    UserId VARCHAR(20) NOT NULL,
    Date DATE NOT NULL,
    CategoryId INT NOT NULL,
    Type ENUM('INCOME', 'EXPENSE') NOT NULL,
    Amount INT NOT NULL,
    Memo VARCHAR(100),
    FOREIGN KEY (UserId) REFERENCES User(UserId)
);

-- カテゴリテーブル（例）
CREATE TABLE Category (
    CategoryId INT AUTO_INCREMENT PRIMARY KEY,
    CategoryName VARCHAR(50) NOT NULL
);

-- グループ関連テーブル（参考）
CREATE TABLE GroupTable (
    GroupId INT AUTO_INCREMENT PRIMARY KEY,
    GroupName VARCHAR(50) NOT NULL,
    OwnerUserId VARCHAR(20) NOT NULL,
    FOREIGN KEY (OwnerUserId) REFERENCES User(UserId)
);

CREATE TABLE GroupMember (
    GroupMemberId INT AUTO_INCREMENT PRIMARY KEY,
    GroupId INT NOT NULL,
    UserId VARCHAR(20) NOT NULL,
    FOREIGN KEY (GroupId) REFERENCES GroupTable(GroupId),
    FOREIGN KEY (UserId) REFERENCES User(UserId)
);
