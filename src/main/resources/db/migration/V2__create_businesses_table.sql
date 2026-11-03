
CREATE TABLE businesses (
                            id UUID PRIMARY KEY,

                            tenant_id UUID NOT NULL,

                            name VARCHAR(150) NOT NULL,

                            business_type VARCHAR(50) NOT NULL,

                            registration_number VARCHAR(100),

                            phone VARCHAR(30),

                            email VARCHAR(150),

                            address VARCHAR(255),

                            currency VARCHAR(3) NOT NULL DEFAULT 'KES',

                            tax_enabled BOOLEAN NOT NULL DEFAULT TRUE,

                            active BOOLEAN NOT NULL DEFAULT TRUE,

                            created_at TIMESTAMP NOT NULL,

                            updated_at TIMESTAMP NOT NULL,

                            CONSTRAINT fk_businesses_tenant
                                FOREIGN KEY (tenant_id)
                                    REFERENCES tenants(id),

                            CONSTRAINT uq_business_tenant_name
                                UNIQUE (tenant_id, name)
);

CREATE INDEX idx_businesses_tenant_id
    ON businesses(tenant_id);
