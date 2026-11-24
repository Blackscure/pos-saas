-- 1. Reusable attribute definitions owned by a business
CREATE TABLE business_attributes (
                                     id UUID PRIMARY KEY,
                                     business_id UUID NOT NULL,
                                     name VARCHAR(100) NOT NULL,
                                     code VARCHAR(100) NOT NULL,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_business_attributes_business
                                         FOREIGN KEY (business_id)
                                             REFERENCES businesses(id),

                                     CONSTRAINT uq_business_attributes_id_business
                                         UNIQUE (id, business_id),

                                     CONSTRAINT uq_business_attributes_code
                                         UNIQUE (business_id, code)
);

-- 2. Allowed values for each business attribute
CREATE TABLE business_attribute_values (
                                           id UUID PRIMARY KEY,
                                           business_id UUID NOT NULL,
                                           attribute_id UUID NOT NULL,
                                           value VARCHAR(100) NOT NULL,
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                           CONSTRAINT fk_attribute_values_attribute
                                               FOREIGN KEY (attribute_id, business_id)
                                                   REFERENCES business_attributes(id, business_id),

                                           CONSTRAINT uq_attribute_values_id_business_attribute
                                               UNIQUE (id, business_id, attribute_id),

                                           CONSTRAINT uq_attribute_values_value
                                               UNIQUE (business_id, attribute_id, value)
);

-- 3. Attributes enabled for a particular product
CREATE TABLE product_attributes (
                                    id UUID PRIMARY KEY,
                                    business_id UUID NOT NULL,
                                    product_id UUID NOT NULL,
                                    attribute_id UUID NOT NULL,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_product_attributes_product
                                        FOREIGN KEY (product_id, business_id)
                                            REFERENCES products(id, business_id)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_product_attributes_attribute
                                        FOREIGN KEY (attribute_id, business_id)
                                            REFERENCES business_attributes(id, business_id),

                                    CONSTRAINT uq_product_attributes_id_product_business
                                        UNIQUE (id, product_id, business_id),

                                    CONSTRAINT uq_product_attributes_product_attribute
                                        UNIQUE (product_id, business_id, attribute_id)
);

-- 4. Actual variants belonging to a product
CREATE TABLE product_variants (
                                  id UUID PRIMARY KEY,
                                  business_id UUID NOT NULL,
                                  product_id UUID NOT NULL,
                                  sku VARCHAR(100) NOT NULL,
                                  barcode VARCHAR(100),
                                  name VARCHAR(200),
                                  purchase_price NUMERIC(15,4),
                                  selling_price NUMERIC(15,4),
                                  active BOOLEAN NOT NULL DEFAULT TRUE,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_product_variants_product
                                      FOREIGN KEY (product_id, business_id)
                                          REFERENCES products(id, business_id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT uq_product_variants_id_business_product
                                      UNIQUE (id, business_id, product_id),

                                  CONSTRAINT chk_variant_purchase_price
                                      CHECK (purchase_price IS NULL OR purchase_price >= 0),

                                  CONSTRAINT chk_variant_selling_price
                                      CHECK (selling_price IS NULL OR selling_price >= 0)
);

-- Variant SKU must be unique within the business
CREATE UNIQUE INDEX uq_product_variants_business_sku
    ON product_variants (business_id, LOWER(sku));

-- Variant barcode must be unique within the business when supplied
CREATE UNIQUE INDEX uq_product_variants_business_barcode
    ON product_variants (business_id, LOWER(barcode))
    WHERE barcode IS NOT NULL;

-- 5. Attribute values selected by each variant
CREATE TABLE product_variant_attribute_values (
                                                  id UUID PRIMARY KEY,
                                                  business_id UUID NOT NULL,
                                                  product_id UUID NOT NULL,
                                                  product_variant_id UUID NOT NULL,
                                                  product_attribute_id UUID NOT NULL,
                                                  attribute_id UUID NOT NULL,
                                                  attribute_value_id UUID NOT NULL,
                                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                  CONSTRAINT fk_variant_attribute_values_variant
                                                      FOREIGN KEY (product_variant_id, business_id, product_id)
                                                          REFERENCES product_variants(id, business_id, product_id)
                                                          ON DELETE CASCADE,

                                                  CONSTRAINT fk_variant_attribute_values_product_attribute
                                                      FOREIGN KEY (product_attribute_id, product_id, business_id)
                                                          REFERENCES product_attributes(id, product_id, business_id)
                                                          ON DELETE CASCADE,

                                                  CONSTRAINT fk_variant_attribute_values_attribute_value
                                                      FOREIGN KEY (attribute_value_id, business_id, attribute_id)
                                                          REFERENCES business_attribute_values(id, business_id, attribute_id),

                                                  CONSTRAINT uq_variant_one_value_per_attribute
                                                      UNIQUE (product_variant_id, business_id, attribute_id)
);

-- Supporting indexes
CREATE INDEX idx_business_attributes_business
    ON business_attributes (business_id);

CREATE INDEX idx_attribute_values_attribute
    ON business_attribute_values (attribute_id, business_id);

CREATE INDEX idx_product_attributes_product
    ON product_attributes (product_id, business_id);

CREATE INDEX idx_product_variants_product
    ON product_variants (product_id, business_id);

CREATE INDEX idx_product_variant_attribute_values_variant
    ON product_variant_attribute_values (product_variant_id, business_id);
