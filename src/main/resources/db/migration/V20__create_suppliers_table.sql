
CREATE TABLE suppliers (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           business_id UUID NOT NULL,

                           name VARCHAR(150) NOT NULL,
                           contact_person VARCHAR(150),
                           phone VARCHAR(30),
                           email VARCHAR(150),
                           address TEXT,
                           tax_pin VARCHAR(50),
                           payment_terms VARCHAR(100),
                           notes TEXT,

                           active BOOLEAN NOT NULL DEFAULT TRUE,

                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_suppliers_business
                               FOREIGN KEY (business_id)
                                   REFERENCES businesses(id),

                           CONSTRAINT uq_suppliers_business_name
                               UNIQUE (business_id, name)
);

CREATE INDEX idx_suppliers_business
    ON suppliers(business_id);

CREATE INDEX idx_suppliers_business_active
    ON suppliers(business_id, active);

CREATE INDEX idx_suppliers_business_name
    ON suppliers(business_id, name);
