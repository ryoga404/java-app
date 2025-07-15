-- データベースの文字コードを utf8mb4 に変更
ALTER DATABASE kakeibo CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- 各テーブルの文字コードを utf8mb4 に変更
-- 必要に応じて以下を編集（テーブル名は実際のものに置換）

ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE records CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE categories CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE groups CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE members CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE sessions CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 他のテーブルがある場合は同様に追加
