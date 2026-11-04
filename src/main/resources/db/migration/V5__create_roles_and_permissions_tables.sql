-- 1. Roles table
CREATE TABLE roles (
                       id UUID PRIMARY KEY,
                       business_id UUID NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       description VARCHAR(255),
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,

                       CONSTRAINT fk_roles_business
                           FOREIGN KEY (business_id)
                               REFERENCES businesses(id),

                       CONSTRAINT uq_role_business_name
                           UNIQUE (business_id, name)
);

CREATE INDEX idx_roles_business_id
    ON roles(business_id);


-- 2. Permissions table
CREATE TABLE permissions (
                             id UUID PRIMARY KEY,
                             code VARCHAR(100) NOT NULL UNIQUE,
                             name VARCHAR(150) NOT NULL,
                             description VARCHAR(255),
                             category VARCHAR(100) NOT NULL
);


-- 3. Role permissions table
CREATE TABLE role_permissions (
                                  role_id UUID NOT NULL,
                                  permission_id UUID NOT NULL,

                                  PRIMARY KEY (role_id, permission_id),

                                  CONSTRAINT fk_role_permissions_role
                                      FOREIGN KEY (role_id)
                                          REFERENCES roles(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_role_permissions_permission
                                      FOREIGN KEY (permission_id)
                                          REFERENCES permissions(id)
                                          ON DELETE CASCADE
);


-- 4. User roles table
CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id UUID NOT NULL,

                            PRIMARY KEY (user_id, role_id),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(id)
                                    ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_role_id
    ON user_roles(role_id);


-- 5. Seed initial permission catalog

INSERT INTO permissions (id, code, name, description, category)
VALUES
-- Business
(gen_random_uuid(), 'BUSINESS_VIEW', 'View Business', 'View business details', 'BUSINESS'),
(gen_random_uuid(), 'BUSINESS_UPDATE', 'Update Business', 'Update business details', 'BUSINESS'),

-- Branches
(gen_random_uuid(), 'BRANCH_VIEW', 'View Branches', 'View business branches', 'BRANCH'),
(gen_random_uuid(), 'BRANCH_CREATE', 'Create Branch', 'Create new branches', 'BRANCH'),
(gen_random_uuid(), 'BRANCH_UPDATE', 'Update Branch', 'Update branch details', 'BRANCH'),
(gen_random_uuid(), 'BRANCH_DELETE', 'Delete Branch', 'Deactivate branches', 'BRANCH'),

-- Users
(gen_random_uuid(), 'USER_VIEW', 'View Users', 'View business users', 'USER'),
(gen_random_uuid(), 'USER_CREATE', 'Create User', 'Create business users', 'USER'),
(gen_random_uuid(), 'USER_UPDATE', 'Update User', 'Update user details', 'USER'),
(gen_random_uuid(), 'USER_DEACTIVATE', 'Deactivate User', 'Deactivate business users', 'USER'),

-- Roles and permissions
(gen_random_uuid(), 'ROLE_VIEW', 'View Roles', 'View business roles', 'ROLE'),
(gen_random_uuid(), 'ROLE_CREATE', 'Create Role', 'Create custom roles', 'ROLE'),
(gen_random_uuid(), 'ROLE_UPDATE', 'Update Role', 'Update role details', 'ROLE'),
(gen_random_uuid(), 'ROLE_DELETE', 'Delete Role', 'Delete custom roles', 'ROLE'),
(gen_random_uuid(), 'ROLE_ASSIGN', 'Assign Roles', 'Assign roles to users', 'ROLE'),

-- Products
(gen_random_uuid(), 'PRODUCT_VIEW', 'View Products', 'View products', 'PRODUCT'),
(gen_random_uuid(), 'PRODUCT_CREATE', 'Create Product', 'Create products', 'PRODUCT'),
(gen_random_uuid(), 'PRODUCT_UPDATE', 'Update Product', 'Update products', 'PRODUCT'),
(gen_random_uuid(), 'PRODUCT_DELETE', 'Delete Product', 'Deactivate products', 'PRODUCT'),

-- Inventory
(gen_random_uuid(), 'INVENTORY_VIEW', 'View Inventory', 'View stock levels', 'INVENTORY'),
(gen_random_uuid(), 'INVENTORY_ADJUST', 'Adjust Inventory', 'Adjust stock quantities', 'INVENTORY'),
(gen_random_uuid(), 'INVENTORY_TRANSFER', 'Transfer Stock', 'Transfer stock between branches', 'INVENTORY'),

-- Sales
(gen_random_uuid(), 'SALE_VIEW', 'View Sales', 'View sales transactions', 'SALES'),
(gen_random_uuid(), 'SALE_CREATE', 'Create Sale', 'Process sales transactions', 'SALES'),
(gen_random_uuid(), 'SALE_VOID', 'Void Sale', 'Void sales transactions', 'SALES'),
(gen_random_uuid(), 'SALE_REFUND', 'Refund Sale', 'Process customer refunds', 'SALES'),

-- Payments
(gen_random_uuid(), 'PAYMENT_VIEW', 'View Payments', 'View payment transactions', 'PAYMENT'),
(gen_random_uuid(), 'PAYMENT_PROCESS', 'Process Payments', 'Process customer payments', 'PAYMENT'),

-- Reports
(gen_random_uuid(), 'REPORT_VIEW', 'View Reports', 'View business reports', 'REPORT'),
(gen_random_uuid(), 'REPORT_EXPORT', 'Export Reports', 'Export business reports', 'REPORT');