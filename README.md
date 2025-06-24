# Java実践　演習
## GitHub コマンドリファレンス
### 用語解説
リポジトリ：ディレクトリ、フォルダのようなもの <br>
ローカル：自身のPC環境 <br>
リモート：GitHub上の環境 <br>
クローン：ローカル環境にリモート上のディレクトリ等を持ってくる（コピー）<br>
ブランチ：編集履歴の分岐。分岐したブランチは他のブランチの影響を受けないので複数の変更を同時に進めることができる。<br>
コミット：ローカルに変更を記録 <br>
プッシュ：リモートにコミット内容を反映

### リポジトリをクローン
ローカル環境にリモートリポジトリをクローンする。<br>
`git clone https://github.com/ユーザ名/リポジトリ名.git`

ブランチを指定してクローン（指定しない場合はmainブランチがクローンされる）<br>
`git clone -b ブランチ名 --single-branch https://github.com/ユーザ名/リポジトリ名.git `<br>
--single-branch：指定したブランチだけを取得する

### ブランチを切り替え・作成
前提としてローカルに存在しないブランチには切り替えれない！<br>
(例：クローン時にdevブランチを指定→mainブランチはローカルにないため切り替えれない)

リモートのブランチを取得<br>
`git fetch origin` （この時点ではローカルの変更はない）<br>

（初回のみ）ローカルにブランチを作っておく<br>
`git checkout -b ローカルブランチ名 origin/リモートブランチ名` <br>
（基本的にローカルブランチ名とリモートブランチ名は一致することが望ましい）

（ローカルのみ存在する）リモートにブランチを作成<br>
`git checkout -b 新ブランチ名` <br>
その後、commit & pushでリモートに反映させる

既にローカルにブランチが存在する<br>
`git checkout ブランチ名`<br>

通常、mainブランチから分岐させていく<br>
mainブランチから新しいブランチを分岐させる<br>
`git checkout main`  (mainブランチに切り替え)<br>
`git pull origin main` (mainブランチを最新に)<br>
`git checkout -b 新ブランチ名`

### ファイル／ディレクトリの削除
`git rm  ファイルパス` <br>
`git rm -r ディレクトリパス`

### ローカルでの変更をリモートに反映
`git commit -m "メッセージ（例：〇〇を作成等）"` <br>
`git push origin ブランチ名`

### ローカルのファイルやディレクトリをリモートへ
`git add パス`<br>
`git commit -m "メッセージ"` <br>
`git push origin ブランチ名` <br>

間違って上書きや削除、その他編集した場合でも復元できる

### 削除をリストア
パターン１ ローカルで編集し、addもコミットもしていない<br>
`git restore パス` <br>

パターン２　ローカルで編集し、addまでした。（ローカルに編集履歴を保存した状態）<br>
`git restore --staged パス` <br>
`git restore パス` <br>

パターン３　完全にリモートへ反映させてしまった(commit & push 済み) <br>
`git revert --no-commit 戻したい時点のコミットID..HEAD`　<br>
`git commit -m "メッセージ" ` <br>
`git push origin ブランチ名`

## 一般的な作業の流れ
１、（初回）ローカルにリポジトリをクローン<br>
`git clone https://github.com/ユーザ名/リポジトリ名.git` <br>
２、リモートでの変更をローカルに反映<br>
`git pull origin ブランチ名` <br>
３、ファイル等編集<br>
４、ローカルに変更記録<br>
`git add 編集したファイルパス` or `git add .` <br>
`git commit -m "メッセージ"` <br>
５、リモートにアップロード <br>
`git push origin ブランチ名`

### 追記
作業を始める前に自分がどのブランチで作業しているのかを確認する！<br>
`git branch` (現在のブランチには*がついている)<br>
作業予定のブランチと違う場合は`git checkout ブランチ名`をする。<br>
(ブランチを切り替えた後はpullで最新の状態に！)