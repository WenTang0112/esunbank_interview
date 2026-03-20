/*
  Phase 1 - Stored Procedures
  Run after DDL.sql
*/

USE [esunbank];
GO

CREATE OR ALTER PROCEDURE dbo.sp_check_phone_exists
    @PhoneNumber VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT CASE WHEN EXISTS (
        SELECT 1
        FROM dbo.Users
        WHERE PhoneNumber = @PhoneNumber
    ) THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END AS ExistsFlag;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_register_user
    @PhoneNumber VARCHAR(20),
    @PasswordHash NVARCHAR(255),
    @PasswordSalt NVARCHAR(255),
    @UserName NVARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (SELECT 1 FROM dbo.Users WHERE PhoneNumber = @PhoneNumber)
    BEGIN
        THROW 50001, 'Phone number already registered.', 1;
    END;

    INSERT INTO dbo.Users (PhoneNumber, PasswordHash, PasswordSalt, UserName)
    VALUES (@PhoneNumber, @PasswordHash, @PasswordSalt, @UserName);

    SELECT CAST(SCOPE_IDENTITY() AS INT) AS UserId;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_login_user
    @PhoneNumber VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP 1
        UserId,
        PhoneNumber,
        PasswordHash,
        PasswordSalt,
        UserName,
        LastLoginTime,
        IsActive
    FROM dbo.Users
    WHERE PhoneNumber = @PhoneNumber;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_update_last_login
    @UserId INT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE dbo.Users
    SET LastLoginTime = SYSUTCDATETIME(),
        UpdatedAt = SYSUTCDATETIME()
    WHERE UserId = @UserId;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_get_books
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        b.ISBN,
        b.Name,
        b.Author,
        b.Introduction,
        SUM(CASE WHEN i.Status = 'AVAILABLE' THEN 1 ELSE 0 END) AS AvailableCount,
        COUNT(i.InventoryId) AS TotalCount
    FROM dbo.Books b
    LEFT JOIN dbo.Inventory i ON i.ISBN = b.ISBN
    GROUP BY b.ISBN, b.Name, b.Author, b.Introduction
    ORDER BY b.Name;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_get_inventory_by_isbn
    @ISBN VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        InventoryId,
        ISBN,
        StoreTime,
        Status,
        LocationCode,
        UpdatedAt
    FROM dbo.Inventory
    WHERE ISBN = @ISBN
    ORDER BY InventoryId;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_get_borrow_records
    @UserId INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        br.BorrowingRecordId,
        br.UserId,
        u.UserName,
        br.InventoryId,
        i.ISBN,
        b.Name AS BookName,
        br.BorrowingTime,
        br.ReturnTime
    FROM dbo.BorrowingRecords br
    INNER JOIN dbo.Users u ON u.UserId = br.UserId
    INNER JOIN dbo.Inventory i ON i.InventoryId = br.InventoryId
    INNER JOIN dbo.Books b ON b.ISBN = i.ISBN
    WHERE (@UserId IS NULL OR br.UserId = @UserId)
    ORDER BY br.BorrowingTime DESC;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_borrow_book
    @UserId INT,
    @InventoryId BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRAN;

        IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE UserId = @UserId AND IsActive = 1)
        BEGIN
            THROW 50002, 'User does not exist or is inactive.', 1;
        END;

        IF NOT EXISTS (SELECT 1 FROM dbo.Inventory WHERE InventoryId = @InventoryId)
        BEGIN
            THROW 50003, 'Inventory item does not exist.', 1;
        END;

        UPDATE dbo.Inventory
        SET Status = 'BORROWED',
            UpdatedAt = SYSUTCDATETIME()
        WHERE InventoryId = @InventoryId
          AND Status = 'AVAILABLE';

        IF @@ROWCOUNT = 0
        BEGIN
            THROW 50004, 'Inventory is not available for borrowing.', 1;
        END;

        INSERT INTO dbo.BorrowingRecords (UserId, InventoryId)
        VALUES (@UserId, @InventoryId);

        COMMIT TRAN;

        SELECT CAST(SCOPE_IDENTITY() AS BIGINT) AS BorrowingRecordId;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRAN;

        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_return_book
    @UserId INT,
    @InventoryId BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRAN;

        IF NOT EXISTS (
            SELECT 1
            FROM dbo.BorrowingRecords
            WHERE UserId = @UserId
              AND InventoryId = @InventoryId
              AND ReturnTime IS NULL
        )
        BEGIN
            THROW 50005, 'Active borrowing record not found for this user/inventory.', 1;
        END;

        UPDATE dbo.BorrowingRecords
        SET ReturnTime = SYSUTCDATETIME()
        WHERE UserId = @UserId
          AND InventoryId = @InventoryId
          AND ReturnTime IS NULL;

        UPDATE dbo.Inventory
        SET Status = 'PROCESSING',
            UpdatedAt = SYSUTCDATETIME()
        WHERE InventoryId = @InventoryId;

        COMMIT TRAN;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRAN;

        THROW;
    END CATCH;
END;
GO
