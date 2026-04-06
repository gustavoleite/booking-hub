-- V3: Add city and state columns to establishment address
ALTER TABLE tb_establishments ADD COLUMN city VARCHAR(255);
ALTER TABLE tb_establishments ADD COLUMN state VARCHAR(100);
