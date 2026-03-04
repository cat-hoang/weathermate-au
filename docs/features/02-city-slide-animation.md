# Feature: City Slide Animation

## Summary
When a popular city chip is tapped on the Home screen, the weather content slides in/out horizontally rather than abruptly refreshing. The direction of the slide mirrors the position of the newly selected city relative to the current one.

## Behaviour

- Tapping a chip **to the right** of the current selection → new content **slides in from the right**, old content **slides out to the left**.
- Tapping a chip **to the left** of the current selection → new content **slides in from the left**, old content **slides out to the right**.
- While the new city's data is loading, the **previous city's data remains visible** with a thin `LinearProgressIndicator` bar overlaid at the top of the screen.
- The slide animation fires **when the new data arrives**, not immediately on tap, so the transition always carries real content.

## Implementation

### File changed
`app/src/main/java/me/hchoang/weather/ui/home/HomeScreen.kt`

### Key changes

| Change | Detail |
|---|---|
| `slideDirection` state | `mutableIntStateOf(1)` — set to `+1` (right) or `-1` (left) by comparing old vs new chip index in `popularCities` list |
| `AnimatedContent` | Wraps the entire scrollable content area, keyed on `currentWeather to forecast`; triggers the slide when new city data replaces old |
| `transitionSpec` | `slideInHorizontally + fadeIn` ↔ `slideOutHorizontally + fadeOut`; slide uses `tween(350ms)` in, `tween(280ms)` out |
| Loading indicator | Replaced full-screen `CircularProgressIndicator` with a `LinearProgressIndicator` overlay so previous content stays visible during load |
| Error state | Separate overlay; only shown when there is no cached data at all and loading has finished |

### Animation spec
```kotlin
slideInHorizontally(tween(350))  { slideDirection * it } + fadeIn(tween(350))
    .togetherWith(
        slideOutHorizontally(tween(280)) { -slideDirection * it } + fadeOut(tween(200))
    )
```

## Dependencies
No new dependencies — uses `androidx.compose.animation` which is already part of the Compose BOM.
