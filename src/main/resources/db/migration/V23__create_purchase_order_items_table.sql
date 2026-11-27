CREATE TABLE purchase_order_items (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                      purchase_order_id UUID NOT NULL,
                                      product_id UUID NOT NULL,
                                      product_variant_id UUID,

                                      quantity_ordered INTEGER NOT NULL,
                                      quantity_received INTEGER NOT NULL DEFAULT 0,

                                      unit_cost NUMERIC(12,2) NOT NULL,
                                      tax_rate NUMERIC(5,2) NOT NULL DEFAULT 0,
                                      subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
                                      tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
                                      total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,

                                      CONSTRAINT fk_purchase_order_items_purchase_order
                                          FOREIGN KEY (purchase_order_id)
                                              REFERENCES purchase_orders(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_purchase_order_items_product
                                          FOREIGN KEY (product_id)
                                              REFERENCES products(id),

                                      CONSTRAINT fk_purchase_order_items_variant
                                          FOREIGN KEY (product_variant_id)
                                              REFERENCES product_variants(id)
);

CREATE INDEX idx_purchase_order_items_purchase_order
    ON purchase_order_items(purchase_order_id);

CREATE INDEX idx_purchase_order_items_product
    ON purchase_order_items(product_id);

CREATE INDEX idx_purchase_order_items_variant
    ON purchase_order_items(product_variant_id);
