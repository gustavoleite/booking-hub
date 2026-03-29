-- V2: Add active flags and unique constraints for RFC-007

-- Add active column to establishments
ALTER TABLE tb_establishments ADD COLUMN active BOOLEAN DEFAULT TRUE;
ALTER TABLE tb_establishments ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE tb_establishments ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add phone column to establishments
ALTER TABLE tb_establishments ADD COLUMN phone VARCHAR(20);

-- Make CNPJ unique
ALTER TABLE tb_establishments ADD CONSTRAINT uk_establishment_cnpj UNIQUE (cnpj);

-- Add active column to provided services
ALTER TABLE tb_provided_services ADD COLUMN active BOOLEAN DEFAULT TRUE;

-- Add active and audit columns to professionals
ALTER TABLE tb_professionals ADD COLUMN active BOOLEAN DEFAULT TRUE;
ALTER TABLE tb_professionals ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE tb_professionals ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add unique constraint to business hours
ALTER TABLE tb_business_hours ADD CONSTRAINT uk_establishment_day UNIQUE (establishment_id, day_of_week);
