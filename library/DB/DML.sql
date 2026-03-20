/*
  Phase 1 - DML seed data
  Run after DDL.sql
*/

USE [esunbank];
GO

/* Books */
INSERT INTO dbo.Books (ISBN, Name, Author, Introduction)
VALUES
('9789863120001', N'Clean Code', N'Robert C. Martin', N'一本關於如何撰寫可維護程式碼的經典著作。'),
('9789862011119', N'Effective Java', N'Joshua Bloch', N'Java 開發者最佳實務，涵蓋語言與 API 使用技巧。'),
('9789864342471', N'Design Patterns', N'Erich Gamma et al.', N'介紹常見軟體設計模式與應用情境。'),
('9789864769155', N'Spring in Action', N'Craig Walls', N'Spring 生態系統與實作導覽。');
GO

/* Inventory */
INSERT INTO dbo.Inventory (ISBN, Status, LocationCode)
VALUES
('9789863120001', 'AVAILABLE', N'A-01'),
('9789863120001', 'AVAILABLE', N'A-02'),
('9789862011119', 'AVAILABLE', N'B-01'),
('9789862011119', 'AVAILABLE', N'B-02'),
('9789864342471', 'AVAILABLE', N'C-01'),
('9789864769155', 'AVAILABLE', N'D-01');
GO

/* Demo user: phone 0912345678, password hash/salt placeholders to be replaced by backend registration flow */
INSERT INTO dbo.Users (PhoneNumber, PasswordHash, PasswordSalt, UserName)
VALUES
('0912345678', N'DEMO_HASH', N'DEMO_SALT', N'demo_user');
GO
