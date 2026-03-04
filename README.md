# WeatherMate AU 🌤️

An Android weather app targeting the Australian market, powered by the **Bureau of Meteorology (BOM)** public API. Built with Kotlin and Jetpack Compose.

## Screenshots

> _Add screenshots here once the app is running on a device/emulator._

## Features

- **Current conditions** — temperature, feels-like, humidity, wind direction & speed, gusts, rain since 9am
- **7-day forecast** — min/max temps, rain chance, UV category, sunrise/sunset times
- **10 major Australian cities** pre-loaded as quick-select chips
- **Location search** — search any Australian suburb or city with debounced input
- **Slide animation** — city chips animate the content in/out horizontally when switching cities
- **Live BOM data** — sourced directly from `api.weather.bom.gov.au`

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository pattern |
| Networking | Retrofit 2 + OkHttp + Gson |
| Navigation | Navigation Compose |
| Async | Kotlin Coroutines + Flow |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

## Project Structure

```
app/src/main/java/me/hchoang/weather/
├── MainActivity.kt
├── data/
│   ├── api/
│   │   ├── BomApiService.kt        # Retrofit endpoints
│   │   └── RetrofitClient.kt       # OkHttp + Retrofit singleton
│   ├── dto/                        # JSON response models
│   │   ├── ForecastDto.kt
│   │   ├── LocationSearchDto.kt
│   │   └── ObservationDto.kt
│   └── repository/
│       └── WeatherRepository.kt    # Data access abstraction
└── ui/
    ├── home/
    │   ├── HomeScreen.kt           # Main weather screen
    │   └── HomeViewModel.kt
    ├── search/
    │   ├── SearchScreen.kt         # Location search screen
    │   └── SearchViewModel.kt
    ├── model/
    │   └── WeatherUiModel.kt       # UI models + popular AU cities
    ├── navigation/
    │   └── NavGraph.kt             # App navigation graph
    ├── theme/
    │   ├── Color.kt                # Australian sky-blue palette
    │   ├── Theme.kt
    │   └── Type.kt
    └── util/
        └── Mappers.kt              # DTO → UI model + weather emoji helper
```

## API

Data is sourced from the **BOM Weather API v1**:

| Endpoint | Usage |
|---|---|
| `GET /v1/locations?search={query}` | Search locations |
| `GET /v1/locations/{geohash}/observations` | Current conditions |
| `GET /v1/locations/{geohash}/forecasts/daily` | 7-day forecast |

Base URL: `https://api.weather.bom.gov.au/`

> BOM data is © Commonwealth of Australia, Bureau of Meteorology.

## Getting Started

### Prerequisites

- Android Studio Meerkat or later
- JDK 11+
- Android SDK 36

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/cat-hoang/weathermate-au.git
   cd weathermate-au
   ```

2. Open the project in Android Studio.

3. Sync Gradle and run on an emulator or device (API 24+).

> No API key is required — the BOM API is publicly accessible.

## Pre-loaded Cities

| City | State |
|---|---|
| Sydney | NSW |
| Melbourne | VIC |
| Brisbane | QLD |
| Adelaide | SA |
| Perth | WA |
| Canberra | ACT |
| Newcastle | NSW |
| Gold Coast | QLD |
| Hobart | TAS |
| Darwin | NT |

## Changelog

See [`docs/features/`](docs/features/) for detailed feature documentation.

| # | Feature |
|---|---|
| 01 | [BOM Weather Home](docs/features/01-bom-weather-home.md) |
| 02 | [City Slide Animation](docs/features/02-city-slide-animation.md) |

## License

```
Copyright 2026 cat-hoang

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
