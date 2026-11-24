-- =====================================================
-- V16: Fix Product Variant Permissions
-- =====================================================

-- 1. Create the exact permissions required by ProductVariantController

INSERT INTO permissions (id, code, name, description, category)
VALUES
    (gen_random_uuid(), 'VARIANT_CREATE', 'Create Variant', 'Create product variants', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'VARIANT_VIEW', 'View Variants', 'View product variants', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'VARIANT_UPDATE', 'Update Variant', 'Update product variants', 'PRODUCT_ATTRIBUTE')
    ON CONFLICT (code) DO NOTHING;


-- 2. Grant these permissions to all existing OWNER roles

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'OWNER'
  AND p.code IN (
                 'VARIANT_CREATE',
                 'VARIANT_VIEW',
                 'VARIANT_UPDATE'
    )
    ON CONFLICT DO NOTHING;