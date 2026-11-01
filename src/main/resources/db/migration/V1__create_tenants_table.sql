
CREATE TABLE tenants (
                         id UUID PRIMARY KEY,
                         name VARCHAR(150) NOT NULL UNIQUE,
                         code VARCHAR(100) NOT NULL UNIQUE,
                         email VARCHAR(150) NOT NULL UNIQUE,
                         active BOOLEAN NOT NULL DEFAULT TRUE,
                         created_at TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP NOT NULL
);
