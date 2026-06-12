-- One-time production upgrade for book.original_price.
-- MySQL 5.6 compatible. Run only after confirming the book.original_price column does not exist.

ALTER TABLE book
    ADD COLUMN original_price DECIMAL(10, 2) DEFAULT NULL COMMENT 'original price' AFTER price;

UPDATE book
SET original_price = price
WHERE original_price IS NULL;
