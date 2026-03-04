package me.hchoang.weather.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.hchoang.weather.ui.home.HomeScreen
import me.hchoang.weather.ui.home.HomeViewModel
import me.hchoang.weather.ui.model.POPULAR_AU_CITIES
import me.hchoang.weather.ui.model.PopularCity
import me.hchoang.weather.ui.search.SearchScreen
import me.hchoang.weather.ui.theme.WeatherTheme

object NavRoutes {
    const val HOME = "home"
    const val SEARCH = "search"
}

@Composable
fun WeatherNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // Share HomeViewModel across home and search so location selection persists
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = NavRoutes.HOME) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onSearchClick = { navController.navigate(NavRoutes.SEARCH) },
                viewModel = homeViewModel
            )
        }

        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onLocationSelected = { geohash, name, state ->
                    // Find a matching city in the popular list, or create a transient one
                    val match = POPULAR_AU_CITIES.find { it.geohash == geohash }
                        ?: PopularCity(geohash = geohash, name = name, state = state)
                    homeViewModel.onCitySelected(match)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

/** Full nav graph starting at the Home destination. */
@Preview(showBackground = true, showSystemUi = true, name = "Nav – Home Destination")
@Composable
private fun PreviewNavHome() {
    WeatherTheme {
        WeatherNavGraph()
    }
}

/** Nav graph pre-navigated to the Search destination. */
@Preview(showBackground = true, showSystemUi = true, name = "Nav – Search Destination")
@Composable
private fun PreviewNavSearch() {
    WeatherTheme {
        val navController = rememberNavController()
        WeatherNavGraph(navController = navController)
        // Trigger up-front navigation to search so the preview renders that screen
        androidx.compose.runtime.LaunchedEffect(Unit) {
            navController.navigate(NavRoutes.SEARCH)
        }
    }
}
