-- IB Farms — PostgreSQL schema for Neon
-- Run in Neon SQL Editor or: psql "$DATABASE_URL" -f scripts/neon-schema.sql
--
-- Note: The app also creates/updates tables automatically (spring.jpa.hibernate.ddl-auto=update).
-- Use this script if you want to create tables manually before first deploy.

BEGIN;

-- Drop in dependency order (safe re-run for empty dev DB)
DROP TABLE IF EXISTS salary_payments CASCADE;
DROP TABLE IF EXISTS other_expenses CASCADE;
DROP TABLE IF EXISTS animal_sales CASCADE;
DROP TABLE IF EXISTS animal_expenses CASCADE;
DROP TABLE IF EXISTS growth_records CASCADE;
DROP TABLE IF EXISTS animals CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ---------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL,
    email           VARCHAR(100) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT FALSE,
    approval_token  VARCHAR(64),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_approval_token UNIQUE (approval_token)
);

-- ---------------------------------------------------------------------------
-- animals
-- ---------------------------------------------------------------------------
CREATE TABLE animals (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(100)   NOT NULL,
    species                 VARCHAR(50)    NOT NULL,
    breed                   VARCHAR(80),
    tag_number              VARCHAR(40)    NOT NULL,
    purchase_price          NUMERIC(12, 2) NOT NULL,
    purchase_date           DATE           NOT NULL,
    picture_filename        VARCHAR(255),
    pregnant                BOOLEAN        NOT NULL DEFAULT FALSE,
    pregnancy_date          DATE,
    expected_delivery_date  DATE,
    status                  VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    owner_id                BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ,
    CONSTRAINT uk_animals_owner_tag UNIQUE (owner_id, tag_number)
);

CREATE INDEX idx_animals_owner_id ON animals (owner_id);

-- ---------------------------------------------------------------------------
-- growth_records
-- ---------------------------------------------------------------------------
CREATE TABLE growth_records (
    id          BIGSERIAL PRIMARY KEY,
    animal_id   BIGINT         NOT NULL REFERENCES animals (id) ON DELETE CASCADE,
    record_date DATE           NOT NULL,
    height_cm   NUMERIC(8, 2),
    length_cm   NUMERIC(8, 2),
    weight_kg   NUMERIC(10, 2),
    notes       VARCHAR(500),
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_growth_records_animal_id ON growth_records (animal_id);

-- ---------------------------------------------------------------------------
-- animal_expenses
-- ---------------------------------------------------------------------------
CREATE TABLE animal_expenses (
    id           BIGSERIAL PRIMARY KEY,
    animal_id    BIGINT         NOT NULL REFERENCES animals (id) ON DELETE CASCADE,
    expense_date DATE           NOT NULL,
    category     VARCHAR(60)    NOT NULL,
    amount       NUMERIC(12, 2) NOT NULL,
    description  VARCHAR(500),
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_animal_expenses_animal_id ON animal_expenses (animal_id);

-- ---------------------------------------------------------------------------
-- animal_sales (one sale per animal)
-- ---------------------------------------------------------------------------
CREATE TABLE animal_sales (
    id          BIGSERIAL PRIMARY KEY,
    animal_id   BIGINT         NOT NULL REFERENCES animals (id) ON DELETE CASCADE,
    sale_date   DATE           NOT NULL,
    sale_price  NUMERIC(12, 2) NOT NULL,
    buyer_name  VARCHAR(120),
    notes       VARCHAR(500),
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_animal_sales_animal_id UNIQUE (animal_id)
);

-- ---------------------------------------------------------------------------
-- employees
-- ---------------------------------------------------------------------------
CREATE TABLE employees (
    id              BIGSERIAL PRIMARY KEY,
    full_name       VARCHAR(100)   NOT NULL,
    role            VARCHAR(80),
    phone           VARCHAR(30),
    monthly_salary  NUMERIC(12, 2),
    hire_date       DATE,
    active          BOOLEAN        NOT NULL DEFAULT TRUE,
    owner_id        BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_employees_owner_id ON employees (owner_id);

-- ---------------------------------------------------------------------------
-- salary_payments
-- ---------------------------------------------------------------------------
CREATE TABLE salary_payments (
    id           BIGSERIAL PRIMARY KEY,
    employee_id  BIGINT         NOT NULL REFERENCES employees (id) ON DELETE CASCADE,
    salary_year  INTEGER        NOT NULL,
    salary_month INTEGER        NOT NULL,
    amount       NUMERIC(12, 2) NOT NULL,
    paid_date    DATE           NOT NULL,
    notes        VARCHAR(500),
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_salary_payments_employee_period UNIQUE (employee_id, salary_year, salary_month)
);

CREATE INDEX idx_salary_payments_employee_id ON salary_payments (employee_id);

-- ---------------------------------------------------------------------------
-- other_expenses
-- ---------------------------------------------------------------------------
CREATE TABLE other_expenses (
    id           BIGSERIAL PRIMARY KEY,
    expense_date DATE           NOT NULL,
    category     VARCHAR(60)    NOT NULL,
    amount       NUMERIC(12, 2) NOT NULL,
    description  VARCHAR(500),
    owner_id     BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_other_expenses_owner_id ON other_expenses (owner_id);

COMMIT;
