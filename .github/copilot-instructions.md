# GitHub Copilot Instructions — WeatherMate AU

## Project Overview
WeatherMate AU is an Android application that displays BOM (Bureau of Meteorology) weather data for popular Australian cities. It is a single-module Android project written entirely in **Kotlin**.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Navigation Compose (`androidx.navigation`) |
| Networking | Retrofit 2 + OkHttp (Gson converter) |
| Async | Kotlin Coroutines + Flow |
| Build | AGP 9.1.0, Gradle version catalog (`libs.versions.toml`) |
| Min SDK | 24 (core library desugaring enabled for `java.time` APIs) |
| Target SDK | 36 |

---

## Architecture & Code Conventions

### Package Structure
```
me.hchoang.weather
├── data/
│   ├── api/          # Retrofit service interfaces and RetrofitClient singleton
│   ├── dto/          # JSON response data-transfer objects (DTOs)
│   └── repository/   # Repository classes — bridge between API and UI layer
└── ui/
    ├── home/         # Home screen: HomeScreen.kt, HomeViewModel.kt
    ├── search/       # City search feature
    ├── model/        # UI models (mapped from DTOs), constants like POPULAR_AU_CITIES
    ├── navigation/   # NavGraph and route definitions
    ├── theme/        # Compose theme, color, typography
    └── util/         # Extension functions, mappers (e.g. toUi())
```

### State Management
- Each screen has a **single immutable `UiState` data class** (e.g. `HomeUiState`).
- State is held in a `MutableStateFlow` inside the `ViewModel` and exposed as `StateFlow`.
- UI observes state with `collectAsStateWithLifecycle()`.
- Always model loading, success, and error in the same state class using nullable fields and a `Boolean isLoading`.

```kotlin
// Preferred UiState pattern
data class HomeUiState(
    val isLoading: Boolean = false,
    val data: MyDataUi? = null,
    val error: String? = null
)
```

### ViewModel Rules
- ViewModels extend `androidx.lifecycle.ViewModel`.
- Use `viewModelScope.launch` for coroutines. Use `async` + `await` for parallel calls.
- Dependencies (repositories) are currently **constructor-wired manually** — no DI framework. Instantiate dependencies directly in the ViewModel body or accept them as constructor parameters.
- Never expose mutable state directly; always use `asStateFlow()`.

### Repository Rules
- Repositories return `Result<T>` (Kotlin stdlib) to propagate success/failure without throwing.
- Wrap API calls in `runCatching { ... }`.
- Repositories should have no Android framework dependencies.

### Data Mapping
- DTOs live in `data/dto/`. They represent the raw API shape (Gson-annotated).
- UI models live in `ui/model/`. They are clean, display-ready types.
- Mapping from DTO → UI model goes in `ui/util/` as **extension functions** named `toUi()`.

```kotlin
// Example extension in ui/util/
fun ObservationDto.toUi(locationName: String, iconDescriptor: String): CurrentWeatherUi = ...
```

---

## Compose UI Guidelines

- Use **Material 3** components only (`androidx.compose.material3`).
- Screens are top-level `@Composable` functions that accept a `ViewModel` (or a derived state snapshot) as a parameter.
- Keep `@Composable` functions small and focused. Extract reusable UI pieces into separate composables in the same file or a dedicated file.
- Always provide `@Preview` annotations for composables, including a dark-mode preview.
- Use `Modifier` as the last parameter and default it to `Modifier`.

```kotlin
@Composable
fun WeatherCard(
    weather: CurrentWeatherUi,
    modifier: Modifier = Modifier
) { ... }
```

- Navigation is handled in a single `NavHost` inside the `navigation/` package. Add new destinations there.

---

## Dependency Management (Gradle Version Catalog)

All dependencies and versions are declared in `gradle/libs.versions.toml`. 
- **Never hardcode version strings** in `build.gradle.kts` files.
- Add new libraries to `[libraries]` and new plugins to `[plugins]` in the TOML file, then reference them with `libs.<alias>`.

```toml
# gradle/libs.versions.toml
[versions]
coil = "2.7.0"

[libraries]
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
```

```kotlin
// app/build.gradle.kts
implementation(libs.coil.compose)
```

---

## Networking

- The single Retrofit instance is created in `data/api/RetrofitClient.kt`.
- Use `@SerializedName` on DTO fields for Gson mapping.
- All network calls are **suspend functions** in the Retrofit service interface.
- Log network traffic using `HttpLoggingInterceptor` (already wired in `RetrofitClient`).

---

## Testing Guidance

- Unit tests live in `app/src/test/`. Use JUnit 4 (`@Test`).
- Android instrumented tests live in `app/src/androidTest/`. Use Espresso.
- Prefer testing ViewModels with `kotlinx-coroutines-test` (`TestScope`, `runTest`).
- Repositories should be tested with a fake/stub of the Retrofit service.

---

## Key Style Rules

1. **Kotlin idioms first** — prefer `?.let`, `when` expressions, named args, and data classes over Java-style patterns.
2. **No nullable platform types** — annotate or convert all Java interop types explicitly.
3. **Immutability** — prefer `val` over `var`; prefer `listOf` / `mapOf` over mutable collections unless mutation is required.
4. **No magic numbers** — extract constants to companion objects or top-level `const val` declarations.
5. **Coroutines, not threads** — never use `Thread`, `AsyncTask`, or `Handler` for background work; use coroutines.
6. **Single-activity** — the app has one `MainActivity`. All navigation is done via Navigation Compose.
