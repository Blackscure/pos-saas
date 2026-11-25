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