-- =========================================================
-- V21: Add Supplier Permissions
-- =========================================================

-- 1. Create Supplier permissions
INSERT INTO permissions (id, code, name, description, category)
VALUES
    (
        gen_random_uuid(),
        'SUPPLIER_CREATE',
        'Create Supplier',
        'Create suppliers under a business',
        'SUPPLIER'
    ),
    (
        gen_random_uuid(),
        'SUPPLIER_VIEW',
        'View Suppliers',
        'View and retrieve business suppliers',
        'SUPPLIER'
    ),
    (
        gen_random_uuid(),
        'SUPPLIER_UPDATE',
        'Update Supplier',
        'Update supplier details and status',
        'SUPPLIER'
    ),
    (
        gen_random_uuid(),
        'SUPPLIER_DELETE',
        'Delete Supplier',
        'Deactivate business suppliers',
        'SUPPLIER'
    )
    ON CONFLICT (code) DO NOTHING;


-- 2. Assign all Supplier permissions to OWNER and MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name IN ('OWNER', 'MANAGER')
  AND p.code IN (
                 'SUPPLIER_CREATE',
                 'SUPPLIER_VIEW',
                 'SUPPLIER_UPDATE',
                 'SUPPLIER_DELETE'
    )
    ON CONFLICT (role_id, permission_id) DO NOTHING;


-- 3. Assign Supplier view permission to CASHIER and STOCK_KEEPER
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name IN ('CASHIER', 'STOCK_KEEPER')
  AND p.code = 'SUPPLIER_VIEW'
    ON CONFLICT (role_id, permission_id) DO NOTHING;