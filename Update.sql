-- 使用するデータベースを明示
USE kakeibo;

-- データベース自体の文字コード変更
ALTER DATABASE kakeibo CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- 各テーブルの文字コード変換
ALTER TABLE category     CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE groupmember  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE grouptable   CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE record       CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE salt         CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE session      CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE user         CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
