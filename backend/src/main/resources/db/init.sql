CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(120) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(30),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_role (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS operation_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  username VARCHAR(50),
  operation VARCHAR(80) NOT NULL,
  target_type VARCHAR(80),
  target_id BIGINT,
  ip VARCHAR(60),
  result VARCHAR(30) NOT NULL,
  detail VARCHAR(500),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_operation_logs_user_id (user_id),
  INDEX idx_operation_logs_operation (operation),
  INDEX idx_operation_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course_applications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  course_name VARCHAR(100) NOT NULL,
  course_code VARCHAR(50) NOT NULL,
  description VARCHAR(1000),
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  reject_reason VARCHAR(500),
  reviewed_by BIGINT,
  reviewed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_course_applications_teacher_id (teacher_id),
  INDEX idx_course_applications_status (status),
  INDEX idx_course_applications_course_code (course_code),
  CONSTRAINT fk_course_applications_teacher FOREIGN KEY (teacher_id) REFERENCES users(id),
  CONSTRAINT fk_course_applications_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS courses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_name VARCHAR(100) NOT NULL,
  course_code VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(1000),
  owner_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_courses_owner_id (owner_id),
  INDEX idx_courses_status (status),
  CONSTRAINT fk_courses_owner FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course_members (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  member_role VARCHAR(30) NOT NULL,
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status TINYINT NOT NULL DEFAULT 1,
  UNIQUE KEY uk_course_member (course_id, user_id),
  INDEX idx_course_members_user_id (user_id),
  INDEX idx_course_members_status (status),
  CONSTRAINT fk_course_members_course FOREIGN KEY (course_id) REFERENCES courses(id),
  CONSTRAINT fk_course_members_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  receiver_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  type VARCHAR(40) NOT NULL,
  target_type VARCHAR(50),
  target_id BIGINT,
  course_id BIGINT,
  read_status TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_notifications_receiver_id (receiver_id),
  INDEX idx_notifications_course_id (course_id),
  INDEX idx_notifications_target (target_type, target_id),
  INDEX idx_notifications_read_status (read_status),
  CONSTRAINT fk_notifications_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS assignments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(2000),
  assignment_type VARCHAR(30) NOT NULL,
  deadline DATETIME NOT NULL,
  total_score DECIMAL(8,2) NOT NULL,
  allow_late TINYINT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_assignments_course_id (course_id),
  INDEX idx_assignments_status (status),
  INDEX idx_assignments_deadline (deadline),
  CONSTRAINT fk_assignments_course FOREIGN KEY (course_id) REFERENCES courses(id),
  CONSTRAINT fk_assignments_creator FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS questions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assignment_id BIGINT NOT NULL,
  question_type VARCHAR(30) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  options VARCHAR(2000),
  standard_answer VARCHAR(500) NOT NULL,
  score DECIMAL(8,2) NOT NULL,
  INDEX idx_questions_assignment_id (assignment_id),
  CONSTRAINT fk_questions_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS grading_rules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assignment_id BIGINT NOT NULL,
  rule_type VARCHAR(50) NOT NULL,
  condition_expr VARCHAR(1000),
  action_expr VARCHAR(1000),
  enabled TINYINT NOT NULL DEFAULT 1,
  INDEX idx_grading_rules_assignment_id (assignment_id),
  INDEX idx_grading_rules_rule_type (rule_type),
  CONSTRAINT fk_grading_rules_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS submissions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assignment_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  content LONGTEXT,
  submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_late TINYINT NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
  auto_score DECIMAL(8,2),
  final_score DECIMAL(8,2),
  INDEX idx_submissions_assignment_id (assignment_id),
  INDEX idx_submissions_student_id (student_id),
  INDEX idx_submissions_submit_time (submit_time),
  CONSTRAINT fk_submissions_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id),
  CONSTRAINT fk_submissions_student FOREIGN KEY (student_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS file_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(1000) NOT NULL,
  file_size BIGINT NOT NULL,
  file_hash VARCHAR(128) NOT NULL,
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_file_records_submission_id (submission_id),
  INDEX idx_file_records_file_hash (file_hash),
  CONSTRAINT fk_file_records_submission FOREIGN KEY (submission_id) REFERENCES submissions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS assignment_files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assignment_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(1000) NOT NULL,
  file_size BIGINT NOT NULL,
  file_hash VARCHAR(128) NOT NULL,
  uploaded_by BIGINT NOT NULL,
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  INDEX idx_assignment_files_assignment_id (assignment_id),
  INDEX idx_assignment_files_uploaded_by (uploaded_by),
  CONSTRAINT fk_assignment_files_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id),
  CONSTRAINT fk_assignment_files_uploader FOREIGN KEY (uploaded_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS grades (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id BIGINT NOT NULL,
  grader_id BIGINT,
  score DECIMAL(8,2),
  comment VARCHAR(1000),
  graded_at DATETIME,
  grade_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  INDEX idx_grades_submission_id (submission_id),
  INDEX idx_grades_grader_id (grader_id),
  CONSTRAINT fk_grades_submission FOREIGN KEY (submission_id) REFERENCES submissions(id),
  CONSTRAINT fk_grades_grader FOREIGN KEY (grader_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS duplicate_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assignment_id BIGINT NOT NULL,
  file_hash VARCHAR(128) NOT NULL,
  submission_ids VARCHAR(1000) NOT NULL,
  source_submission_id BIGINT,
  matched_submission_id BIGINT,
  source_student_id BIGINT,
  matched_student_id BIGINT,
  detection_type VARCHAR(32) NOT NULL DEFAULT 'EXACT_HASH',
  similarity_score DECIMAL(5,4),
  threshold DECIMAL(5,4) NOT NULL DEFAULT 0.9000,
  detected_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING_REVIEW',
  remark VARCHAR(500),
  UNIQUE KEY uk_duplicate_assignment_hash (assignment_id, file_hash),
  INDEX idx_duplicate_records_assignment_id (assignment_id),
  INDEX idx_duplicate_records_source_submission (source_submission_id),
  INDEX idx_duplicate_records_matched_submission (matched_submission_id),
  INDEX idx_duplicate_records_detection_type (detection_type),
  INDEX idx_duplicate_records_file_hash (file_hash),
  CONSTRAINT fk_duplicate_records_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS export_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(1000) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS',
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_export_records_course_id (course_id),
  INDEX idx_export_records_created_by (created_by),
  CONSTRAINT fk_export_records_course FOREIGN KEY (course_id) REFERENCES courses(id),
  CONSTRAINT fk_export_records_creator FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE notifications ADD COLUMN receiver_id BIGINT;
ALTER TABLE notifications ADD COLUMN read_status TINYINT NOT NULL DEFAULT 0;
ALTER TABLE notifications ADD COLUMN target_type VARCHAR(50);
ALTER TABLE notifications ADD COLUMN target_id BIGINT;
ALTER TABLE notifications ADD COLUMN course_id BIGINT;
UPDATE notifications SET receiver_id = user_id WHERE receiver_id IS NULL;
UPDATE notifications SET read_status = is_read WHERE is_read IS NOT NULL;

ALTER TABLE duplicate_records ADD COLUMN source_submission_id BIGINT;
ALTER TABLE duplicate_records ADD COLUMN matched_submission_id BIGINT;
ALTER TABLE duplicate_records ADD COLUMN source_student_id BIGINT;
ALTER TABLE duplicate_records ADD COLUMN matched_student_id BIGINT;
ALTER TABLE duplicate_records ADD COLUMN detection_type VARCHAR(32) NOT NULL DEFAULT 'EXACT_HASH';
ALTER TABLE duplicate_records ADD COLUMN similarity_score DECIMAL(5,4);
ALTER TABLE duplicate_records ADD COLUMN threshold DECIMAL(5,4) NOT NULL DEFAULT 0.9000;
ALTER TABLE duplicate_records ADD COLUMN remark VARCHAR(500);
ALTER TABLE duplicate_records ADD INDEX idx_duplicate_records_source_submission (source_submission_id);
ALTER TABLE duplicate_records ADD INDEX idx_duplicate_records_matched_submission (matched_submission_id);
ALTER TABLE duplicate_records ADD INDEX idx_duplicate_records_detection_type (detection_type);
UPDATE duplicate_records SET detection_type = 'EXACT_HASH' WHERE detection_type IS NULL;
UPDATE duplicate_records SET threshold = 0.9000 WHERE threshold IS NULL;
UPDATE duplicate_records SET status = 'PENDING_REVIEW' WHERE status = 'SUSPECTED';
