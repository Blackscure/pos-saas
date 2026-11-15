-- Add category management permissions
INSERT INTO permissions (id, code, name, description, category)
VALUES
    (
        gen_random_uuid(),
        'CATEGORY_CREATE',
        'Create Category',
        'Create product categories under a business',
        'CATEGORY'
    ),
    (
        gen_random_uuid(),
        'CATEGORY_VIEW',
        'View Categories',
        'View and retrieve business categories',
        'CATEGORY'
    ),
    (
        gen_random_uuid(),
        'CATEGORY_UPDATE',
        'Update Category',
        'Update category details and status',
        'CATEGORY'
    ),
    (
        gen_random_uuid(),
        'CATEGORY_DELETE',
        'Delete Category',
        'Deactivate business categories',
        'CATEGORY'
    )
    ON CONFLICT (code) DO NOTHING;


-- Assign all category permissions to existing OWNER and MANAGER roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name IN ('OWNER', 'MANAGER')
  AND p.code IN (
                 'CATEGORY_CREATE',
                 'CATEGORY_VIEW',
                 'CATEGORY_UPDATE',
                 'CATEGORY_DELETE'
    )
    ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Assign view permission to CASHIER and STOCK_KEEPER roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name IN ('CASHIER', 'STOCK_KEEPER')
  AND p.code = 'CATEGORY_VIEW'
    ON CONFLICT (role_id, permission_id) DO NOTHING;
