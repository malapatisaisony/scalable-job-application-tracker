CREATE TABLE job_applications (
                                  id BIGSERIAL PRIMARY KEY,

                                  user_id BIGINT NOT NULL,

                                  company_name VARCHAR(255) NOT NULL,

                                  job_title VARCHAR(255) NOT NULL,

                                  job_url TEXT,

                                  location VARCHAR(255),

                                  status VARCHAR(50) NOT NULL DEFAULT 'SAVED',

                                  applied_at TIMESTAMPTZ,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_job_application_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(id)
                                          ON DELETE CASCADE
);

CREATE INDEX idx_job_applications_user_id
    ON job_applications(user_id);

CREATE INDEX idx_job_applications_status
    ON job_applications(status);