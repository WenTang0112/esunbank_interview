/*
  Phase 1 - DDL
  Target: SQL Server (MSSQL)
*/

USE [esunbank];
GO

/* Drop in dependency order for re-runnable setup */
IF OBJECT_ID('dbo.BorrowingRecords', 'U') IS NOT NULL DROP TABLE dbo.BorrowingRecords;
IF OBJECT_ID('dbo.Inventory', 'U') IS NOT NULL DROP TABLE dbo.Inventory;
IF OBJECT_ID('dbo.Books', 'U') IS NOT NULL DROP TABLE dbo.Books;
IF OBJECT_ID('dbo.Users', 'U') IS NOT NULL DROP TABLE dbo.Users;
GO

CREATE TABLE dbo.Users (
    UserId INT IDENTITY(1,1) PRIMARY KEY,
    PhoneNumber VARCHAR(20) NOT NULL,
    PasswordHash NVARCHAR(255) NOT NULL,
    PasswordSalt NVARCHAR(255) NOT NULL,
    UserName NVARCHAR(100) NOT NULL,
    RegistrationTime DATETIME2(0) NOT NULL CONSTRAINT DF_Users_RegistrationTime DEFAULT SYSUTCDATETIME(),
    LastLoginTime DATETIME2(0) NULL,
    IsActive BIT NOT NULL CONSTRAINT DF_Users_IsActive DEFAULT (1),
    CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Users_UpdatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_Users_PhoneNumber UNIQUE (PhoneNumber)
);
GO

CREATE TABLE dbo.Books (
    ISBN VARCHAR(20) NOT NULL PRIMARY KEY,
    Name NVARCHAR(200) NOT NULL,
    Author NVARCHAR(100) NOT NULL,
    Introduction NVARCHAR(2000) NULL,
    CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Books_CreatedAt DEFAULT SYSUTCDATETIME()
);
GO

CREATE TABLE dbo.Inventory (
    InventoryId BIGINT IDENTITY(1,1) PRIMARY KEY,
    ISBN VARCHAR(20) NOT NULL,
    StoreTime DATETIME2(0) NOT NULL CONSTRAINT DF_Inventory_StoreTime DEFAULT SYSUTCDATETIME(),
    Status VARCHAR(20) NOT NULL,
    LocationCode NVARCHAR(30) NULL,
    UpdatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Inventory_UpdatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Inventory_Books FOREIGN KEY (ISBN) REFERENCES dbo.Books(ISBN),
    CONSTRAINT CK_Inventory_Status CHECK (Status IN ('AVAILABLE', 'BORROWED', 'PROCESSING', 'LOST', 'DAMAGED', 'DISCARDED'))
);
GO

CREATE TABLE dbo.BorrowingRecords (
    BorrowingRecordId BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    InventoryId BIGINT NOT NULL,
    BorrowingTime DATETIME2(0) NOT NULL CONSTRAINT DF_BorrowingRecords_BorrowingTime DEFAULT SYSUTCDATETIME(),
    ReturnTime DATETIME2(0) NULL,
    CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_BorrowingRecords_CreatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_BorrowingRecords_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId),
    CONSTRAINT FK_BorrowingRecords_Inventory FOREIGN KEY (InventoryId) REFERENCES dbo.Inventory(InventoryId)
);
GO

/* Fast lookup indexes requested in spec style */
CREATE INDEX IX_BorrowingRecords_UserId ON dbo.BorrowingRecords(UserId);
CREATE INDEX IX_BorrowingRecords_InventoryId ON dbo.BorrowingRecords(InventoryId);
GO

/* Ensure one active borrowing record per inventory */
CREATE UNIQUE INDEX UX_BorrowingRecords_ActiveInventory
ON dbo.BorrowingRecords(InventoryId)
WHERE ReturnTime IS NULL;
GO
