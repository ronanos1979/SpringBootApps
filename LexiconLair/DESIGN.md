# Design of Lexicon Lair

## Mermaid Script

This is mermaid syntax

## Entity Relationship Diagram

```mermaid
erDiagram
    USER {
        int id PK
        string username
        string password
        string email
    }

    BOOK {
        int id PK
        string title
        string isbn
        date publishedDate
        int addedByUserId FK
    }

    AUTHOR {
        int id PK
        string firstName
        string lastName
        int addedByUserId FK
    }

    BOOK_AUTHOR {
        int bookId FK
        int authorId FK
    }

    DICTIONARY_ENTRY {
        int id PK
        string entryWord
        string definition
        date dateAdded
        int userId FK
        int bookId FK
    }

    USER ||--o{ BOOK : "adds"
    USER ||--o{ AUTHOR : "adds"
    USER ||--o{ DICTIONARY_ENTRY : "creates"
    BOOK ||--o{ BOOK_AUTHOR : "has"
    AUTHOR ||--o{ BOOK_AUTHOR : "writes"
    BOOK ||--o{ DICTIONARY_ENTRY : "source of"
```

---

## MVC Breakdown

| MVC Tier | Classes |
|---|---|
| **Model** | `User`, `UserRepository`, `UserService` · `Book`, `BookRepository`, `BookService` · `Author`, `AuthorRepository`, `AuthorService` · `DictionaryEntry`, `DictionaryEntryRepository`, `DictionaryService` |
| **View** | `welcome.jsp` · `listBooks.jsp`, `addBook.jsp` · `listAuthors.jsp`, `addAuthor.jsp` · `listUsers.jsp`, `addUser.jsp` · `listDictionaryEntries.jsp`, `addDictionaryEntry.jsp` |
| **Controller** | `WelcomeController`, `BookControllerJPA`, `AuthorControllerJPA`, `UserControllerJPA`, `DictionaryEntryControllerJPA` |

> **Note:** Controllers should call a Service, which calls the Repository — not the Repository directly. This keeps business logic out of the Controller and makes each layer independently testable.

```mermaid
flowchart LR

    A[Web Browser] -->|HTTP| C

    subgraph C["Controller"]
        C1[WelcomeController]
        C2[DictionaryEntryControllerJPA]
        C3[BookControllerJPA]
        C4[AuthorControllerJPA]
        C5[UserControllerJPA]
    end

    subgraph M["Model"]
        direction TB
        subgraph Entities["Entities"]
            M1[DictionaryEntry]
            M4[Book]
            M7[Author]
            M10[User]
        end
        subgraph Services["Services"]
            M3[DictionaryService]
            M6[BookService]
            M9[AuthorService]
            M12[UserService]
        end
        subgraph Repositories["Repositories"]
            M2[DictionaryEntryRepository]
            M5[BookRepository]
            M8[AuthorRepository]
            M11[UserRepository]
        end
    end

    subgraph V["View"]
        V1[welcome.jsp]
        V2[listDictionaryEntries.jsp]
        V3[addDictionaryEntry.jsp]
        V4[listBooks.jsp]
        V5[addBook.jsp]
        V6[listAuthors.jsp]
        V7[addAuthor.jsp]
        V8[listUsers.jsp]
        V9[addUser.jsp]
    end

    C --> Services
    Services --> Repositories
    Repositories -->|ORM| Entities
    Entities --> DB[(Database)]
    C --> V
    M3 -->|REST| EXT[dictionaryapi.dev]
```

---

## Mermaid Script

This is mermaid syntax

```mermaid
flowchart TD

%% ===== CLIENT =====
subgraph CLIENT["Client Layer"]
    A[Web Browser]
end

%% ===== SPRING BOOT APP =====
subgraph APP["Spring Boot MVC Application (port 8080)"]

    %% SECURITY
    subgraph SECURITY["Security Layer"]
        SEC["SpringSecurityConfiguration
        InMemoryUserDetailsManager
        Users: ronan, ronan2 (BCrypt)"]
    end

    %% CONTROLLERS
    subgraph CONTROLLER["Controller Layer"]
        C1["WelcomeController
        GET /"]
        C2["DictionaryEntryControllerJPA
        GET  /list-dictionary-entries
        GET  /add-dictionary-entry
        POST /add-dictionary-entry
        GET  /update-dictionary-entry?id
        POST /update-dictionary-entry
        GET  /delete-dictionary-entry?id"]
        C3["BookControllerJPA
        GET  /list-books
        GET  /add-book
        POST /add-book
        GET  /update-book?id
        POST /update-book
        GET  /delete-book?id"]
        C4["AuthorControllerJPA
        GET  /list-authors
        GET  /add-author
        POST /add-author
        GET  /update-author?id
        POST /update-author
        GET  /delete-author?id"]
        C5["UserControllerJPA
        GET  /list-users
        GET  /add-user
        POST /add-user
        GET  /update-user?id
        POST /update-user
        GET  /delete-user?id"]
    end

    %% SERVICES
    subgraph SERVICE["Service Layer"]
        S1["DictionaryService
        getMeaning(word)
        → dictionaryapi.dev"]
        S2["BookService"]
        S3["AuthorService"]
        S4["UserService"]
    end

    %% REPOSITORIES
    subgraph REPO["Repository Layer"]
        R1["DictionaryEntryRepository
        JpaRepository&lt;DictionaryEntry, Integer&gt;"]
        R2["BookRepository
        JpaRepository&lt;Book, Integer&gt;"]
        R3["AuthorRepository
        JpaRepository&lt;Author, Integer&gt;"]
        R4["UserRepository
        JpaRepository&lt;User, Integer&gt;"]
    end

    %% ENTITIES
    subgraph ENTITY["Domain Entities"]
        E1["DictionaryEntry
        id, entryWord, definition
        dateAdded, userId, bookId"]
        E2["Book
        id, title, isbn
        publishedDate, addedByUserId"]
        E3["Author
        id, firstName, lastName
        addedByUserId"]
        E4["User
        id, username, password, email"]
        E5["BookAuthor (join)
        bookId, authorId"]
    end

    %% VIEWS
    subgraph VIEWS["JSP Views (WEB-INF/jsp)"]
        V1[welcome.jsp]
        V2[listDictionaryEntries.jsp]
        V3[addDictionaryEntry.jsp]
        V4[listBooks.jsp]
        V5[addBook.jsp]
        V6[listAuthors.jsp]
        V7[addAuthor.jsp]
        V8[listUsers.jsp]
        V9[addUser.jsp]
    end

    %% CONFIG
    subgraph CONFIG["Configuration"]
        CFG["AppConfig
        @Bean RestTemplate"]
    end
end

%% ===== DATABASE =====
subgraph DATA["Database (active profile: postgresql)"]
    DB[("PostgreSQL : 5432
    — or —
    MySQL : 3306
    lexiconlair DB")]
end

%% ===== EXTERNAL =====
subgraph EXT["External Systems"]
    EXT1["dictionaryapi.dev
    /api/v2/entries/en/{word}"]
end

%% ===== FLOW =====
A -->|"HTTP request"| SEC
SEC -->|"Form login / session"| C1
SEC -->|"Form login / session"| C2
SEC -->|"Form login / session"| C3
SEC -->|"Form login / session"| C4
SEC -->|"Form login / session"| C5

C1 --> V1
C2 --> V2
C2 --> V3
C3 --> V4
C3 --> V5
C4 --> V6
C4 --> V7
C5 --> V8
C5 --> V9

C2 --> S1
C3 --> S2
C4 --> S3
C5 --> S4

S1 --> R1
S2 --> R2
S3 --> R3
S4 --> R4

R1 -->|"ORM"| E1
R2 -->|"ORM"| E2
R3 -->|"ORM"| E3
R4 -->|"ORM"| E4
E2 --- E5
E3 --- E5
E5 -->|"join table"| DB
E1 --> DB
E2 --> DB
E3 --> DB
E4 --> DB

CFG -->|"provides RestTemplate"| S1
S1 -->|"REST GET"| EXT1

%% ===== STYLING =====
classDef client fill:#e3f2fd,stroke:#1e88e5,stroke-width:2px,color:#000;
classDef security fill:#fff8e1,stroke:#fdd835,stroke-width:2px,color:#000;
classDef controller fill:#fff3e0,stroke:#fb8c00,stroke-width:2px,color:#000;
classDef service fill:#e8f5e9,stroke:#43a047,stroke-width:2px,color:#000;
classDef repo fill:#ede7f6,stroke:#5e35b1,stroke-width:2px,color:#000;
classDef entity fill:#e0f7fa,stroke:#00897b,stroke-width:2px,color:#000;
classDef view fill:#fce4ec,stroke:#e53935,stroke-width:2px,color:#000;
classDef config fill:#f1f8e9,stroke:#7cb342,stroke-width:2px,color:#000;
classDef db fill:#f3e5f5,stroke:#8e24aa,stroke-width:2px,color:#000;
classDef ext fill:#fbe9e7,stroke:#f4511e,stroke-width:2px,color:#000;

class A client;
class SEC security;
class C1,C2,C3,C4,C5 controller;
class S1,S2,S3,S4 service;
class R1,R2,R3,R4 repo;
class E1,E2,E3,E4,E5 entity;
class V1,V2,V3,V4,V5,V6,V7,V8,V9 view;
class CFG config;
class DB db;
class EXT1 ext;
```
