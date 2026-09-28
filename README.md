# NewsApp

NewsApp is a modern Android news application built with Kotlin and Jetpack Compose. The project follows MVI and Clean Architecture principles to keep UI state, business logic, and data sources clearly separated.

## Features

- Fetch latest news from NewsData.io
- English and Persian news
- Online language switching
- Persistent language selection
- Local news caching with Room
- Offline news support
- Pagination
- Article details
- Image loading with Coil
- Dependency injection with Koin

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVI
- Clean Architecture
- Coroutines & Flow
- Ktor Client
- Room
- Coil
- Koin
- Kotlinx Serialization
- Navigation Compose
- DataStore / SharedPreferences for settings

## Architecture

The application uses Clean Architecture with MVI-based presentation.

```text
Presentation
    ↓
Use Cases
    ↓
Repository Interfaces
    ↓
Repository Implementations
    ↓
Remote / Local Data Sources
```

MVI is used to manage UI state and user actions:

```text
User Action
    ↓
Action
    ↓
ViewModel
    ↓
Use Case
    ↓
Repository
    ↓
Result
    ↓
UI State
```

## Language Handling

Users can switch between English and Persian while the application is online.

The UI works with a domain-level `AppLanguage` value instead of passing API-specific strings such as `en` and `fa` through the presentation layer.

The repository converts the selected language into the value required by the API when making a Ktor request.

```text
UI
 ↓
AppLanguage
 ↓
MVI Action
 ↓
ViewModel
 ↓
Use Case
 ↓
Repository
 ↓
en / fa
 ↓
Ktor Request
```

Changing the language triggers a new news request, allowing the application to load content in the newly selected language.

## Offline Behavior

Language switching is intentionally disabled while the application is offline.

When there is no internet connection:

```text
Offline
   ↓
Language Switcher Disabled
   ↓
Room Cache
   ↓
Display Cached News
```

The application does not attempt to change the language or request new content while offline.

Pagination is also disabled when there is no network connection.

When the connection becomes available again, the application can request fresh news using the currently selected language.

## Local Storage

Room is used to cache news articles locally.

The cache represents the latest successfully fetched news data. When the remote request is unavailable, the application falls back to the locally stored articles.

Language is not stored as part of the article entity because language switching is an online-only feature.

## Pagination

NewsData.io pagination is handled through the `nextPage` value returned by the API.

Additional pages are requested only when:

- The application is online
- A next page is available
- No other news request is currently loading

## API

NewsApp uses NewsData.io to retrieve news.

Add your API key to `local.properties`:

```properties
API_KEY=YOUR_API_KEY
```

The API key is accessed through `BuildConfig` and is not hard-coded into the application source code.

## Project Structure

```text
app/src/main/java/com/example/newsapp/
├── article/
│   ├── di/
│   └── presentation/
├── news/
│   ├── di/
│   └── presentation/
└── core/
    ├── data/
    │   ├── local/
    │   ├── network/
    │   ├── remote/
    │   └── repository/
    ├── di/
    ├── domain/
    │   ├── usecase/
    │   └── ...
    └── presentation/
```

## Data Flow

### Online

```text
User
 ↓
News Screen
 ↓
ViewModel
 ↓
Use Case
 ↓
NewsRepository
 ↓
Ktor
 ↓
NewsData.io
 ↓
Room Cache
 ↓
UI State
 ↓
Compose UI
```

### Offline

```text
User
 ↓
News Screen
 ↓
ViewModel
 ↓
Use Case
 ↓
NewsRepository
 ↓
Room
 ↓
UI State
 ↓
Compose UI
```
