ALTER TABLE accounts ADD COLUMN account_number VARCHAR(32);

UPDATE accounts
SET account_number = CONCAT('9201', LPAD(CAST(id AS VARCHAR), 8, '0'))
WHERE account_number IS NULL;

ALTER TABLE accounts ALTER COLUMN account_number SET NOT NULL;

ALTER TABLE accounts ADD CONSTRAINT uk_accounts_account_number UNIQUE (account_number);
