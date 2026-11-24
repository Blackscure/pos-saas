CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       business_id UUID NOT NULL,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL,
                       phone VARCHAR(30),
                       password VARCHAR(255) NOT NULL,
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,

                       CONSTRAINT fk_users_business
                           FOREIGN KEY (business_id) REFERENCES businesses(id),

                       CONSTRAINT uq_user_business_email
                           UNIQUE (business_id, email)
);

CREATE INDEX idx_users_business_id
    ON users(business_id);