# Advent Calendar Backend

A Spring Boot REST API for managing daily challenges with mood-based recommendations, built using clean layered backend architecture.

## Features

- **User Management** - Create and manage users
- **Challenge Assignment** - Assign challenges to users with mood-based recommendations
- **Daily Challenge System** - One challenge per user per day with fallback to low-energy challenges
- **Progress Tracking** - Track completion status and calculate progress percentage
- **Mood-Based Recommendations** - Challenges matched to user's energy level (LOW, NEUTRAL, HIGH)

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.x**
- **Spring Data JPA**
- **PostgreSQL**
- **Maven**
- **SpringDoc OpenAPI** (Swagger UI)

## API Endpoints

### Health
- `GET /health` - Health check endpoint

### Challenges
- `GET /challenges` - Get all challenges
- `GET /challenges/category/{category}` - Get challenges by category
- `GET /challenges/today?userId={id}&mood={mood}` - Get today's challenge based on mood

### Users
- `POST /users` - Create a new user
- `GET /users/{id}` - Get user by ID
- `GET /users` - Get all users

### User Challenges
- `POST /user-challenges/join?userId={id}&challengeId={id}` - Join a challenge
- `GET /user-challenges/user/{userId}` - Get all challenges for a user
- `GET /user-challenges/user/{userId}/status?status={status}` - Get challenges by status
- `GET /user-challenges/user/{userId}/progress` - Get user progress statistics
- `GET /user-challenges/daily?userId={id}` - Get or assign daily challenge (without mood)
- `GET /user-challenges/{id}` - Get specific user challenge
- `PUT /user-challenges/{id}/complete` - Mark challenge as completed
- `PUT /user-challenges/{id}/status?status={status}` - Update challenge status

## Mood-Based Challenge Selection

The system matches user's mood to challenge energy levels:

| Mood | Energy Level | Challenge Type |
|------|--------------|----------------|
| LOW | LOW | Easy, achievable challenges |
| NEUTRAL | MEDIUM | Balanced challenges |
| HIGH | HIGH | Exciting, ambitious challenges |

**Fallback Chain:**
1. Try DAILY challenge with matching energy level
2. Try ANY DAILY challenge
3. Try LOW_ENERGY challenge
4. Throw error if no challenges available

## Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL database

### Database Configuration

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/advent_calendar
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Run the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### API Documentation

Once the application is running, access Swagger UI at:
```
http://localhost:8080/swagger-ui/index.html
```

## Database Schema

```
users
├── id (PK)
├── name
└── email (unique)

challenges
├── id (PK)
├── title
├── description
├── category (EXPLORE_CITY, TREND_BASED, CAMPUS_LIFE, SOCIAL_SPARK, CULTURAL_EXCHANGE, WILDCARD, LOW_ENERGY, DAILY)
├── energy_level (LOW, MEDIUM, HIGH)
└── active (boolean)

user_challenges
├── id (PK)
├── user_id (FK)
├── challenge_id (FK)
├── status (ASSIGNED, COMPLETED)
├── mood (LOW, NEUTRAL, HIGH)
├── start_time
└── completion_time
```

## Enums

### CompletionStatus
- `ASSIGNED` - Challenge has been assigned to user
- `COMPLETED` - User has completed the challenge

### ChallengeCategory
- `EXPLORE_CITY`
- `TREND_BASED`
- `CAMPUS_LIFE`
- `SOCIAL_SPARK`
- `CULTURAL_EXCHANGE`
- `WILDCARD`
- `LOW_ENERGY`
- `DAILY`

### EnergyLevel
- `LOW`
- `MEDIUM`
- `HIGH`

### Mood
- `LOW` - User feeling tired or down
- `NEUTRAL` - User feeling balanced
- `HIGH` - User feeling motivated or excited

## Example API Calls

```bash
# Create a user
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com"}'

# Get today's challenge based on mood
curl "http://localhost:8080/challenges/today?userId=1&mood=LOW"

# Get user progress
curl "http://localhost:8080/user-challenges/user/1/progress"

# Mark challenge as completed
curl -X PUT "http://localhost:8080/user-challenges/1/complete"
```

## Project Structure

```
src/main/java/com/divyam/advent/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
├── enums/          # Enumerations
├── exception/      # Custom exceptions
├── model/          # JPA entities
├── repository/     # Data access layer
└── service/        # Business logic
```

## License

This project is for educational purposes.
