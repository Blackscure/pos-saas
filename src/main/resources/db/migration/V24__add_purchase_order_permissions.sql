-- =========================================================
-- V24: Add Purchase Order Permissions
-- =========================================================

-- 1. Create Purchase Order permissions
INSERT INTO permissions (id, code, name, description, category)
VALUES
    (
        gen_random_uuid(),
        'PURCHASE_ORDER_CREATE',
        'Create Purchase Order',
        'Create purchase orders for a business',
        'PURCHASE_ORDER'
    ),
    (
        gen_random_uuid(),
        'PURCHASE_ORDER_VIEW',
        'View Purchase Orders',
        'View and retrieve purchase orders',
        'PURCHASE_ORDER'
    ),
    (
        gen_random_uuid(),
        'PURCHASE_ORDER_UPDATE',
        'Update Purchase Order',
        'Update draft purchase orders',
        'PURCHASE_ORDER'
    ),
    (
        gen_random_uuid(),
        'PURCHASE_ORDER_SUBMIT',
        'Submit Purchase Order',
        'Submit purchase orders for approval',
        'PURCHASE_ORDER'
    ),
    (
        gen_random_uuid(),
        'PURCHASE_ORDER_APPROVE',
        'Approve Purchase Order',
        'Approve submitted purchase orders',
        'PURCHASE_ORDER'
    ),
    (
        gen_random_uuid(),
        'PURCHASE_ORDER_REJECT',
        'Reject Purchase Order',
        'Reject submitted purchase orders',
        'PURCHASE_ORDER'
    ),
    (
        gen_random_uuid(),
        'PURCHASE_ORDER_CANCEL',
        'Cancel Purchase Order',
        'Cancel purchase orders',
        'PURCHASE_ORDER'
    )
    ON CONFLICT (code) DO NOTHING;


-- 2. Assign all Purchase Order permissions to OWNER and MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name IN ('OWNER', 'MANAGER')
  AND p.code IN (
                 'PURCHASE_ORDER_CREATE',
                 'PURCHASE_ORDER_VIEW',
                 'PURCHASE_ORDER_UPDATE',
                 'PURCHASE_ORDER_SUBMIT',
                 'PURCHASE_ORDER_APPROVE',
                 'PURCHASE_ORDER_REJECT',
                 'PURCHASE_ORDER_CANCEL'
    )
    ON CONFLICT (role_id, permission_id) DO NOTHING;


-- 3. Assign Purchase Order view permission to CASHIER and STOCK_KEEPER
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name IN ('CASHIER', 'STOCK_KEEPER')
  AND p.code = 'PURCHASE_ORDER_VIEW'
    ON CONFLICT (role_id, permission_id) DO NOTHING;
