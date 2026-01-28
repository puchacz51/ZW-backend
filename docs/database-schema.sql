-- =============================================================================
-- SCHEMAT BAZY DANYCH - SYSTEM ZARZĄDZANIA PROJEKTAMI
-- Znormalizowany schemat z więzami integralności i indeksami
-- =============================================================================

-- -----------------------------------------------------------------------------
-- TABELA: users (Użytkownicy systemu)
-- Normalizacja: 3NF - wszystkie atrybuty zależą tylko od klucza głównego
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    avatar_file_name VARCHAR(255),
    avatar_content_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Więzy integralności
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_first_name_length CHECK (LENGTH(first_name) >= 1),
    CONSTRAINT chk_last_name_length CHECK (LENGTH(last_name) >= 1)
);

-- Indeksy dla users
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_created_at ON users(created_at);

-- -----------------------------------------------------------------------------
-- TABELA: projects (Projekty)
-- Normalizacja: 3NF - created_by jest kluczem obcym, nie ma zależności przechodnich
-- -----------------------------------------------------------------------------
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' 
        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED')),
    created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at DATE DEFAULT CURRENT_DATE,
    
    -- Więzy integralności
    CONSTRAINT chk_project_name_length CHECK (LENGTH(name) >= 1),
    CONSTRAINT chk_project_dates CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);

-- Indeksy dla projects
CREATE INDEX idx_project_created_by ON projects(created_by);
CREATE INDEX idx_project_status ON projects(status);
CREATE INDEX idx_project_created_at ON projects(created_at);
CREATE INDEX idx_project_name ON projects(name);

-- -----------------------------------------------------------------------------
-- TABELA: tasks (Zadania w projektach)
-- Normalizacja: 3NF - każde pole zależy tylko od klucza głównego
-- -----------------------------------------------------------------------------
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL CHECK (status IN ('TODO', 'IN_PROGRESS', 'REVIEW', 'DONE')),
    due_date DATE,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    assigned_to BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at DATE DEFAULT CURRENT_DATE,
    
    -- Więzy integralności
    CONSTRAINT chk_task_name_length CHECK (LENGTH(name) >= 1)
);

-- Indeksy dla tasks
CREATE INDEX idx_task_project ON tasks(project_id);
CREATE INDEX idx_task_assigned_to ON tasks(assigned_to);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_due_date ON tasks(due_date);

-- -----------------------------------------------------------------------------
-- TABELA: project_user (Relacja wiele-do-wielu: Projekty - Użytkownicy)
-- Normalizacja: BCNF - klucz złożony, każdy atrybut zależy od całego klucza
-- -----------------------------------------------------------------------------
CREATE TABLE project_user (
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'MEMBER', 'VIEWER')),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (project_id, user_id)
);

-- Indeksy dla project_user
CREATE INDEX idx_project_user_project ON project_user(project_id);
CREATE INDEX idx_project_user_user ON project_user(user_id);
CREATE INDEX idx_project_user_role ON project_user(role);

-- -----------------------------------------------------------------------------
-- TABELA: project_comments (Komentarze do projektów)
-- Normalizacja: 3NF
-- -----------------------------------------------------------------------------
CREATE TABLE project_comments (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT chk_comment_content_length CHECK (LENGTH(content) >= 1)
);

-- Indeksy dla project_comments
CREATE INDEX idx_project_comment_project ON project_comments(project_id);
CREATE INDEX idx_project_comment_user ON project_comments(user_id);
CREATE INDEX idx_project_comment_created_at ON project_comments(created_at);

-- -----------------------------------------------------------------------------
-- TABELA: task_comments (Komentarze do zadań)
-- Normalizacja: 3NF
-- -----------------------------------------------------------------------------
CREATE TABLE task_comments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content VARCHAR(500) NOT NULL,
    created_at DATE DEFAULT CURRENT_DATE,
    updated_at TIMESTAMP,
    
    CONSTRAINT chk_task_comment_content_length CHECK (LENGTH(content) >= 1)
);

-- Indeksy dla task_comments
CREATE INDEX idx_task_comment_task ON task_comments(task_id);
CREATE INDEX idx_task_comment_user ON task_comments(user_id);
CREATE INDEX idx_task_comment_created_at ON task_comments(created_at);

-- -----------------------------------------------------------------------------
-- TABELA: project_files (Pliki projektów)
-- Normalizacja: 3NF
-- -----------------------------------------------------------------------------
CREATE TABLE project_files (
    id BIGSERIAL PRIMARY KEY,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(500) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size > 0),
    description VARCHAR(1000),
    uploaded_by BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indeksy dla project_files
CREATE INDEX idx_project_file_project ON project_files(project_id);
CREATE INDEX idx_project_file_uploaded_by ON project_files(uploaded_by);
CREATE INDEX idx_project_file_upload_date ON project_files(upload_date);
CREATE UNIQUE INDEX idx_project_file_stored_name ON project_files(stored_file_name);

-- -----------------------------------------------------------------------------
-- TABELA: messages (Wiadomości czatu)
-- Normalizacja: 3NF
-- -----------------------------------------------------------------------------
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content VARCHAR(1000) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_message_content_length CHECK (LENGTH(content) >= 1)
);

-- Indeksy dla messages
CREATE INDEX idx_message_sender ON messages(sender_id);
CREATE INDEX idx_message_timestamp ON messages(timestamp);

-- -----------------------------------------------------------------------------
-- TABELA: notifications (Powiadomienia)
-- Normalizacja: 3NF
-- -----------------------------------------------------------------------------
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message VARCHAR(500) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    type VARCHAR(50),
    related_entity_id BIGINT,
    related_entity_type VARCHAR(100),
    created_at DATE DEFAULT CURRENT_DATE,
    
    CONSTRAINT chk_notification_message_length CHECK (LENGTH(message) >= 1)
);

-- Indeksy dla notifications
CREATE INDEX idx_notification_user ON notifications(user_id);
CREATE INDEX idx_notification_read ON notifications(read);
CREATE INDEX idx_notification_created_at ON notifications(created_at);

-- -----------------------------------------------------------------------------
-- TABELA: audit_logs (Logi audytu)
-- Normalizacja: 3NF - służy do śledzenia wszystkich operacji w systemie
-- -----------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    entity VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    details VARCHAR(1000),
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indeksy dla audit_logs
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity);
CREATE INDEX idx_audit_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_action ON audit_logs(action);

-- -----------------------------------------------------------------------------
-- TABELA: refresh_tokens (Tokeny odświeżania JWT)
-- Normalizacja: 3NF
-- -----------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL
);

-- Indeks dla refresh_tokens
CREATE UNIQUE INDEX idx_refresh_token ON refresh_tokens(token);

-- -----------------------------------------------------------------------------
-- TABELA: password_reset_tokens (Tokeny resetowania hasła)
-- Normalizacja: 3NF
-- -----------------------------------------------------------------------------
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL
);

-- Indeks dla password_reset_tokens
CREATE UNIQUE INDEX idx_password_reset_token ON password_reset_tokens(token);

-- =============================================================================
-- WYZWALACZE (TRIGGERS)
-- =============================================================================

-- Trigger: Automatyczna aktualizacja updated_at dla project_comments
CREATE OR REPLACE FUNCTION update_project_comment_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_project_comment_update
    BEFORE UPDATE ON project_comments
    FOR EACH ROW
    EXECUTE FUNCTION update_project_comment_timestamp();

-- Trigger: Automatyczna aktualizacja updated_at dla task_comments
CREATE OR REPLACE FUNCTION update_task_comment_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_task_comment_update
    BEFORE UPDATE ON task_comments
    FOR EACH ROW
    EXECUTE FUNCTION update_task_comment_timestamp();

-- Trigger: Walidacja dat projektu
CREATE OR REPLACE FUNCTION validate_project_dates()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.end_date IS NOT NULL AND NEW.start_date IS NOT NULL AND NEW.end_date < NEW.start_date THEN
        RAISE EXCEPTION 'Data zakończenia nie może być wcześniejsza niż data rozpoczęcia';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_project_date_validation
    BEFORE INSERT OR UPDATE ON projects
    FOR EACH ROW
    EXECUTE FUNCTION validate_project_dates();

-- Trigger: Automatyczne logowanie audytu przy tworzeniu projektu
CREATE OR REPLACE FUNCTION log_project_creation()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO audit_logs (user_id, action, entity, entity_id, timestamp)
    VALUES (NEW.created_by, 'CREATE', 'Project', NEW.id, CURRENT_TIMESTAMP);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_project_create
    AFTER INSERT ON projects
    FOR EACH ROW
    EXECUTE FUNCTION log_project_creation();

-- =============================================================================
-- WIDOKI (VIEWS) - funkcjonalność serwera BD
-- =============================================================================

-- Widok: Statystyki projektów
CREATE OR REPLACE VIEW v_project_statistics AS
SELECT 
    p.id AS project_id,
    p.name AS project_name,
    p.status,
    COUNT(DISTINCT t.id) AS total_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'DONE' THEN t.id END) AS completed_tasks,
    COUNT(DISTINCT pu.user_id) AS team_members,
    COUNT(DISTINCT pc.id) AS total_comments,
    COUNT(DISTINCT pf.id) AS total_files
FROM projects p
LEFT JOIN tasks t ON t.project_id = p.id
LEFT JOIN project_user pu ON pu.project_id = p.id
LEFT JOIN project_comments pc ON pc.project_id = p.id
LEFT JOIN project_files pf ON pf.project_id = p.id
GROUP BY p.id, p.name, p.status;

-- Widok: Aktywność użytkowników
CREATE OR REPLACE VIEW v_user_activity AS
SELECT 
    u.id AS user_id,
    u.email,
    CONCAT(u.first_name, ' ', u.last_name) AS full_name,
    COUNT(DISTINCT p.id) AS projects_created,
    COUNT(DISTINCT t.id) AS tasks_assigned,
    COUNT(DISTINCT al.id) AS recent_actions
FROM users u
LEFT JOIN projects p ON p.created_by = u.id
LEFT JOIN tasks t ON t.assigned_to = u.id
LEFT JOIN audit_logs al ON al.user_id = u.id AND al.timestamp > NOW() - INTERVAL '7 days'
GROUP BY u.id, u.email, u.first_name, u.last_name;

-- Widok: Zadania z opóźnieniem
CREATE OR REPLACE VIEW v_overdue_tasks AS
SELECT 
    t.id AS task_id,
    t.name AS task_name,
    t.due_date,
    t.status,
    p.name AS project_name,
    CONCAT(u.first_name, ' ', u.last_name) AS assigned_to_name,
    u.email AS assigned_to_email,
    CURRENT_DATE - t.due_date AS days_overdue
FROM tasks t
JOIN projects p ON p.id = t.project_id
LEFT JOIN users u ON u.id = t.assigned_to
WHERE t.due_date < CURRENT_DATE 
  AND t.status != 'DONE'
ORDER BY days_overdue DESC;

-- =============================================================================
-- PROCEDURY SKŁADOWANE - funkcjonalność serwera BD
-- =============================================================================

-- Procedura: Archiwizacja starych logów audytu
CREATE OR REPLACE PROCEDURE archive_old_audit_logs(days_to_keep INTEGER)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM audit_logs 
    WHERE timestamp < CURRENT_TIMESTAMP - (days_to_keep || ' days')::INTERVAL;
    
    RAISE NOTICE 'Usunięto logi audytu starsze niż % dni', days_to_keep;
END;
$$;

-- Procedura: Przypisanie użytkownika do projektu z powiadomieniem
CREATE OR REPLACE PROCEDURE assign_user_to_project(
    p_project_id BIGINT,
    p_user_id BIGINT,
    p_role VARCHAR(20)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_project_name VARCHAR(255);
BEGIN
    -- Pobierz nazwę projektu
    SELECT name INTO v_project_name FROM projects WHERE id = p_project_id;
    
    -- Przypisz użytkownika
    INSERT INTO project_user (project_id, user_id, role)
    VALUES (p_project_id, p_user_id, p_role)
    ON CONFLICT (project_id, user_id) DO UPDATE SET role = EXCLUDED.role;
    
    -- Utwórz powiadomienie
    INSERT INTO notifications (user_id, message, type, related_entity_id, related_entity_type)
    VALUES (p_user_id, 
            'Zostałeś dodany do projektu: ' || v_project_name, 
            'PROJECT_ASSIGNMENT',
            p_project_id,
            'Project');
    
    RAISE NOTICE 'Użytkownik % przypisany do projektu % z rolą %', p_user_id, p_project_id, p_role;
END;
$$;

-- =============================================================================
-- KOMENTARZ O NORMALIZACJI
-- =============================================================================
-- Wszystkie tabele są w 3NF (Trzeciej Postaci Normalnej):
-- 1. 1NF: Wszystkie atrybuty są atomowe, każda tabela ma klucz główny
-- 2. 2NF: Wszystkie atrybuty niekluczowe zależą od całego klucza głównego
-- 3. 3NF: Brak zależności przechodnich - każdy atrybut niekluczowy zależy 
--         bezpośrednio od klucza głównego
--
-- Tabela project_user jest w BCNF (Boyce-Codd Normal Form) z kluczem złożonym
--
-- Integralność referencyjna zapewniona przez:
-- - ON DELETE CASCADE - usuwanie powiązanych rekordów
-- - ON DELETE SET NULL - ustawienie NULL przy usunięciu referencji
-- - ON DELETE RESTRICT - blokada usunięcia gdy istnieją referencje
-- =============================================================================
