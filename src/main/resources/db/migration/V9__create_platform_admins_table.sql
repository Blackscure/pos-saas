CREATE TABLE platform_admins (
                                 id UUID PRIMARY KEY,

                                 first_name VARCHAR(100) NOT NULL,
                                 last_name VARCHAR(100) NOT NULL,

                                 email VARCHAR(255) NOT NULL,
                                 password_hash VARCHAR(255) NOT NULL,

                                 role VARCHAR(30) NOT NULL DEFAULT 'SUPER_ADMIN',

                                 active BOOLEAN NOT NULL DEFAULT TRUE,

                                 created_at TIMESTAMP NOT NULL,
                                 updated_at TIMESTAMP NOT NULL,

                                 CONSTRAINT platform_admins_role_check
                                     CHECK (role IN ('SUPER_ADMIN'))
);

CREATE UNIQUE INDEX platform_admins_email_unique
    ON platform_admins (LOWER(email));

CREATE INDEX idx_platform_admins_active
    ON platform_admins (active);