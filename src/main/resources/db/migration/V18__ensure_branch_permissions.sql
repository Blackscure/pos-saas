-- =========================================================
-- V18: Ensure Branch Permissions Exist and Assign to OWNER
-- =========================================================
-- 1. Ensure branch permissions exist
INSERT INTO permissions (id, code, name, description, category)
VALUES
    (gen_random_uuid(), 'BRANCH_VIEW', 'View Branches', 'View business branches', 'BRANCH'),
    (gen_random_uuid(), 'BRANCH_CREATE', 'Create Branch', 'Create new business branches', 'BRANCH'),
    (gen_random_uuid(), 'BRANCH_UPDATE', 'Update Branch', 'Update branch information', 'BRANCH'),
    (gen_random_uuid(), 'BRANCH_DELETE', 'Delete Branch', 'Delete or deactivate branches', 'BRANCH')
    ON CONFLICT (code) DO NOTHING;


-- 2. Assign all branch permissions to every OWNER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,

    p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name IN ('OWNER', 'MANAGER')
  AND p.code IN (
                 'BRANCH_VIEW',
                 'BRANCH_CREATE',
                 'BRANCH_UPDATE',
                 'BRANCH_DELETE'
    )
    ON CONFLICT (role_id, permission_id) DO NOTHING;
