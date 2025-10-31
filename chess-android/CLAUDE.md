# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android chess application built with:
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Build System**: Gradle with Kotlin DSL
- **Target SDK**: 36, Min SDK: 35
- **Package**: com.example.chessandroid

## Build Commands

**Build the project:**
```bash
./gradlew build
```

**Run unit tests:**
```bash
./gradlew test
```

**Run instrumented tests:**
```bash
./gradlew connectedAndroidTest
```

**Install debug APK:**
```bash
./gradlew installDebug
```

**Clean build:**
```bash
./gradlew clean
```

**Generate release APK:**
```bash
./gradlew assembleRelease
```

## Architecture

The app follows **Clean Architecture** principles with MVVM pattern:

### Layer Structure

**Presentation Layer** (`ui/`):
- **Screens**: Login, Home, Match History, Profile
- **ViewModels**: Handle UI logic and state (e.g., `LoginViewModel`, `MatchHistoryViewModel`)
- **UI States**: Immutable state classes (e.g., `LoginUiState`, `MatchHistoryUiState`)
- **Navigation**: Type-safe navigation with sealed classes (`ChessNavigation.kt`)
- **Theme**: Material 3 theming in `ui/theme/`

**Data Layer** (`data/`):
- **Repositories**: Data access abstraction
  - `IUserRepository` / `UserRepository` - User authentication and data
    - Email/password login via Auth0
    - Secure credential storage with automatic token refresh
    - Access token retrieval for authenticated API calls
  - `IMatchHistoryRepository` / `MatchHistoryRepository` - Match history data
- **Interfaces**: Repository contracts for testability and flexibility

**Dependency Injection** (`di/`):
- **Hilt**: Dependency injection framework
- **RepositoryModule**: Provides singleton repositories
- **ChessApplication**: `@HiltAndroidApp` entry point
- **MainActivity**: `@AndroidEntryPoint` for DI

### Key Components

- **MainActivity** (`MainActivity.kt`): Single activity with full lifecycle logging
- **ChessApp** (`ui/navigation/ChessApp.kt`): Main navigation host
- **ChessRoute** (`ui/navigation/ChessNavigation.kt`): Type-safe route definitions
  - Login → Home → Match History / Profile

### Navigation Flow

```
Login Screen (start)
    ↓ (on login success)
Home Screen
    ├→ Match History Screen (back stack)
    ├→ Profile Screen (back stack)
    └→ Logout → Login Screen (clear back stack)
```

### File Structure

```
app/src/main/java/com/example/chessandroid/
├── MainActivity.kt              # Single activity entry point
├── ChessApplication.kt          # Hilt application class
├── ui/
│   ├── navigation/
│   │   ├── ChessApp.kt         # NavHost setup
│   │   └── ChessNavigation.kt  # Route definitions
│   ├── screens/
│   │   ├── login/              # Login feature
│   │   ├── home/               # Home screen
│   │   ├── matchhistory/       # Match history with ViewModel
│   │   └── profile/            # Profile screen
│   └── theme/                  # Material 3 theming
├── data/
│   └── repository/             # Repository interfaces & implementations
└── di/
    └── RepositoryModule.kt     # Hilt DI module
```

### State Management

- **ViewModels**: Hold and manage UI state
- **UI State Classes**: Immutable data classes for UI rendering
- **Unidirectional Data Flow**: State flows down, events flow up
- **Hilt Integration**: ViewModels injected via `@HiltViewModel`

## Dependencies Management

Dependencies are managed through Version Catalogs in `gradle/libs.versions.toml`. Key dependencies include:

**UI & Compose**:
- Jetpack Compose BOM (2024.09.00) - UI consistency
- Material 3 - Design components
- Material Icons Extended - Icon library
- Navigation Compose (2.9.5) - Navigation
- Compose UI Tooling - Preview & debugging

**Core Android**:
- AndroidX Core KTX (1.16.0)
- Lifecycle Runtime & ViewModel Compose (2.9.2)
- Activity Compose (1.10.1)

**Dependency Injection**:
- Hilt (2.52) - DI framework
- Hilt Navigation Compose (1.2.0)
- KSP (2.0.21-1.0.28) - Annotation processing

**Authentication**:
- Auth0 (2.10.2) - Authentication provider
  - SecureCredentialsManager for encrypted credential storage
  - AuthenticationAPIClient for email/password login
  - Credentials persist across app restarts

**Networking** (configured, not yet fully implemented):
- Retrofit (2.9.0) - HTTP client
- Kotlinx Serialization (1.7.3) - JSON parsing

**Testing**:
- JUnit 4 (4.13.2)
- AndroidX JUnit (1.2.1)
- Espresso Core (3.6.1)
- Compose UI Test

## Development Notes

### Current Implementation Status

- Single-activity architecture with Compose
- Navigation system fully implemented with 4 screens
- Hilt dependency injection configured
- Repository pattern with interfaces
- ViewModels for state management
- Material 3 theming with edge-to-edge display
- Activity lifecycle logging for debugging
- **Authentication**:
  - Email/password login via Auth0 (fully implemented)
  - Automatic credential check on app launch
  - Secure credential storage with encryption
  - Access token retrieval for API calls
  - Google social login (planned, see `docs/google-login-implementation-plan.md`)

### Code Style

- Kotlin idiomatic code
- Sealed classes for navigation routes
- Interface-based repositories for testability
- ViewModels use Hilt for DI
- Comprehensive lifecycle callbacks with logging
- **Async Operations**: Use `suspendCancellableCoroutine` to bridge callback-based APIs (like Auth0) with Kotlin coroutines
- **Error Handling**: Use `Result<T>` for repository methods to handle success/failure consistently

### When Working on This Codebase

**Navigation**: Use `ChessRoute` sealed class for type-safe navigation. Always specify `popUpTo` and `inclusive` flags when needed.

**New Features**: Follow the existing pattern:
1. Create screen in `ui/screens/{feature}/`
2. Add route to `ChessNavigation.kt`
3. Register in `ChessApp.kt` NavHost
4. Create ViewModel if state management needed
5. Create repository interface + implementation if data access needed
6. Register repository in `RepositoryModule.kt`

**Dependency Injection**:
- Mark Activities/Fragments with `@AndroidEntryPoint`
- Mark ViewModels with `@HiltViewModel` and use `@Inject constructor`
- Add providers to `RepositoryModule` for singletons

**Testing**: Unit tests go in `app/src/test/`, instrumented tests in `app/src/androidTest/`

**Authentication & API Calls**:
- User credentials managed by `UserRepository`
- Access tokens retrieved via `userRepository.getAccessToken()`
- Tokens automatically refreshed when expired
- Use `suspendCancellableCoroutine` when integrating callback-based Auth0 APIs
- Example for authenticated API calls:
```kotlin
userRepository.getAccessToken()
    .onSuccess { token ->
        // Use token in API call headers: "Authorization: Bearer $token"
    }
    .onFailure { error ->
        // Handle authentication error
    }
```

**Login Flow**:
- LoginViewModel checks for existing credentials on init
- If valid credentials exist, user auto-navigates to Home
- Credentials persist across app restarts (encrypted via Android Keystore)
- Logout clears credentials from secure storage