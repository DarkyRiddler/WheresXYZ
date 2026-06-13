# "Where's XYZ?" Backend Authentication API Integration Guide

This guide is for the backend developer building the API service for the **Where's XYZ?** Android application. It outlines the database requirements, endpoint specifications, expected request/response payloads, and security guidelines.

---

## 🗄️ 1. Database Schema (MySQL)

As per the project requirements, the `Users` entity must be structured as follows:

```sql
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    user_code INT(4) NOT NULL UNIQUE, -- Unique random 4-digit code (1000 to 9999)
    name VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,   -- Hashed password (e.g., via bcrypt)
    user_photo VARCHAR(255) DEFAULT NULL, -- URL/path to user avatar photo
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### ⚠️ Critical Database Rules:
1. **User Code Gen**: Upon user registration, the backend must generate a unique, random 4-digit integer (`user_code`) between `1000` and `9999`. Verify it doesn't already exist in the database (or handle collisions).
2. **Password Storage**: Passwords must be hashed using a modern hashing algorithm (e.g., **bcrypt** or **Argon2**). Do not store raw passwords.
3. **Unique Keys**: The `email` and `user_code` fields must be marked as `UNIQUE`.

---

## 🌐 2. API Endpoints

The frontend client expects JSON payloads and handles network status codes. All endpoints should return standard JSON headers: `Content-Type: application/json`.

---

### Endpoint A: Register User
* **Method**: `POST`
* **Path**: `/api/auth/register`
* **Request Payload**:
```json
{
  "name": "John",
  "lastname": "Doe",
  "email": "johndoe@example.com",
  "password": "securepassword123"
}
```
* **Success Response** (`201 Created` or `200 OK`):
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
    "userPhoto": null
  }
}
```
* **Error Response** (`400 Bad Request` / `409 Conflict`):
```json
{
  "message": "Email address already registered"
}
```

---

### Endpoint B: Login User
* **Method**: `POST`
* **Path**: `/api/auth/login`
* **Request Payload**:
```json
{
  "email": "johndoe@example.com",
  "password": "securepassword123"
}
```
* **Success Response** (`200 OK`):
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
* **Error Response** (`401 Unauthorized`):
```json
{
  "message": "Invalid email or password credentials"
}
```

---

### Endpoint C: OAuth Provider Login
* **Method**: `POST`
* **Path**: `/api/auth/oauth`
* **Request Payload**:
```json
{
  "provider": "Google", 
  "token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFkZjBl..." -- OAuth ID Token from Google SDK
}
```
* **Backend Responsibilities**:
  1. Verify the incoming token with the provider's API (e.g. Google's `https://oauth2.googleapis.com/tokeninfo?id_token=...`).
  2. If the user doesn't exist in the database, automatically register them (generate a new random `user_code`).
  3. Generate and return a local JWT access token for the session.
* **Success Response** (`200 OK`): Same body structure as **Login User**.

---

### Endpoint D: Fetch Logged In User Profile (Protected)
* **Method**: `GET`
* **Path**: `/api/users/me`
* **Expected Headers**:
  * `Authorization: Bearer <accessToken>`
* **Success Response** (`200 OK`):
```json
{
  "id": 142,
  "userCode": 8051,
  "name": "John",
  "lastname": "Doe",
  "email": "johndoe@example.com",
  "userPhoto": "https://api.wheresxyz.com/uploads/avatars/user_142.jpg"
}
```
* **Error Response** (`401 Unauthorized`):
```json
{
  "message": "Token expired or invalid"
}
```

---

## 🔒 3. Token Security Guidelines (JWT)

1. **Access Tokens**: Use **JSON Web Tokens (JWT)**. Sign them on the server using a cryptographically secure key (e.g., HS256 with a strong secret or RS256 using private/public keys).
2. **Expiration**: Set a reasonable expiration time (e.g., 1 hour / `3600` seconds). The frontend is configured to read the `expiresIn` parameter to calculate validity.
3. **Claims**: Embed the `user_id` inside the JWT payload claims so you can identify which client is calling protected endpoints. Do not embed sensitive information like passwords.
