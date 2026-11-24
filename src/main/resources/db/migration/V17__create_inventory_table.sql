-- =========================================================
-- V17: Create Inventory Table
-- =========================================================

-- Required for the composite foreign key that ensures
-- the branch belongs to the correct business.
ALTER TABLE branches
    ADD CONSTRAINT uq_branches_id_business
        UNIQUE (id, business_id);


-- =========================================================
-- INVENTORY TABLE
-- =========================================================

CREATE TABLE inventory (
                           id UUID PRIMARY KEY,

                           business_id UUID NOT NULL,
                           branch_id UUID NOT NULL,
                           product_id UUID NOT NULL,
                           product_variant_id UUID,

                           quantity_on_hand NUMERIC(19, 4) NOT NULL DEFAULT 0,
                           quantity_reserved NUMERIC(19, 4) NOT NULL DEFAULT 0,

                           reorder_level NUMERIC(19, 4) NOT NULL DEFAULT 0,

                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,


    -- Branch must belong to this business
                           CONSTRAINT fk_inventory_branch_business
                               FOREIGN KEY (branch_id, business_id)
                                   REFERENCES branches (id, business_id),


    -- Product must belong to this business
                           CONSTRAINT fk_inventory_product_business
                               FOREIGN KEY (product_id, business_id)
                                   REFERENCES products (id, business_id),


    -- Variant must belong to the same business and product
                           CONSTRAINT fk_inventory_variant_business_product
                               FOREIGN KEY (product_variant_id, business_id, product_id)
                                   REFERENCES product_variants (id, business_id, product_id),


    -- Stock quantity cannot be negative
                           CONSTRAINT chk_inventory_quantity_on_hand
                               CHECK (quantity_on_hand >= 0),


    -- Reserved quantity cannot be negative
                           CONSTRAINT chk_inventory_quantity_reserved
                               CHECK (quantity_reserved >= 0),


    -- Reserved stock cannot exceed available physical stock
                           CONSTRAINT chk_inventory_reserved_not_exceed_on_hand
                               CHECK (quantity_reserved <= quantity_on_hand),


    -- Reorder level cannot be negative
                           CONSTRAINT chk_inventory_reorder_level
                               CHECK (reorder_level >= 0)
);


-- =========================================================
-- INDEXES
-- =========================================================

-- Business and branch inventory lookup
CREATE INDEX idx_inventory_business_branch
    ON inventory (business_id, branch_id);


-- Product inventory lookup
CREATE INDEX idx_inventory_product
    ON inventory (product_id);


-- Variant inventory lookup (only rows with variants)
CREATE INDEX idx_inventory_variant
    ON inventory (product_variant_id)
    WHERE product_variant_id IS NOT NULL;


-- =========================================================
-- UNIQUE STOCK RECORDS
-- =========================================================

-- One stock record per branch/product
-- when the product has no variant
CREATE UNIQUE INDEX uq_inventory_branch_product_no_variant
    ON inventory (branch_id, product_id)
    WHERE product_variant_id IS NULL;


-- One stock record per branch/product/variant
CREATE UNIQUE INDEX uq_inventory_branch_product_variant
    ON inventory (branch_id, product_id, product_variant_id)
    WHERE product_variant_id IS NOT NULL;
