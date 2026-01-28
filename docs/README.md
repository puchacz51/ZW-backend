# System Zarządzania Projektami - Dokumentacja Techniczna

## 1. Wprowadzenie

System Zarządzania Projektami to aplikacja webowa typu REST API zbudowana w oparciu o Spring Boot 3.4, umożliwiająca zarządzanie projektami, zadaniami i współpracę zespołową.

## 2. Architektura Systemu

### 2.1 Stos Technologiczny

| Warstwa | Technologia |
|---------|-------------|
| Backend | Spring Boot 3.4.4 |
| Język | Java 23 |
| Baza danych | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Autentykacja | JWT (JSON Web Tokens) |
| Dokumentacja API | OpenAPI 3.0 / Swagger UI |
| WebSocket | Spring WebSocket |
| Email | Spring Mail + Thymeleaf |

### 2.2 Wzorce Projektowe

- **MVC (Model-View-Controller)** - separacja logiki biznesowej
- **Repository Pattern** - warstwa dostępu do danych
- **DTO Pattern** - transfer danych między warstwami
- **Service Layer** - logika biznesowa
- **Builder Pattern** - konstruowanie obiektów (Lombok @Builder)

## 3. Schemat Bazy Danych

### 3.1 Diagram ERD

```
┌─────────────┐       ┌─────────────────┐       ┌─────────────┐
│   USERS     │       │  PROJECT_USER   │       │  PROJECTS   │
├─────────────┤       ├─────────────────┤       ├─────────────┤
│ id (PK)     │◄──────│ user_id (FK)    │       │ id (PK)     │
│ first_name  │       │ project_id (FK) │──────►│ name        │
│ last_name   │       │ role            │       │ description │
│ email (UQ)  │       │ assigned_at     │       │ status      │
│ password    │       └─────────────────┘       │ start_date  │
│ role        │                                 │ end_date    │
│ created_at  │◄────────────────────────────────│ created_by  │
└─────────────┘                                 └─────────────┘
      │                                               │
      │                                               │
      ▼                                               ▼
┌─────────────┐                               ┌─────────────┐
│   TASKS     │                               │   PROJECT   │
├─────────────┤                               │  COMMENTS   │
│ id (PK)     │                               ├─────────────┤
│ name        │                               │ id (PK)     │
│ description │                               │ project_id  │
│ status      │                               │ user_id     │
│ due_date    │                               │ content     │
│ project_id  │◄──────────────────────────────│ created_at  │
│ assigned_to │                               │ updated_at  │
│ created_at  │                               └─────────────┘
└─────────────┘
      │
      ▼
┌─────────────┐       ┌─────────────────┐       ┌─────────────┐
│    TASK     │       │  NOTIFICATIONS  │       │ AUDIT_LOGS  │
│  COMMENTS   │       ├─────────────────┤       ├─────────────┤
├─────────────┤       │ id (PK)         │       │ id (PK)     │
│ id (PK)     │       │ user_id (FK)    │       │ user_id     │
│ task_id     │       │ message         │       │ action      │
│ user_id     │       │ read            │       │ entity      │
│ content     │       │ type            │       │ entity_id   │
│ created_at  │       │ created_at      │       │ timestamp   │
│ updated_at  │       └─────────────────┘       └─────────────┘
└─────────────┘
```

### 3.2 Normalizacja

Wszystkie tabele są znormalizowane do **3NF (Trzeciej Postaci Normalnej)**:

1. **1NF**: Wszystkie atrybuty są atomowe, każda tabela ma klucz główny
2. **2NF**: Wszystkie atrybuty niekluczowe zależą od całego klucza głównego
3. **3NF**: Brak zależności przechodnich

Tabela `project_user` jest w **BCNF** z kluczem złożonym `(project_id, user_id)`.

### 3.3 Więzy Integralności

| Typ więzu | Opis |
|-----------|------|
| **PRIMARY KEY** | Unikalne identyfikatory rekordów |
| **FOREIGN KEY** | Relacje między tabelami |
| **UNIQUE** | Unikalne wartości (email, nazwy plików) |
| **NOT NULL** | Wymagane pola |
| **CHECK** | Walidacja wartości (statusy, formaty) |

### 3.4 Indeksy

Utworzono indeksy dla:
- Kluczy obcych (optymalizacja JOIN)
- Pól często wyszukiwanych (email, status)
- Pól sortowania (created_at, timestamp)

### 3.5 Wyzwalacze (Triggers)

1. **trg_project_comment_update** - automatyczna aktualizacja `updated_at`
2. **trg_task_comment_update** - automatyczna aktualizacja `updated_at`
3. **trg_project_date_validation** - walidacja dat projektu
4. **trg_audit_project_create** - automatyczne logowanie tworzenia projektów

## 4. Moduły Systemu

### 4.1 Moduł Autentykacji
- Rejestracja użytkowników
- Logowanie (JWT)
- Odświeżanie tokenów
- Resetowanie hasła (email)

### 4.2 Moduł Projektów
- CRUD projektów
- Zarządzanie członkami zespołu
- Role w projekcie (OWNER, MEMBER, VIEWER)

### 4.3 Moduł Zadań
- CRUD zadań
- Statusy: TODO, IN_PROGRESS, REVIEW, DONE
- Przypisywanie do użytkowników

### 4.4 Moduł Komentarzy
- Komentarze do projektów
- Komentarze do zadań

### 4.5 Moduł Plików
- Upload plików do projektów
- Pobieranie plików
- Walidacja typów i rozmiarów

### 4.6 Moduł Powiadomień
- Powiadomienia systemowe
- Oznaczanie jako przeczytane
- Asynchroniczne tworzenie

### 4.7 Moduł Audytu
- Logowanie wszystkich operacji
- Historia zmian
- Statystyki aktywności

### 4.8 Moduł Czatu
- WebSocket dla komunikacji w czasie rzeczywistym
- Historia wiadomości

## 5. Bezpieczeństwo

### 5.1 Autentykacja
- JWT (JSON Web Tokens)
- Access Token (1h) + Refresh Token (24h)
- BCrypt do hashowania haseł

### 5.2 Autoryzacja
- Role: USER, ADMIN
- Role projektowe: OWNER, MEMBER, VIEWER
- Spring Security @PreAuthorize

### 5.3 Walidacja
- Bean Validation (JSR-380)
- Walidacja na poziomie encji
- Walidacja DTO

## 6. API Endpoints

### Autentykacja
| Metoda | Endpoint | Opis |
|--------|----------|------|
| POST | /api/auth/register | Rejestracja |
| POST | /api/auth/login | Logowanie |
| POST | /api/auth/refresh | Odświeżenie tokenu |
| POST | /api/auth/forgot-password | Reset hasła |

### Projekty
| Metoda | Endpoint | Opis |
|--------|----------|------|
| GET | /api/projects | Lista projektów |
| POST | /api/projects | Tworzenie projektu |
| GET | /api/projects/{id} | Szczegóły projektu |
| PUT | /api/projects/{id} | Aktualizacja |
| DELETE | /api/projects/{id} | Usunięcie |

### Zadania
| Metoda | Endpoint | Opis |
|--------|----------|------|
| GET | /api/tasks | Lista zadań |
| POST | /api/tasks | Tworzenie zadania |
| GET | /api/tasks/{id} | Szczegóły zadania |
| PUT | /api/tasks/{id} | Aktualizacja |
| DELETE | /api/tasks/{id} | Usunięcie |

### Powiadomienia
| Metoda | Endpoint | Opis |
|--------|----------|------|
| GET | /api/notifications | Lista powiadomień |
| PUT | /api/notifications/{id}/read | Oznacz jako przeczytane |
| DELETE | /api/notifications/{id} | Usuń powiadomienie |

### Audit (Admin)
| Metoda | Endpoint | Opis |
|--------|----------|------|
| GET | /api/audit | Lista logów |
| GET | /api/audit/user/{userId} | Logi użytkownika |
| GET | /api/audit/entity/{entity}/{id} | Logi encji |

## 7. Spójność Danych

### 7.1 Kaskadowe Usuwanie
- Usunięcie projektu → usunięcie zadań, komentarzy, plików
- Usunięcie zadania → usunięcie komentarzy zadania
- Usunięcie użytkownika → zachowanie/usunięcie powiązanych danych

### 7.2 Transakcje
- `@Transactional` dla operacji modyfikujących
- `@Transactional(readOnly = true)` dla odczytów
- Rollback przy błędach

### 7.3 Walidacja Biznesowa
- Data końca projektu >= data początku
- Przypisanie zadania tylko do członka projektu
- Edycja komentarza tylko przez autora

## 8. Funkcjonalności PostgreSQL

### 8.1 Wykorzystane funkcje
- Sekwencje (BIGSERIAL)
- Indeksy (B-tree)
- Ograniczenia CHECK
- Triggery
- Procedury składowane
- Widoki materializowane

### 8.2 Przykładowe widoki
- `v_project_statistics` - statystyki projektów
- `v_user_activity` - aktywność użytkowników
- `v_overdue_tasks` - zadania po terminie

## 9. Uruchomienie

### 9.1 Wymagania
- Java 23+
- PostgreSQL 15+
- Gradle 8+

### 9.2 Konfiguracja
```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydatabase
spring.datasource.username=user
spring.datasource.password=secret
```

### 9.3 Uruchomienie
```bash
./gradlew bootRun
```

### 9.4 Dokumentacja API
Po uruchomieniu: http://localhost:8080/swagger-ui.html

## 10. Testy

Projekt zawiera:
- Testy jednostkowe (JUnit 5)
- Testy integracyjne (TestContainers)
- Testy kontrolerów (MockMvc)
- Testy repozytoriów

```bash
./gradlew test
```
