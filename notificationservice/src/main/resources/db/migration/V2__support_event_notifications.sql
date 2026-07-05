ALTER TABLE notifications ALTER COLUMN account_id DROP NOT NULL;

ALTER TABLE notifications ADD COLUMN source_event_id VARCHAR(64);

CREATE INDEX idx_notifications_source_event_id
	ON notifications (source_event_id);
