CREATE TABLE categories (
                            id UUID PRIMARY KEY,

                            business_id UUID NOT NULL,

                            parent_category_id UUID,

                            name VARCHAR(150) NOT NULL,

                            description TEXT,

                            active BOOLEAN NOT NULL DEFAULT TRUE,

                            created_at TIMESTAMP NOT NULL,

                            updated_at TIMESTAMP NOT NULL,

                            CONSTRAINT fk_categories_business
                                FOREIGN KEY (business_id)
                                    REFERENCES businesses(id),

                            CONSTRAINT uq_categories_id_business
                                UNIQUE (id, business_id),

                            CONSTRAINT fk_categories_parent
                                FOREIGN KEY (parent_category_id, business_id)
                                    REFERENCES categories(id, business_id),

                            CONSTRAINT uq_categories_business_parent_name
                                UNIQUE NULLS NOT DISTINCT
                                    (business_id, parent_category_id, name),

                            CONSTRAINT chk_categories_not_own_parent
                                CHECK (
                                    parent_category_id IS NULL
                                        OR parent_category_id <> id
                                    )
);

CREATE INDEX idx_categories_business_id
    ON categories(business_id);

CREATE INDEX idx_categories_parent_id
    ON categories(parent_category_id);

CREATE INDEX idx_categories_business_active
    ON categories(business_id, active);
