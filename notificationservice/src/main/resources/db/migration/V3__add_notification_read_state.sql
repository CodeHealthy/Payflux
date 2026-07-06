ALTER TABLE notifications ADD COLUMN read_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_notifications_owner_user_id_read_at
	ON notifications (owner_user_id, read_at);
