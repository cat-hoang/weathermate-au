# Feature: BOM Weather Home

## Summary
Initial weather app feature targeting the Australian market, sourcing data from the Bureau of Meteorology (BOM) public API.

## Screens

### Home Screen (`ui/home/HomeScreen.kt`)
- City quick-select chip row (10 major AU cities pre-loaded)
- **Current conditions hero card**: temperature, feels-like, weather emoji icon, observing station name, observation time
- **Weather details grid**: humidity, wind direction & speed, gust speed, rain since 9am
- **7-day forecast list**: day name, date, weather icon, short description, rain chance, min/max temperatures
- BOM data attribution footer
- Loading indicator and error state with retry button

### Search Screen (`ui/search/SearchScreen.kt`)
- Full-text search of any Australian location via BOM API
- Debounced input (400 ms) to avoid excessive API calls — minimum 2 characters
- Results list showing suburb name, state, postcode
- Tapping a result navigates back to Home and loads weather for that location

## Architecture

```
data/
  api/
    BomApiService.kt       — Retrofit interface (search, observations, daily forecast)
    RetrofitClient.kt      — OkHttp + Retrofit singleton with logging interceptor
  dto/
    LocationSearchDto.kt   — Location search response DTOs
    ObservationDto.kt      — Current conditions DTOs
    ForecastDto.kt         — 7-day forecast DTOs
  repository/
    WeatherRepository.kt   — Repository wrapping all API calls with Result<T>

ui/
  model/
    WeatherUiModel.kt      — UI models (LocationUi, CurrentWeatherUi, DayForecastUi, PopularCity)
  util/
    Mappers.kt             — DTO → UI model mappers + BOM icon_descriptor → emoji helper
  home/
    HomeViewModel.kt       — Loads observations + forecast in parallel; city selection state
    HomeScreen.kt          — Composable UI
  search/
    SearchViewModel.kt     — Debounced search, result state
    SearchScreen.kt        — Composable UI
  navigation/
    NavGraph.kt            — Home ↔ Search navigation, shared HomeViewModel
  theme/
    Color.kt               — Australian sky-blue palette
    Theme.kt               — WeatherTheme with dynamic color support
```

## API
**Base URL:** `https://api.weather.bom.gov.au/v1/`

| Endpoint | Usage |
|---|---|
| `GET /locations?search=<query>` | Location search |
| `GET /locations/{geohash}/observations` | Current conditions |
| `GET /locations/{geohash}/forecasts/daily` | 7-day forecast |

## Dependencies Added
| Library | Purpose |
|---|---|
| `retrofit2` + `converter-gson` | HTTP client & JSON parsing |
| `okhttp3` logging-interceptor | Request/response logging |
| `lifecycle-viewmodel-compose` | ViewModel in Composable |
| `navigation-compose` | Screen navigation |
| `kotlinx-coroutines-android` | Async API calls |
| `desugar_jdk_libs` | `java.time` support on API 24+ |

## Pre-loaded Cities
Sydney, Melbourne, Brisbane, Adelaide, Perth, Canberra, Newcastle, Gold Coast, Hobart, Darwin.

## Notes
- `minSdk = 24` — core library desugaring enabled for `java.time` APIs
- Internet permission added to `AndroidManifest.xml`
- `usesCleartextTraffic="false"` enforced (BOM API is HTTPS)
