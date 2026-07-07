ALTER TABLE wallet_transfers
	ADD COLUMN request_hash VARCHAR(64);

CREATE INDEX idx_wallet_transfers_owner_request_hash
	ON wallet_transfers (owner_user_id, request_hash);
