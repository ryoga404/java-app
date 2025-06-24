
# Java実践 演習  
## GitHub コマンドリファレンス

### 用語解説  
- リポジトリ：ディレクトリ、フォルダのようなもの  
- ローカル：自身のPC環境  
- リモート：GitHub上の環境  
- クローン：ローカル環境にリモート上のディレクトリ等を持ってくる（コピー）  
- ブランチ：編集履歴の分岐。分岐したブランチは他のブランチの影響を受けないので複数の変更を同時に進めることができる。  
- コミット：ローカルに変更を記録  
- プッシュ：リモートにコミット内容を反映  

---

### リポジトリをクローン  
ローカル環境にリモートリポジトリをクローンする。  
```bash
git clone https://github.com/ユーザ名/リポジトリ名.git
```

ブランチを指定してクローン（指定しない場合はmainブランチがクローンされる）  
```bash
git clone -b ブランチ名 --single-branch https://github.com/ユーザ名/リポジトリ名.git
```
※ --single-branch：指定したブランチだけを取得する

---

### ブランチを切り替え・作成  
- ローカルに存在しないブランチには切り替えれない  
- リモートのブランチを取得  
```bash
git fetch origin
```
- （初回のみ）ローカルにブランチを作成して切り替え  
```bash
git checkout -b ローカルブランチ名 origin/リモートブランチ名
```
（ローカルとリモートのブランチ名は一致することが望ましい）

- （ローカルのみ存在する）リモートにブランチを作成  
```bash
git checkout -b 新ブランチ名
```
commit & pushでリモートに反映

- 既にローカルにブランチが存在する場合  
```bash
git checkout ブランチ名
```

- mainブランチから新しいブランチを分岐させる  
```bash
git checkout main
git pull origin main
git checkout -b 新ブランチ名
```

---

### ファイル／ディレクトリの削除  
```bash
git rm ファイルパス
git rm -r ディレクトリパス
```

---

### ローカルでの変更をリモートに反映  
```bash
git commit -m "メッセージ（例：〇〇を作成等）"
git push origin ブランチ名
```

---

### ローカルのファイルやディレクトリをリモートへ  
```bash
git add パス
git commit -m "メッセージ"
git push origin ブランチ名
```

---

### 間違って上書きや削除、編集した場合の復元  

#### パターン1  
ローカルで編集し、addもcommitもしていない場合  
```bash
git restore パス
```

#### パターン2  
ローカルで編集し、addまでした場合（ローカルに編集履歴を保存した状態）  
```bash
git restore --staged パス
git restore パス
```

#### パターン3  
commit & push済みを元に戻したい場合  
```bash
git revert --no-commit 戻したい時点のコミットID..HEAD
git commit -m "メッセージ"
git push origin ブランチ名
```

---

### ブランチ関連  

| No. | 内容                          | コマンド                        |
|-----|-------------------------------|-------------------------------|
| 1   | ローカルブランチ一覧取得           | `git branch`                   |
| 2   | リモートブランチ一覧取得           | `git branch -r`                |
| 3   | ローカル＆リモート両方一覧取得      | `git branch -a`                |
| 4   | ローカルブランチ作成（mainから分岐） | `git branch ブランチ名`          |
| 5   | ローカルブランチ作成＋切り替え        | `git checkout -b ブランチ名`     |
| 6   | ローカルブランチ削除（マージ済み）      | `git branch -d ブランチ名`         |
| 7   | ローカルブランチ削除（強制）         | `git branch -D ブランチ名`         |
| 8   | ローカルブランチ切り替え             | `git checkout ブランチ名`          |
| 9   | リモートブランチ削除            | `git push origin --delete ブランチ名`  |


---

## 一般的な作業の流れ

1. （初回）ローカルにリポジトリをクローン  
```bash
git clone https://github.com/ユーザ名/リポジトリ名.git
```

2. リモートでの変更をローカルに反映  
```bash
git pull origin ブランチ名
```

3. ファイル等編集

4. ローカルに変更記録  
```bash
git add 編集したファイルパス  # または git add .
git commit -m "メッセージ"
```

5. リモートにアップロード  
```bash
git push origin ブランチ名
```

---

### 追記  
作業を始める前に自分がどのブランチで作業しているのかを確認する  
```bash
git branch
```
（現在のブランチには`*`がついている）

作業予定のブランチと違う場合は切り替え  
```bash
git checkout ブランチ名
```

切り替えた後は最新の状態にする  
```bash
git pull origin ブランチ名
```
