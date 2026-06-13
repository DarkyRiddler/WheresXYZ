# Where's XYZ? - Android Application

Welcome to the **Where's XYZ?** project. This is a relational group-tracking and event management application built with modern Android standards.

## 🚀 Current Status: UI & Mock Integration
Yes, the login and registration flows in the UI are currently running on **Mock data** (via the Hilt-injected `AuthRepositoryImpl.kt`). This allows you to run, view, and test the frontend screens, biometrics, and bottom navigation tabs immediately in your emulator without needing a running server.

---

## 🛠️ How a Backend Developer Can Create the API for This App

The repository contains a defined network layer that matches the database requirements. The backend developer should build the API server to match this structure.

### 1. The Blueprint: Endpoint Specifications
The backend should implement the following endpoints declared in `ApiService.kt`:

| Module | HTTP Method | Path | Description |
| :--- | :--- | :--- | :--- |
| **Auth** | POST | `/api/auth/register` | Register new user. Hashes password & generates 4-digit code. |
| **Auth** | POST | `/api/auth/login` | Log in user. Returns JWT token, refresh token & expiry. |
| **Auth** | POST | `/api/auth/oauth` | Verify Google/GitHub OAuth token & log in. |
| **Users** | GET | `/api/users/me` | Fetch logged-in user profile (protected by JWT). |
| **Groups** | GET | `/api/groups` | Fetch groups the user belongs to. |
| **Groups** | POST | `/api/groups` | Create a new group. |
| **Groups** | POST | `/api/groups/{groupId}/join` | Join a group using its 4-digit code. |
| **Events** | GET | `/api/groups/{groupId}/event` | Fetch the active event for a specific group. |
| **Events** | POST | `/api/events` | Create a new event. |
| **Pings** | POST | `/api/pings` | Send a push notification/ping to a group member. |

### 2. Request & Response JSON Formats
Payload schemas are defined in `DataTransferObjects.kt`. For example, a successful login response must look like this:

```json
{
  "accessToken": "STRING (signed JWT)",
  "refreshToken": "STRING (UUID)",
  "expiresIn": 3600,
  "user": {
    "id": 12,
    "userCode": 8051,
    "name": "Dawid",
    "lastname": "Smith",
    "email": "user@wheresxyz.com",
    "userPhoto": null
  }
}
```

### 3. Database Architecture (MySQL)
Refer to `backend_integration_guide.md` for the full SQL schema, including:
1. **Users**: Primary credentials & unique 4-digit `user_code`.
2. **Groups**: Code, name, and distance boundaries.
3. **User_Groups**: Many-to-many relationship mapping.
4. **Events**: 1-to-1 relationship with Groups.
5. **Pings**: 1-to-many relationship with Users.

---

## 🔌 How to Connect the App to the Live API Later
Once the backend is ready:

1. **Change Base URL**: Update `BASE_URL` in `NetworkModule.kt`:
   ```kotlin
   private const val BASE_URL = "https://your-backend-api.com/"
   ```
2. **Update Hilt Binding**: Switch from the Mock repository to the real `AuthRepositoryImpl` in your Hilt module.

---

**Note:** Share the `backend_integration_guide.md` file with your developer for copy-pasteable SQL schemas and JSON templates!
