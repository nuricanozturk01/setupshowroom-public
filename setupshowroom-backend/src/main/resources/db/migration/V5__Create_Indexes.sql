CREATE INDEX idx_comment_setup_deleted ON comment(setup_id, deleted);
CREATE INDEX idx_comment_created_id ON comment(created_at, id);

CREATE INDEX idx_commentlike_comment_user ON comment_like(comment_id, user_id);
CREATE INDEX idx_commentlike_comment_deleted ON comment_like(comment_id, deleted);
