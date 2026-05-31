CREATE UNIQUE INDEX uq_investment_profiles_user_current
    ON investment_profiles (user_id)
    WHERE is_current = true AND deleted_at IS NULL;