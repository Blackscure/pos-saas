CREATE TABLE purchase_orders (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                 business_id UUID NOT NULL,
                                 branch_id UUID NOT NULL,
                                 supplier_id UUID NOT NULL,

                                 po_number VARCHAR(50) NOT NULL,
                                 status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

                                 order_date DATE NOT NULL,
                                 expected_delivery_date DATE,

                                 subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
                                 tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
                                 total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,

                                 notes TEXT,

                                 created_by UUID,
                                 approved_by UUID,
                                 approved_at TIMESTAMP,

                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_purchase_orders_business
                                     FOREIGN KEY (business_id)
                                         REFERENCES businesses(id),

                                 CONSTRAINT fk_purchase_orders_branch
                                     FOREIGN KEY (branch_id)
                                         REFERENCES branches(id),

                                 CONSTRAINT fk_purchase_orders_supplier
                                     FOREIGN KEY (supplier_id)
                                         REFERENCES suppliers(id),

                                 CONSTRAINT fk_purchase_orders_created_by
                                     FOREIGN KEY (created_by)
                                         REFERENCES users(id),

                                 CONSTRAINT fk_purchase_orders_approved_by
                                     FOREIGN KEY (approved_by)
                                         REFERENCES users(id),

                                 CONSTRAINT uq_purchase_order_business_number
                                     UNIQUE (business_id, po_number)
);

CREATE INDEX idx_purchase_orders_business
    ON purchase_orders(business_id);

CREATE INDEX idx_purchase_orders_branch
    ON purchase_orders(branch_id);

CREATE INDEX idx_purchase_orders_supplier
    ON purchase_orders(supplier_id);

CREATE INDEX idx_purchase_orders_status
    ON purchase_orders(status);

CREATE INDEX idx_purchase_orders_business_status
    ON purchase_orders(business_id, status);

CREATE INDEX idx_purchase_orders_order_date
    ON purchase_orders(business_id, order_date);
