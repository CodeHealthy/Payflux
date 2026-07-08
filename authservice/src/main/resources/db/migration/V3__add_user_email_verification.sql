ALTER TABLE users
	ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_users_email_verified
	ON users (email_verified);
