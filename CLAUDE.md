# タスク管理API

JWT認証とPostgreSQLを使用したタスク管理のSpring Boot REST APIです。

## 技術スタック

- **Java 21** / Spring Boot 4.0.3
- **Spring Security** + JWT（jjwt 0.12.6）
- **Spring Data JPA** + PostgreSQL
- **Lombok**
- **Gradle**（Kotlin DSL）

## プロジェクト構成

```
src/main/java/com/guanyiping/task/management/
├── config/           # SecurityConfig
├── controller/       # AuthController, TaskController
├── dto/              # AuthRequest, AuthResponse
├── entity/           # User, Task
├── repository/       # UserRepository, TaskRepository
├── security/         # JwtFilter, JwtUtil, UserDetailsServiceImpl
└── service/          # AuthService, TaskService
```

## APIエンドポイント

### 認証（認証不要）
| メソッド | パス | 説明 |
|---------|------|------|
| POST | `/auth/register` | ユーザー登録 |
| POST | `/auth/login` | ログイン（JWTトークンを返す） |

### タスク（JWTトークン必須）
| メソッド | パス | 説明 |
|---------|------|------|
| GET | `/tasks` | タスク一覧取得 |
| GET | `/tasks/{id}` | タスク詳細取得 |
| POST | `/tasks` | タスク作成 |
| PUT | `/tasks/{id}` | タスク更新 |
| PATCH | `/tasks/{id}/complete` | タスク完了にする |
| DELETE | `/tasks/{id}` | タスク削除 |

## データベース設定

ローカルのPostgreSQLを使用：
- ホスト：`localhost:5432`
- データベース名：`mydb`
- DDL自動設定：`update`（起動時にスキーマを自動更新）

## セキュリティ

- JWTトークンの有効期限：24時間
- `/auth/**` は認証不要
- その他のエンドポイントはすべて `Authorization: Bearer <token>` ヘッダーが必要
- パスワードはBCryptでハッシュ化
- ステートレスセッション（CSRFなし）

## ビルド・実行

```bash
./gradlew bootRun    # アプリ起動
./gradlew test       # テスト実行
./gradlew build      # ビルド
```

## 設定ファイル

`src/main/resources/application.properties` の主な設定：
- `jwt.secret` — HMAC-SHA秘密鍵（32文字以上）
- `jwt.expiration` — トークン有効期限（ミリ秒）
- `spring.datasource.*` — データベース接続情報

## テスト

現在はSpringコンテキストの起動確認テストのみ実装済みです。
サービス・コントローラー・JWT認証フローのユニットテストおよび統合テストは未実装です。