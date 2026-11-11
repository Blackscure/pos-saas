-- 1. Subscription plans
CREATE TABLE subscription_plans (
                                    id UUID PRIMARY KEY,
                                    name VARCHAR(100) NOT NULL,
                                    code VARCHAR(50) NOT NULL UNIQUE,
                                    description VARCHAR(255),
                                    price NUMERIC(12, 2) NOT NULL DEFAULT 0,
                                    currency VARCHAR(3) NOT NULL DEFAULT 'KES',
                                    billing_interval VARCHAR(20) NOT NULL,
                                    max_branches INTEGER,
                                    max_users INTEGER,
                                    active BOOLEAN NOT NULL DEFAULT TRUE,
                                    created_at TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP NOT NULL,

                                    CONSTRAINT subscription_plans_price_check
                                        CHECK (price >= 0),

                                    CONSTRAINT subscription_plans_interval_check
                                        CHECK (billing_interval IN ('MONTHLY', 'YEARLY')),

                                    CONSTRAINT subscription_plans_branches_check
                                        CHECK (max_branches IS NULL OR max_branches > 0),

                                    CONSTRAINT subscription_plans_users_check
                                        CHECK (max_users IS NULL OR max_users > 0)
);

-- 2. Tenant subscriptions
CREATE TABLE subscriptions (
                               id UUID PRIMARY KEY,
                               tenant_id UUID NOT NULL,
                               plan_id UUID NOT NULL,
                               status VARCHAR(20) NOT NULL,
                               start_date TIMESTAMP NOT NULL,
                               end_date TIMESTAMP,
                               trial_end_date TIMESTAMP,
                               cancelled_at TIMESTAMP,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP NOT NULL,

                               CONSTRAINT subscriptions_tenant_fk
                                   FOREIGN KEY (tenant_id)
                                       REFERENCES tenants(id),

                               CONSTRAINT subscriptions_plan_fk
                                   FOREIGN KEY (plan_id)
                                       REFERENCES subscription_plans(id),

                               CONSTRAINT subscriptions_status_check
                                   CHECK (
                                       status IN (
                                                  'TRIALING',
                                                  'ACTIVE',
                                                  'PAST_DUE',
                                                  'SUSPENDED',
                                                  'CANCELLED',
                                                  'EXPIRED'
                                           )
                                       ),

                               CONSTRAINT subscriptions_dates_check
                                   CHECK (end_date IS NULL OR end_date >= start_date)
);

-- 3. Allow only one current subscription per tenant.
-- Historical cancelled and expired subscriptions remain stored.
CREATE UNIQUE INDEX subscriptions_one_current_per_tenant
    ON subscriptions(tenant_id)
    WHERE status IN ('TRIALING', 'ACTIVE', 'PAST_DUE', 'SUSPENDED');

-- 4. Indexes for common queries
CREATE INDEX idx_subscriptions_tenant_id
    ON subscriptions(tenant_id);

CREATE INDEX idx_subscriptions_plan_id
    ON subscriptions(plan_id);

CREATE INDEX idx_subscriptions_status
    ON subscriptions(status);
