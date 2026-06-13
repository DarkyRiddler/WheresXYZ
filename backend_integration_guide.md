# "Where's XYZ?" Backend Authentication API Integration Guide

This guide is for the backend developer building the API service for the **Where's XYZ?** Android application. It outlines the database requirements, endpoint specifications, expected request/response payloads, and security guidelines.

---

## 🗄️ 1. Database Schema (MySQL)

As per the project requirements, the entities must be structured as follows:

### Users Table
```sql
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    user_code INT NOT NULL UNIQUE, -- Unique random 4-digit code (1000 to 9999)
    name VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,   -- Hashed password (e.g., via bcrypt)
    user_photo VARCHAR(255) DEFAULT NULL, -- URL/path to user avatar photo
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Groups Table
```sql
CREATE TABLE `Groups` (
    group_id INT AUTO_INCREMENT PRIMARY KEY,
    group_code INT NOT NULL UNIQUE, -- 4-digit join code
    group_name VARCHAR(255) NOT NULL,
    accepted_distance FLOAT DEFAULT 100.0,
    group_photo VARCHAR(255) DEFAULT NULL
);
```

### User_Group (Many-to-Many)
```sql
CREATE TABLE user_group (
    user_id INT NOT NULL,
    group_id INT NOT NULL,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES `Groups`(group_id) ON DELETE CASCADE
);
```

### Events (1:1 with Group)
```sql
CREATE TABLE Events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT UNIQUE NOT NULL,
    event_title VARCHAR(255) NOT NULL,
    event_description TEXT,
    event_photo VARCHAR(255),
    event_date_start DATETIME,
    event_date_end DATETIME,
    FOREIGN KEY (group_id) REFERENCES `Groups`(group_id) ON DELETE CASCADE
);
```

### Pings (1:N with User)
```sql
CREATE TABLE Pings (
    ping_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    emoji VARCHAR(10),
    text VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);
```

---

## 🌐 2. API Endpoints

All endpoints should return standard JSON headers: `Content-Type: application/json`.

| Module | Method | Path | Description |
|--------|--------|------|-------------|
| Auth | POST | `/api/auth/register` | Register new user. Hashes password & generates 4-digit code. |
| Auth | POST | `/api/auth/login` | Log in user. Returns JWT token, refresh token & expiry. |
| Auth | POST | `/api/auth/oauth` | Verify Google/GitHub OAuth token & log in. |
| Users | GET | `/api/users/me` | Fetch logged-in user profile details (protected by JWT header). |
| Groups | GET | `/api/groups` | Fetch groups the user belongs to. |
| Groups | POST | `/api/groups` | Create a new group. |
| Groups | POST | `/api/groups/{groupId}/join` | Join a group using its 4-digit code. |
| Events | GET | `/api/groups/{groupId}/event` | Fetch the active event for a specific group. |
| Events | POST | `/api/events` | Create a new event. |
| Pings | POST | `/api/pings` | Send a push notification/ping to a group member. |

---

## 📦 3. JSON Payload Examples

### Login/Register Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "7c8e23f0-466d-4bb1-8e0c-d38df8b1a8d0",
  "expiresIn": 3600,
  "user": {
    "id": 142,
    "userCode": 8051,
    "name": "John",
    "lastname": "Doe",
    "email": "johndoe@example.com",
    "userPhoto": "https://api.wheresxyz.com/uploads/avatars/user_142.jpg"
  }
}
```

---

## 🔒 4. Token Security Guidelines (JWT)

1. **Access Tokens**: Use **JSON Web Tokens (JWT)**.
2. **Expiration**: Set a reasonable expiration time (e.g., 1 hour / `3600` seconds).
3. **Claims**: Embed the `user_id` inside the JWT payload claims.
