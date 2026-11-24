-- Create Product permissions
INSERT INTO permissions (id, code, name, description, category)
VALUES
    (gen_random_uuid(), 'PRODUCT_CREATE', 'Create Product',
     'Create products under a business', 'PRODUCT'),

    (gen_random_uuid(), 'PRODUCT_VIEW', 'View Products',
     'View and retrieve business products', 'PRODUCT'),

    (gen_random_uuid(), 'PRODUCT_UPDATE', 'Update Product',
     'Update product details and status', 'PRODUCT'),

    (gen_random_uuid(), 'PRODUCT_DELETE', 'Delete Product',
     'Deactivate business products', 'PRODUCT')
    ON CONFLICT (code) DO NOTHING;

-- OWNER and MANAGER receive all Product permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name IN ('OWNER', 'MANAGER')
  AND p.code IN (
                 'PRODUCT_CREATE',
                 'PRODUCT_VIEW',
                 'PRODUCT_UPDATE',
                 'PRODUCT_DELETE'
    )
    ON CONFLICT (role_id, permission_id) DO NOTHING;

-- CASHIER and STOCK_KEEPER can view Products
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name IN ('CASHIER', 'STOCK_KEEPER')
  AND p.code = 'PRODUCT_VIEW'
    ON CONFLICT (role_id, permission_id) DO NOTHING;