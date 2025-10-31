# TODO List

## Security: Add Authorization to User Endpoints

### 1. Create ForbiddenException
**File:** `src/main/kotlin/com/example/chess_backend/common/ForbiddenException.kt`
```kotlin
class ForbiddenException(message: String) : RuntimeException(message)
```

### 2. Add Error Code
**File:** `src/main/kotlin/com/example/chess_backend/common/ErrorCode.kt`
- Add `FORBIDDEN` to enum

### 3. Add Exception Handler
**File:** `src/main/kotlin/com/example/chess_backend/common/GlobalExceptionHandler.kt`
- Add handler for `ForbiddenException` returning 403 status

### 4. Secure PUT /api/users/{id}
**File:** `src/main/kotlin/com/example/chess_backend/user/UserController.kt`
- Add `@AuthenticationPrincipal jwt: Jwt?` parameter
- Get current user from JWT
- Check `currentUser.id == id` (user can only update own profile)
- Throw `ForbiddenException` if IDs don't match

### 5. Secure DELETE /api/users/{id}
**File:** `src/main/kotlin/com/example/chess_backend/user/UserController.kt`
- Add `@AuthenticationPrincipal jwt: Jwt?` parameter
- Get current user from JWT
- Check `currentUser.id == id` (user can only delete own account)
- Throw `ForbiddenException` if IDs don't match

### 6. Add JWT to GET Endpoints (Optional)
**File:** `src/main/kotlin/com/example/chess_backend/user/UserController.kt`
- Consider adding JWT to `GET /api/users/{id}` (view opponent profiles)
- Consider adding JWT to `GET /api/users` (find opponents/leaderboards)
- Decide if these should be public or require authentication

### 7. Update Tests
**File:** `src/test/kotlin/com/example/chess_backend/user/UserControllerIntegrationTest.kt`
- Update `should update user successfully` test to mock JWT
- Add test: `should return 403 when user tries to update another user`
- Update `should delete user successfully` test to mock JWT
- Add test: `should return 403 when user tries to delete another user`

### 8. Review Match Endpoints
**File:** `src/main/kotlin/com/example/chess_backend/match/MatchController.kt`
- Check `POST /api/matches` - should verify players exist?
- Check `PUT /api/matches/{id}` - who can update match status?
- Add authorization checks as needed

## Current Security Status

### ✅ Secured Endpoints
- `POST /api/users/register` - Requires JWT in production
- `GET /api/users/me` - Requires JWT in production
- `GET /api/matches/me` - Requires JWT in production

### ❌ Unsecured Endpoints (CRITICAL)
- `PUT /api/users/{id}` - Anyone can update any user
- `DELETE /api/users/{id}` - Anyone can delete any user

### ⚠️ Public Endpoints (Consider securing)
- `GET /api/users/{id}` - Public access
- `GET /api/users` - Public access (enables scraping)
- `POST /api/matches` - Public access
- `PUT /api/matches/{id}` - Public access

## Notes
- In dev mode (`SPRING_PROFILES_ACTIVE=dev`), JWT will be null - handle accordingly
- Production mode requires valid JWT for all `/api/**` endpoints
- Authorization checks ensure users can only modify their own resources
- Consider if match operations need similar authorization