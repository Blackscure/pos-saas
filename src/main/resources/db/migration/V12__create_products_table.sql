
CREATE TABLE products (
                          id UUID PRIMARY KEY,

                          business_id UUID NOT NULL,
                          category_id UUID,

                          sku VARCHAR(100) NOT NULL,
                          name VARCHAR(200) NOT NULL,
                          description TEXT,

                          barcode VARCHAR(100),
                          image_url VARCHAR(1000),

                          purchase_price NUMERIC(19, 4) NOT NULL DEFAULT 0,
                          selling_price NUMERIC(19, 4) NOT NULL,

                          tax_rate NUMERIC(7, 4) NOT NULL DEFAULT 0,
                          tax_inclusive BOOLEAN NOT NULL DEFAULT FALSE,

                          active BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,

                          CONSTRAINT fk_products_business
                              FOREIGN KEY (business_id)
                                  REFERENCES businesses(id),

                          CONSTRAINT fk_products_category
                              FOREIGN KEY (category_id, business_id)
                                  REFERENCES categories(id, business_id),

                          CONSTRAINT uq_products_id_business
                              UNIQUE (id, business_id),

                          CONSTRAINT chk_products_purchase_price
                              CHECK (purchase_price >= 0),

                          CONSTRAINT chk_products_selling_price
                              CHECK (selling_price >= 0),

                          CONSTRAINT chk_products_tax_rate
                              CHECK (tax_rate >= 0 AND tax_rate <= 100)
);

-- SKU must be unique within a business, ignoring letter case.
CREATE UNIQUE INDEX uq_products_business_sku
    ON products (business_id, LOWER(sku));

-- Barcode must be unique within a business when provided.
CREATE UNIQUE INDEX uq_products_business_barcode
    ON products (business_id, LOWER(barcode))
    WHERE barcode IS NOT NULL;

-- Common product lookup indexes.
CREATE INDEX idx_products_business_id
    ON products (business_id);

CREATE INDEX idx_products_category_id
    ON products (category_id);

CREATE INDEX idx_products_business_active
    ON products (business_id, active);

CREATE INDEX idx_products_business_name
    ON products (business_id, name);
