# Google Social Login Implementation Plan

## Overview
Plan to implement Google authentication using Auth0's social login feature via WebAuthProvider.

## Current State
- ✅ Google login button exists in LoginScreen (LoginScreen.kt:73-75)
- ✅ Button calls `viewModel::onGoogleLogin`
- ✅ Auth0 SDK is already configured (version 2.10.2)
- ⚠️ `onGoogleLogin()` is currently a placeholder with a fake 1-second delay

## Prerequisites

### Auth0 Dashboard Configuration
Before implementing the code changes, configure Auth0:

1. **Enable Google Social Connection**
   - Go to Auth0 Dashboard → Authentication → Social
   - Enable Google OAuth 2.0
   - Configure Google Client ID and Secret

2. **Configure Callback URL**
   - Add callback URL in Auth0 application settings:
   ```
   demo://{yourDomain}/android/com.example.chessandroid/callback
   ```
   - Replace `{yourDomain}` with your Auth0 domain from `strings.xml`

3. **Application Settings**
   - Token Endpoint Authentication Method: "None"
   - Application Type: "Native"

### AndroidManifest.xml
Verify intent filter exists for handling Auth0 callback:

```xml
<activity android:name="com.auth0.android.provider.WebAuthActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:host="@string/com_auth0_domain"
            android:pathPrefix="/android/com.example.chessandroid/callback"
            android:scheme="demo" />
    </intent-filter>
</activity>
```

## Implementation Steps

### 1. Update IUserRepository Interface

**File**: `app/src/main/java/com/example/chessandroid/data/repository/IUserRepository.kt`

Add method:
```kotlin
/**
 * Login with Google using Auth0 social connection
 * Opens Chrome Custom Tab for OAuth flow
 * @param activity Activity context for launching WebAuthProvider
 * @return Result with Unit on success or failure with error
 */
suspend fun loginWithGoogle(activity: Activity): Result<Unit>
```

Add import:
```kotlin
import android.app.Activity
```

### 2. Implement in UserRepository

**File**: `app/src/main/java/com/example/chessandroid/data/repository/UserRepository.kt`

Add import:
```kotlin
import android.app.Activity
import com.auth0.android.provider.WebAuthProvider
```

Add method implementation:
```kotlin
override suspend fun loginWithGoogle(activity: Activity): Result<Unit> {
    return suspendCancellableCoroutine { continuation ->
        WebAuthProvider.login(account)
            .withScheme("demo")
            .withConnection("google-oauth2")
            .start(activity, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    credentialsManager.saveCredentials(result)
                    continuation.resume(Result.success(Unit))
                }

                override fun onFailure(error: AuthenticationException) {
                    continuation.resume(Result.failure(error))
                }
            })
    }
}
```

**Key points**:
- Uses `WebAuthProvider.login()` to launch Chrome Custom Tab
- `withScheme("demo")` matches the callback URL scheme
- `withConnection("google-oauth2")` specifies Google as the identity provider
- Credentials automatically saved to `SecureCredentialsManager`
- Returns `Result<Unit>` for consistent error handling

### 3. Update LoginViewModel

**File**: `app/src/main/java/com/example/chessandroid/ui/screens/login/LoginViewModel.kt`

Replace the placeholder `onGoogleLogin()` method:

```kotlin
fun onGoogleLogin(activity: Activity) {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        userRepository.loginWithGoogle(activity)
            .onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = ""
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Google login failed"
                    )
                }
            }
    }
}
```

Add import:
```kotlin
import android.app.Activity
```

### 4. Update LoginScreen to Pass Activity Context

**File**: `app/src/main/java/com/example/chessandroid/ui/screens/login/LoginScreen.kt`

Update the `LoginScreen` composable to get Activity context:

```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    // ... rest of the composable

    // Update Google Sign-in Button
    GoogleSignInButton(
        onClick = {
            activity?.let { viewModel.onGoogleLogin(it) }
        }
    )
}
```

Add import:
```kotlin
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
```

## How It Works

### Authentication Flow

1. User clicks "Sign in with Google" button
2. `onGoogleLogin()` called with Activity context
3. WebAuthProvider launches Chrome Custom Tab
4. User authenticates with Google
5. Google redirects to Auth0
6. Auth0 redirects back to app via callback URL
7. App receives Credentials (access token, refresh token, ID token)
8. Credentials saved to SecureCredentialsManager (encrypted storage)
9. `isLoggedIn = true` → Navigate to Home screen

### Credential Storage

- Credentials stored encrypted using Android Keystore
- Same storage mechanism as email/password login
- Persists across app restarts
- Automatically refreshed when expired
- Accessed via `getAccessToken()` for API calls

### Error Handling

Common errors:
- User cancels authentication
- Network issues
- Auth0 configuration problems
- Google OAuth issues

All errors surfaced in UI via `errorMessage` in `LoginUiState`

## Testing Checklist

- [ ] Auth0 Dashboard configured correctly
- [ ] Google OAuth credentials set up
- [ ] Callback URL registered
- [ ] AndroidManifest.xml has intent filter
- [ ] Click Google login button
- [ ] Chrome Custom Tab opens
- [ ] Can authenticate with Google account
- [ ] Returns to app after authentication
- [ ] Navigates to Home screen
- [ ] Credentials persisted (check by restarting app)
- [ ] Can make API calls with access token
- [ ] Logout clears credentials
- [ ] Error messages display correctly

## Notes

- Chrome Custom Tabs required (installed on all modern Android devices)
- Works with any Auth0 social connection (can add Facebook, GitHub, etc.)
- No Google Sign-In SDK required - Auth0 handles everything
- Same credential management as email/password login
- Token refresh handled automatically by SecureCredentialsManager

## References

- Auth0 Android Quickstart: https://auth0.com/docs/quickstart/native/android
- Auth0 Android SDK: https://github.com/auth0/Auth0.Android
- WebAuthProvider Documentation: https://auth0.github.io/Auth0.Android/