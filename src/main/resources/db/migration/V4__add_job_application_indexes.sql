CREATE INDEX idx_job_applications_user_created_at
    ON job_applications(user_id, created_at DESC);

CREATE INDEX idx_job_applications_user_status_created_at
    ON job_applications(user_id, status, created_at DESC);