# 圖書借閱系統

玉山銀行後端工程師 Java 實作題

## 技術選型

- **前端**：Vue 3 + Vue Router 4 + Axios + Vite
- **後端**：Spring Boot 4.0.3 / Java 25 / Maven
- **資料庫**：SQL Server，全部透過 Stored Procedure 存取
- **認證**：HMAC-SHA256 自製 Token + Interceptor
- **密碼**：SHA-256 加鹽雜湊

## 架構

```
Vue 3 (Web) ── REST API ──► Spring Boot (App) ── SP ──► SQL Server (DB)
```

後端分四層：

- **展示層** `controller` — RESTful 端點
- **業務層** `service` — 商業邏輯、Transaction
- **資料層** `repository` — SimpleJdbcCall 呼叫 SP
- **共用層** `config` / `util` / `security` / `dto` — XSS Filter、密碼工具、Token 驗證

## 專案結構

```
library/
├── DB/
│   ├── DDL.sql              # 建表
│   ├── DML.sql              # 種子資料
│   └── StoredProcedures.sql # 9 支 SP
├── frontend/src/
│   ├── api/                 # HTTP 封裝
│   ├── router/              # 路由守衛
│   ├── store/               # auth state
│   └── views/               # 頁面元件
├── src/main/java/com/example/
│   ├── controller/          # 3 個 Controller
│   ├── service/             # 3 個 Service
│   ├── repository/          # 4 個 Repository (介面 + impl)
│   ├── security/            # AuthTokenUtil, AuthInterceptor
│   ├── config/              # CORS, WebConfig, XssFilter
│   ├── dto/                 # Request / Response
│   ├── entity/              # JPA Entity
│   ├── enums/               # InventoryStatus
│   └── util/                # PasswordUtil
└── src/test/java/           # 單元測試
```

## 啟動方式

### 1. 資料庫

在 SQL Server 依序執行：

```sql
CREATE DATABASE esunbank;
```

然後依序執行 `DB/DDL.sql` → `DB/StoredProcedures.sql` → `DB/DML.sql`

> 連線預設用 Windows 整合驗證，可在 `application.properties` 調整。

### 2. 後端

```bash
cd library
..\mvnw.cmd spring-boot:run
# http://localhost:8080
```

### 3. 前端

```bash
cd library/frontend
npm install
npm run dev
# http://localhost:5173
```

## API

| Method | Path | 說明 | 需登入 |
|--------|------|------|--------|
| POST | `/api/users/register` | 註冊 | ✗ |
| POST | `/api/users/login` | 登入 | ✗ |
| GET | `/api/books` | 書籍列表（含可借數量） | ✓ |
| GET | `/api/books/{isbn}/inventory` | 館藏明細 | ✓ |
| POST | `/api/borrow` | 借書 | ✓ |
| POST | `/api/return` | 還書 | ✓ |
| GET | `/api/borrow/records` | 我的借閱紀錄 | ✓ |

## 資料表

| 表 | PK | 重點 |
|----|----|------|
| Users | UserId | 手機唯一、密碼加鹽雜湊、紀錄最後登入時間 |
| Books | ISBN | 書名、作者、簡介 |
| Inventory | InventoryId | 狀態：在庫/出借中/整理中/遺失/損毀/廢棄 |
| BorrowingRecords | BorrowingRecordId | UserId、InventoryId 各建 index |

## 主要設計決策

- **借還書 Transaction**：SP 內 `BEGIN TRAN` + `SET XACT_ABORT ON`，搭配 Spring `@Transactional` 雙層保護
- **同一館藏不可重複借閱**：BorrowingRecords 上建立 filtered unique index（`WHERE ReturnTime IS NULL`）
- **防 SQL Injection**：全部走 SP 參數化，Java 端用 `SimpleJdbcCall`
- **防 XSS**：自製 `XssFilter` 清洗 parameter / header，並設定 CSP、X-Frame-Options 等 response header
- **認證**：自製 HMAC-SHA256 Token，透過 `AuthInterceptor` 攔截需登入的路由

## 單元測試

使用 JUnit 5 + Mockito，針對 Service 層撰寫測試，共 12 個案例全數通過。

**UserServiceTest**（7 cases）— 驗證註冊流程（成功、手機重複、格式錯誤、密碼確實雜湊儲存）與登入流程（成功、密碼錯誤、帳號不存在）

**BorrowServiceTest**（5 cases）— 驗證借書（成功、庫存不可借時拋例外）與還書（成功、非本人借閱、已歸還不可重複還）

```bash
..\mvnw.cmd test
```

## Demo 帳號

手機：`0912345678`　密碼：`0912345678`
