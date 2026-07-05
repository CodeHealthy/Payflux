ALTER TABLE accounts ADD CONSTRAINT uk_accounts_owner_user_id UNIQUE (owner_user_id);
