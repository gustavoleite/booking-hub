CREATE TABLE tb_establishments (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    cnpj VARCHAR(20),
    description TEXT,
    street VARCHAR(255),
    number VARCHAR(20),
    zip_code VARCHAR(20),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8)
);

CREATE TABLE tb_establishment_photos (
    establishment_id UUID NOT NULL REFERENCES tb_establishments(id),
    photo_url TEXT NOT NULL
);

CREATE TABLE tb_business_hours (
    id UUID PRIMARY KEY,
    establishment_id UUID NOT NULL REFERENCES tb_establishments(id),
    day_of_week INT NOT NULL,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL
);

CREATE TABLE tb_provided_services (
    id UUID PRIMARY KEY,
    establishment_id UUID NOT NULL REFERENCES tb_establishments(id),
    title VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE tb_professionals (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    bio TEXT,
    avatar_url TEXT
);

CREATE TABLE tb_professional_specialties (
    professional_id UUID NOT NULL REFERENCES tb_professionals(id),
    specialty VARCHAR(255) NOT NULL
);

CREATE TABLE tb_affiliations (
    id UUID PRIMARY KEY,
    establishment_id UUID NOT NULL REFERENCES tb_establishments(id),
    professional_id UUID NOT NULL REFERENCES tb_professionals(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE tb_work_schedules (
    id UUID PRIMARY KEY,
    affiliation_id UUID NOT NULL REFERENCES tb_affiliations(id),
    day_of_week INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

CREATE TABLE tb_service_offerings (
    id UUID PRIMARY KEY,
    affiliation_id UUID NOT NULL REFERENCES tb_affiliations(id),
    service_id UUID NOT NULL REFERENCES tb_provided_services(id),
    price DECIMAL(19, 2) NOT NULL,
    duration_minutes INT NOT NULL
);
