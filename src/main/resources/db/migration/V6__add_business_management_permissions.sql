-- Add business management permissions
INSERT INTO permissions (id, code, name, description, category)
VALUES
    (
        gen_random_uuid(),
        'BUSINESS_CREATE',
        'Create Business',
        'Create new businesses under a tenant',
        'BUSINESS'
    ),
    (
        gen_random_uuid(),
        'BUSINESS_DELETE',
        'Delete Business',
        'Delete or deactivate businesses under a tenant',
        'BUSINESS'
    )
    ON CONFLICT (code) DO NOTHING;


-- Assign the new permissions to all existing OWNER roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'OWNER'
  AND p.code IN ('BUSINESS_CREATE', 'BUSINESS_DELETE')
    ON CONFLICT (role_id, permission_id) DO NOTHING;