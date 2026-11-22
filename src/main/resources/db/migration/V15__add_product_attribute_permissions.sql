-- V15: Add product attribute and variant permissions

INSERT INTO permissions (id, code, name, description, category)
VALUES
    (gen_random_uuid(), 'ATTRIBUTE_VIEW', 'View Attributes', 'View business product attributes', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'ATTRIBUTE_CREATE', 'Create Attributes', 'Create business product attributes', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'ATTRIBUTE_UPDATE', 'Update Attributes', 'Update business product attributes', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'ATTRIBUTE_DELETE', 'Delete Attributes', 'Delete business product attributes', 'PRODUCT_ATTRIBUTE'),

    (gen_random_uuid(), 'ATTRIBUTE_VALUE_VIEW', 'View Attribute Values', 'View attribute values', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'ATTRIBUTE_VALUE_CREATE', 'Create Attribute Values', 'Create attribute values', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'ATTRIBUTE_VALUE_UPDATE', 'Update Attribute Values', 'Update attribute values', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'ATTRIBUTE_VALUE_DELETE', 'Delete Attribute Values', 'Delete attribute values', 'PRODUCT_ATTRIBUTE'),

    (gen_random_uuid(), 'PRODUCT_ATTRIBUTE_VIEW', 'View Product Attributes', 'View attributes assigned to products', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'PRODUCT_ATTRIBUTE_ASSIGN', 'Assign Product Attributes', 'Assign attributes to products', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'PRODUCT_ATTRIBUTE_UPDATE', 'Update Product Attributes', 'Update product attribute assignments', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'PRODUCT_ATTRIBUTE_REMOVE', 'Remove Product Attributes', 'Remove attributes from products', 'PRODUCT_ATTRIBUTE'),

    (gen_random_uuid(), 'PRODUCT_VARIANT_VIEW', 'View Product Variants', 'View product variants', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'PRODUCT_VARIANT_CREATE', 'Create Product Variants', 'Create product variants', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'PRODUCT_VARIANT_UPDATE', 'Update Product Variants', 'Update product variants', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'PRODUCT_VARIANT_DELETE', 'Delete Product Variants', 'Delete product variants', 'PRODUCT_ATTRIBUTE'),

    (gen_random_uuid(), 'VARIANT_ATTRIBUTE_VIEW', 'View Variant Attributes', 'View variant attribute values', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'VARIANT_ATTRIBUTE_ASSIGN', 'Assign Variant Attributes', 'Assign attribute values to variants', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'VARIANT_ATTRIBUTE_UPDATE', 'Update Variant Attributes', 'Update variant attribute values', 'PRODUCT_ATTRIBUTE'),
    (gen_random_uuid(), 'VARIANT_ATTRIBUTE_REMOVE', 'Remove Variant Attributes', 'Remove variant attribute values', 'PRODUCT_ATTRIBUTE')
    ON CONFLICT (code) DO NOTHING;


-- Grant the newly created permissions to existing OWNER roles

INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'OWNER'
  AND p.category = 'PRODUCT_ATTRIBUTE'
    ON CONFLICT DO NOTHING;
