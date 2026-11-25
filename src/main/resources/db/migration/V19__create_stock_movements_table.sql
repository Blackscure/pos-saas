
CREATE TABLE stock_movements (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                 tenant_id UUID NOT NULL,
                                 business_id UUID NOT NULL,
                                 branch_id UUID NOT NULL,
                                 inventory_id UUID NOT NULL,
                                 product_id UUID NOT NULL,
                                 product_variant_id UUID,

                                 movement_type VARCHAR(30) NOT NULL,

                                 quantity NUMERIC(19, 4) NOT NULL,
                                 quantity_before NUMERIC(19, 4) NOT NULL,
                                 quantity_after NUMERIC(19, 4) NOT NULL,

                                 reference_type VARCHAR(50),
                                 reference_id UUID,

                                 reason TEXT,

                                 performed_by UUID,

                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT chk_stock_movement_quantity_positive
                                     CHECK (quantity > 0),

                                 CONSTRAINT chk_stock_movement_type
                                     CHECK (
                                         movement_type IN (
                                                           'STOCK_IN',
                                                           'STOCK_OUT',
                                                           'ADJUSTMENT',
                                                           'TRANSFER',
                                                           'SALE',
                                                           'RETURN'
                                             )
                                         )
);

-- Business lookup
CREATE INDEX idx_stock_movements_business
    ON stock_movements (business_id);

-- Branch lookup
CREATE INDEX idx_stock_movements_branch
    ON stock_movements (business_id, branch_id);

-- Inventory history
CREATE INDEX idx_stock_movements_inventory
    ON stock_movements (inventory_id);

-- Product movement history
CREATE INDEX idx_stock_movements_product
    ON stock_movements (business_id, product_id);

-- Date-based reporting
CREATE INDEX idx_stock_movements_created_at
    ON stock_movements (business_id, created_at DESC);

-- Reference lookup (sales, purchases, transfers, etc.)
CREATE INDEX idx_stock_movements_reference
    ON stock_movements (reference_type, reference_id);
