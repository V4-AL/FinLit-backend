

# SpringBoot Learning Demo - Backend Documentation

**Project Version:** 0.0.1-SNAPSHOT  
**Java Version:** 25  
**Spring Boot Version:** 3.5.15  
**Database:** PostgreSQL (Neon)

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Maven Configuration (pom.xml)](#maven-configuration)
3. [Application Properties](#application-properties)
4. [Entity Models](#entity-models)
5. [Database Repositories](#database-repositories)
6. [Service Layer](#service-layer)
7. [REST Controllers](#rest-controllers)
8. [Components](#components)
9. [Architecture & Data Flow](#architecture--data-flow)

---

## Project Overview

This is a **Spring Boot learning platform backend** that manages educational modules and lessons. The application provides:
- User management (CRUD operations)
- Module and Lesson storage
- User progress tracking
- RESTful APIs for course content

**Core Stack:**
- Framework: Spring Boot 3.5.15
- ORM: JPA/Hibernate
- Database: PostgreSQL (Neon)
- Build Tool: Maven
- Annotations: Lombok (reduces boilerplate code)
- AWS Integration: S3 support for media storage

---

## Maven Configuration

### File: `pom.xml`

#### 1. **Project Definition** (Lines 2-28)
```xml
<groupId>com.example</groupId>
<artifactId>demo</artifactId>
<version>0.0.1-SNAPSHOT</version>
```
- **groupId:** Defines the organization/company namespace (`com.example`)
- **artifactId:** Unique project identifier (`demo`)
- **version:** Current release version (`0.0.1-SNAPSHOT` = development version, not production-ready)
- **Parent:** Inherits from `spring-boot-starter-parent:3.5.15` (provides Spring Boot defaults)

#### 2. **Java Compiler Configuration** (Line 30)
```xml
<java.version>25</java.version>
```
- Targets Java 25 (latest LTS-adjacent version with modern features like virtual threads, records)

#### 3. **Database Properties** (Lines 31-38)
```xml
<spring.datasource.url>jdbc:postgresql://your-neon-host:5432/your-db-name</spring.datasource.url>
<spring.datasource.username>your-username</spring.datasource.username>
<spring.datasource.password>your-password</spring.datasource.password>
<spring.jpa.hibernate.ddl-auto>update</spring.jpa.hibernate.ddl-auto>
```
- **datasource.url:** PostgreSQL connection string (Neon serverless database)
- **datasource.username/password:** Authentication credentials (placeholder values - replace before deployment)
- **hibernate.ddl-auto:** `update` strategy = automatically create/modify tables based on entity annotations
- **show-sql:** `true` = logs all SQL queries to console (useful for debugging)
- **driver-class-name:** PostgreSQL JDBC driver

#### 4. **Repositories** (Lines 42-59)
Maven repositories for downloading dependencies:
- **Maven Central:** Primary repository for most Java libraries
- **Spring Releases:** Official Spring Framework releases

#### 5. **Dependencies** (Lines 71-115)

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter` | Core Spring Boot functionality |
| `spring-boot-starter-web` | REST API support (embedded Tomcat server) |
| `spring-boot-starter-data-jpa` | JPA/Hibernate ORM for database operations |
| `postgresql:postgresql` | PostgreSQL JDBC driver (runtime scope only) |
| `org.projectlombok:lombok` | Annotation processor: auto-generates getters/setters/equals/hashCode |
| `spring-boot-starter-test` | Testing framework (JUnit, Mockito) |
| `software.amazon.awssdk:s3` | AWS S3 integration for file uploads |
| `software.amazon.awssdk:s3-presigner` | Generate pre-signed S3 URLs for secure file access |

#### 6. **Build Plugins** (Lines 118-137)
- **maven-compiler-plugin:** Compiles source code with Lombok annotation processors
- **spring-boot-maven-plugin:** Creates executable JAR and enables `mvn spring-boot:run`

---

## Application Properties

### File: `src/main/resources/application.properties`

```properties
# ========== H2 CONSOLE (In-Memory Database) ==========
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```
- Enables web UI for H2 database at `http://localhost:8080/h2-console`
- Currently commented out because app uses PostgreSQL

```properties
# ========== SERVER CONFIGURATION ==========
server.port=8080
```
- Application runs on port 8080 (accessible at `http://localhost:8080`)

```properties
# ========== DATABASE CONFIGURATION ==========
spring.datasource.url=jdbc:postgresql://<your-neon-host>:5432/<your-db-name>?sslmode=require
spring.datasource.username=<your-username>
spring.datasource.password=<your-password>

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.datasource.driver-class-name=org.postgresql.Driver
```
- **ddl-auto=update:** Auto-creates/updates tables without dropping existing data
- **show-sql=true:** Prints all SQL queries to console (disable in production for performance)
- **sslmode=require:** Enforces SSL encryption for database connections (Neon requirement)

**⚠️ TODO:** Replace `<your-neon-host>`, `<your-username>`, and `<your-password>` with actual credentials

---

## Entity Models

### 1. **User** (`src/main/java/com/example/demo/model/User.java`)

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    // getters/setters...
}
```

**Annotations Breakdown:**
- `@Entity`: Maps class to database table `user` (auto-generated from class name)
- `@Id`: Marks `id` as primary key
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Auto-increment ID (database generates next value)

**Fields:**
- `id`: Unique identifier, auto-incremented
- `username`: User's login name (required)
- `email`: User's email address (required)

**Database Table:**
```
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255),
    email VARCHAR(255)
);
```

---

### 2. **Lesson** (`src/main/java/com/example/demo/model/Lesson.java`)

```java
@Entity
@Data  // Lombok: auto-generates getters/setters/equals/hashCode/toString
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    
    @Column(columnDefinition = "LONGTEXT")
    private String content;  // Stores HTML/rich text
    
    private int duration;    // Lesson duration in minutes
    private String mediaUrl; // URL to lesson media (Cloudinary)
    private String cloudinaryPublicId;  // Asset ID for media management
}
```

**Key Fields:**
- `content`: Uses `LONGTEXT` (PostgreSQL: `TEXT`) to store large content blocks (HTML, code, etc.)
- `mediaUrl`: External URL pointing to lesson video/image
- `cloudinaryPublicId`: Reference for managing assets on Cloudinary CDN

---

### 3. **Module** (`src/main/java/com/example/demo/model/Module.java`)

```java
@Entity
@Data
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String moduleId;  // e.g., "module-1"
    
    @Column(name = "module_order")
    private int moduleOrder;  // Sequence within course
    
    private String title;
    
    @Column(columnDefinition = "LONGTEXT")
    private String description;
}
```

**Purpose:** Groups lessons into logical sections (e.g., "Basics", "Advanced Topics")

**Note:** `moduleId` is a custom string ID from JSON file; separate from database-generated `id`

---

### 4. **UserProgress** (`src/main/java/com/example/demo/model/UserProgress.java`)

```java
@Entity
@Data
public class UserProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;  // Temp: replace with @ManyToOne User when adding Spring Security
    private Long lessonId;    // Lesson ID completed
    private boolean completed;  // Completion status
    private LocalDateTime completionDate;  // When lesson was completed
}
```

**Usage:** Tracks which lessons each user has completed

**TODO:** Replace `username` string with `@ManyToOne private User user` for proper foreign key relationship

---

## Database Repositories

Repositories extend `JpaRepository<Entity, ID>` and use **Spring Data JPA** magic methods.

### UserRepository
```java
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);  // Auto-generated query
}
```
- `findByUsername(String)`: Spring auto-generates SQL `SELECT * FROM user WHERE username = ?`
- Inherits CRUD methods: `save()`, `findById()`, `findAll()`, `delete()`, `deleteById()`

### LessonRepository
```java
public interface LessonRepository extends JpaRepository<Lesson, Long> {
}
```
- Minimal interface; Spring generates all standard CRUD operations

### ModuleRepository
```java
public interface ModuleRepository extends JpaRepository<Module, Long> {
}
```
- Same as LessonRepository; provides standard database operations

---

## Service Layer

### UserService
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {  // Constructor injection
        this.userRepository = userRepository;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(@NonNull Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User saveUser(@NonNull User user) {
        return userRepository.save(user);  // Insert or update
    }
    
    public void deleteUser(@NonNull Long id) {
        userRepository.deleteById(id);
    }
}
```

**Key Concepts:**
- `@Service`: Spring bean for business logic
- **Constructor Injection**: Dependencies passed via constructor (better than `@Autowired` field injection)
- `@NonNull`: Validates parameter isn't null (Lombok compiler checks)
- `.orElseThrow()`: Converts Optional to exception if value missing
- Methods handle all CRUD operations + error handling

---

### ProgressService
```java
public class ProgressService {
    // Empty; placeholder for future implementation
}
```
- Currently not implemented; prepared for progress tracking logic

---

## REST Controllers

### GreetingController
```java
@RestController
@RequestMapping("/api/users")
public class GreetingController {
    private final UserService userService;
    
    public GreetingController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public List<User> getAll() {
        return userService.getAllUsers();
    }
    
    @PostMapping
    public User create(@RequestBody @NonNull User user) {
        return userService.saveUser(user);
    }
}
```

**Endpoints:**

| Method | URL | Functionality |
|--------|-----|---|
| GET | `/api/users` | Fetch all users |
| POST | `/api/users` | Create new user (JSON body: `{username, email}`) |

**Request/Response Examples:**

GET `/api/users`
```json
[
  {"id": 1, "username": "john_doe", "email": "john@example.com"},
  {"id": 2, "username": "jane_smith", "email": "jane@example.com"}
]
```

POST `/api/users`
```json
Request Body:
{"username": "bob", "email": "bob@example.com"}

Response (201 Created):
{"id": 3, "username": "bob", "email": "bob@example.com"}
```

---

## Components

### DataLoader
```java
@Component
public class DataLoader {
    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;
    
    @PostConstruct
    public void loadData() throws Exception {
        // Check if data already loaded
        if (lessonRepository.count() > 0) return;
        
        // Read JSON file
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getResourceAsStream(
            "/financial_literacy_lesson.json"
        );
        JsonNode root = mapper.readTree(inputStream);
        
        // Parse modules and lessons from JSON
        JsonNode modules = root.path("course").path("modules");
        modules.forEach(module -> {
            // Create Module entity
            Module moduleEntity = new Module();
            moduleEntity.setModuleId(module.path("id").asText());
            moduleEntity.setModuleOrder(module.path("order").asInt());
            moduleEntity.setTitle(module.path("title").asText());
            moduleEntity.setDescription(module.path("description").asText(""));
            
            // Parse lessons within module
            JsonNode lessonsList = module.path("lessons");
            lessonsList.forEach(lessonNode -> {
                Lesson lesson = new Lesson();
                lesson.setTitle(lessonNode.path("title").asText());
                lesson.setDescription(lessonNode.path("id").asText());
                lesson.setContent(lessonNode.path("content_blocks").toString());
                lesson.setDuration(30);
                // Add to list...
            });
        });
        
        // Persist to database
        moduleRepository.saveAll(modulesToSave);
        lessonRepository.saveAll(lessons);
    }
}
```

**Execution:**
- `@Component`: Spring-managed bean, auto-instantiated at startup
- `@PostConstruct`: Method runs **once** after bean construction, before `main()` returns
- **JSON Parsing:** Uses Jackson `ObjectMapper` to deserialize `financial_literacy_lesson.json`
- **Idempotent Load:** Checks `lessonRepository.count() > 0` to skip re-loading if data exists
- **Data Isolation:** Loads lessons & modules separately; no foreign key relationships in current design

---

## Architecture & Data Flow

### Request-Response Cycle

```
HTTP Request → RestController 
    ↓
    → validate @RequestBody 
    ↓
    → call Service methods
    ↓
    → Repository.save/findAll/etc (database access)
    ↓
    → return Entity/List<Entity>
    ↓
    → serialize to JSON
    ↓
HTTP Response (200/201/error code)
```

### Application Startup Flow

```
1. JVM launches DemoApplication.main()
    ↓
2. @SpringBootApplication scans classpath for @Component, @Service, @Repository, @Controller
    ↓
3. Spring instantiates beans with constructor/setter injection
    ↓
4. DataLoader @PostConstruct runs → loads JSON into database
    ↓
5. Tomcat embedded server starts on port 8080
    ↓
6. REST endpoints ready to accept requests
```

---

## Key Design Patterns

### 1. **Dependency Injection (Constructor-based)**
Services receive dependencies via constructor, not field injection:
```java
public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```
✅ Benefits: Immutable, testable, explicit dependencies

### 2. **Repository Pattern**
Data access abstraction layer:
- `JpaRepository` provides generic CRUD methods
- Custom queries via method names: `findByUsername()` → auto-generates SQL

### 3. **Service Layer**
Business logic separation:
- Controllers delegate to Services
- Services delegate to Repositories
- Enables reuse and testing

### 4. **Component Bean Lifecycle**
`@PostConstruct` initialization:
- Runs once after bean construction
- Ideal for one-time setup (seed data, cache loading)

---

## Configuration Notes

### Database Migration
The project uses **Hibernate DDL auto-update**, meaning:
- `spring.jpa.hibernate.ddl-auto=update`: Modifies existing tables to match entities
- Tables auto-created if they don't exist
- **WARNING:** Use `validate` in production (prevents accidental schema changes)

### Lombok Annotation Processing
Maven compiler configured to process Lombok annotations:
```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </path>
</annotationProcessorPaths>
```
This enables `@Data` to generate getters/setters at compile-time (not runtime).

---

## Future Enhancements

1. **Spring Security**: Add authentication/authorization (replace username string with User FK)
2. **ProgressService Implementation**: Complete progress tracking logic
3. **LessonResponse DTO**: Create response object to filter entity fields
4. **Exception Handling**: Implement `@ControllerAdvice` for global error handling
5. **Logging**: Replace `System.out.println()` with SLF4J
6. **API Validation**: Add `@Valid` annotations for request body validation
7. **Pagination**: Implement `Pageable` for large result sets
8. **Caching**: Add Redis for frequently accessed data

---

## Summary

This Spring Boot application demonstrates:
- ✅ Entity/model layer with JPA annotations
- ✅ Repository pattern for data access
- ✅ Service layer for business logic
- ✅ REST controller for HTTP endpoints
- ✅ Component-based initialization with DataLoader
- ✅ PostgreSQL integration via Neon
- ✅ JSON parsing and persistence

**Starter Status:** Production-ready architecture, but requires security hardening and additional features for real-world deployment.
