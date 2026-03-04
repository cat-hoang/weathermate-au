# Feature: SQLite Weather Cache

## Summary
On app launch, weather data for **all popular cities is pre-fetched in parallel** from the BOM API and persisted in a local SQLite database (via Room). Switching between city chips reads exclusively from this local cache — no additional network calls are made per city tap.

## Behaviour

- On first launch the app fetches observations and a 7-day forecast for every city in `POPULAR_AU_CITIES` simultaneously, storing all responses in SQLite.
- Tapping a city chip reads instantly from the local database — UI transitions are immediate with no loading delay.
- If a city's cache entry is missing (e.g. initial fetch partially failed), a one-off live fetch is performed as a fallback and the result is cached before display.
- If the entire initial fetch fails, the full-screen error view is shown with a retry button.

## Implementation

### New files

| File | Purpose |
|---|---|
| `data/db/WeatherCacheEntity.kt` | Room `@Entity` classes: `ObservationCacheEntity` and `ForecastCacheEntity`, each keyed by `geohash` with the API response stored as a JSON string and a `cachedAt` timestamp |
| `data/db/WeatherCacheDao.kt` | Suspend DAO methods — `getObservation`, `insertObservation`, `getForecast`, `insertForecast` |
| `data/db/WeatherDatabase.kt` | `RoomDatabase` singleton (`weather_cache.db`); exposes `weatherCacheDao()` |

### Files changed

| File | Change |
|---|---|
| `data/repository/WeatherRepository.kt` | Accepts a `WeatherCacheDao` alongside the API service; adds `fetchAndCache(geohash)` (hits API, writes both tables) and cache-read helpers `getCachedObservation` / `getCachedForecast` |
| `ui/home/HomeViewModel.kt` | Changed to `AndroidViewModel`; `init` calls `prefetchAllCities()` which fans out `fetchAndCache` for every city via `async`/`awaitAll`; `loadWeather()` reads from SQLite only |
| `ui/search/SearchViewModel.kt` | Changed to `AndroidViewModel` to supply `Application` context for the shared `WeatherDatabase` singleton |

### Data flow

```
App launch
  └─ HomeViewModel.init
       └─ prefetchAllCities()
            ├─ async { repository.fetchAndCache("r1r0fsm") }  // Sydney
            ├─ async { repository.fetchAndCache("r1f9652") }  // Melbourne
            └─ ... (all cities in parallel)
                 └─ BOM API → Gson → SQLite (observation_cache, forecast_cache)
                      └─ displayFromCache(firstCity) → UI

City chip tapped
  └─ HomeViewModel.loadWeather(city)
       └─ displayFromCache(city)
            ├─ getCachedObservation(geohash) → SQLite read
            ├─ getCachedForecast(geohash)    → SQLite read
            └─ map DTOs → UI models → StateFlow update
```

### Database schema

**`observation_cache`**

| Column | Type | Notes |
|---|---|---|
| `geohash` | TEXT (PK) | BOM location geohash |
| `dataJson` | TEXT | Gson-serialised `ObservationResponseDto` |
| `cachedAt` | INTEGER | `System.currentTimeMillis()` epoch ms |

**`forecast_cache`**

| Column | Type | Notes |
|---|---|---|
| `geohash` | TEXT (PK) | BOM location geohash |
| `dataJson` | TEXT | Gson-serialised `ForecastResponseDto` |
| `cachedAt` | INTEGER | `System.currentTimeMillis()` epoch ms |

## Dependencies Added

| Library | Purpose |
|---|---|
| `androidx.room:room-runtime` | Room persistence library |
| `androidx.room:room-ktx` | Kotlin coroutine extensions for Room |
| `androidx.room:room-compiler` (KSP) | Annotation processor — generates DAO implementations |
| `com.google.devtools.ksp` (plugin) | Kotlin Symbol Processing for Room code generation |
| `org.jetbrains.kotlin.android` (plugin) | Required by the KSP Gradle plugin |

## Notes
- The `WeatherDatabase` singleton uses the application context to avoid memory leaks.
- Gson (already a transitive Retrofit dependency) is reused for JSON serialisation — no extra library needed.
- Cache entries are replaced on every cold-start prefetch (`OnConflictStrategy.REPLACE`), so data is always refreshed when the app is opened.
