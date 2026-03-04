package me.hchoang.weather.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.hchoang.weather.ui.model.LocationUi
import me.hchoang.weather.ui.theme.WeatherTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onLocationSelected: (geohash: String, name: String, state: String) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = {
                            Text(
                                "Search Australian locations...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }

                uiState.query.trim().length < 2 && uiState.results.isEmpty() -> {
                    SearchHint(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null && uiState.results.isEmpty() -> {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.results) { location ->
                            LocationResultItem(
                                location = location,
                                onClick = {
                                    onLocationSelected(
                                        location.geohash,
                                        location.name,
                                        location.state
                                    )
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationResultItem(
    location: LocationUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = buildString {
                    if (location.state.isNotBlank()) append(location.state)
                    if (location.postcode.isNotBlank()) {
                        if (isNotEmpty()) append(", ")
                        append(location.postcode)
                    }
                    append(" — Australia")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun SearchHint(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔍", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Search for any Australian location",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "e.g. Sydney, Melbourne, Cairns...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

// ── Preview helpers ───────────────────────────────────────────────────────────

private val sampleLocations = listOf(
    LocationUi(geohash = "r1r0fsm", name = "Sydney", state = "NSW", postcode = "2000"),
    LocationUi(geohash = "r1f9652", name = "Sydney Olympic Park", state = "NSW", postcode = "2127"),
    LocationUi(geohash = "r1r1234", name = "Sydney Airport", state = "NSW", postcode = "2020"),
    LocationUi(geohash = "r3gx2f5", name = "Canberra", state = "ACT", postcode = "2601"),
)

// ── Sub-composable previews ───────────────────────────────────────────────────

@Preview(showBackground = true, name = "Search Hint")
@Composable
private fun PreviewSearchHint() {
    WeatherTheme {
        SearchHint()
    }
}

@Preview(showBackground = true, name = "Location Result Item")
@Composable
private fun PreviewLocationResultItem() {
    WeatherTheme {
        Column {
            LocationResultItem(
                location = sampleLocations[0],
                onClick = {}
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            LocationResultItem(
                location = sampleLocations[1],
                onClick = {}
            )
        }
    }
}

// ── Full-screen state previews ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "Search Screen – Empty / Hint")
@Composable
private fun PreviewSearchScreenHint() {
    WeatherTheme {
        SearchScreenContent(query = "", isLoading = false, error = null, results = emptyList())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "Search Screen – Results")
@Composable
private fun PreviewSearchScreenResults() {
    WeatherTheme {
        SearchScreenContent(
            query = "Sydney",
            isLoading = false,
            error = null,
            results = sampleLocations
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "Search Screen – Loading")
@Composable
private fun PreviewSearchScreenLoading() {
    WeatherTheme {
        SearchScreenContent(query = "Mel", isLoading = true, error = null, results = emptyList())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "Search Screen – Error")
@Composable
private fun PreviewSearchScreenError() {
    WeatherTheme {
        SearchScreenContent(
            query = "Xyz123",
            isLoading = false,
            error = "No locations found for \"Xyz123\".",
            results = emptyList()
        )
    }
}

/** Stateless scaffold used exclusively by previews to render each SearchScreen state. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenContent(
    query: String,
    isLoading: Boolean,
    error: String?,
    results: List<LocationUi>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {},
                        placeholder = {
                            Text(
                                "Search Australian locations...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = {}) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }

                query.trim().length < 2 && results.isEmpty() -> {
                    SearchHint(modifier = Modifier.align(Alignment.Center))
                }

                error != null && results.isEmpty() -> {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }

                else -> {
                    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(results) { location ->
                            LocationResultItem(location = location, onClick = {})
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
