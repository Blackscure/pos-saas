-- Replace the single plan price with monthly and yearly pricing.

ALTER TABLE subscription_plans
    RENAME COLUMN price TO monthly_price;

ALTER TABLE subscription_plans
    ADD COLUMN yearly_price NUMERIC(12, 2) NOT NULL DEFAULT 0;

-- Remove billing_interval because each plan now supports
-- both monthly and yearly billing.

ALTER TABLE subscription_plans
DROP CONSTRAINT IF EXISTS subscription_plans_interval_check;

ALTER TABLE subscription_plans
DROP COLUMN billing_interval;

-- Ensure both prices cannot be negative.

ALTER TABLE subscription_plans
    ADD CONSTRAINT subscription_plans_monthly_price_check
        CHECK (monthly_price >= 0);

ALTER TABLE subscription_plans
    ADD CONSTRAINT subscription_plans_yearly_price_check
        CHECK (yearly_price >= 0);
