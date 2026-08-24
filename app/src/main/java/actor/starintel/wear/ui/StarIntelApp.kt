package actor.starintel.wear.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import actor.starintel.wear.StarIntelViewModel
import actor.starintel.wear.model.AuthMode
import actor.starintel.wear.model.DTypeStat
import actor.starintel.wear.model.SearchResult

private enum class Screen {
    DASHBOARD,
    SEARCH,
    TARGET,
    SETTINGS,
}

@Composable
fun StarIntelWearApp(vm: StarIntelViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var screen = rememberMutableScreen()

    MaterialTheme {
        when (screen.value) {
            Screen.DASHBOARD -> DashboardScreen(
                vm = vm,
                onSearch = { screen.value = Screen.SEARCH },
                onTarget = { screen.value = Screen.TARGET },
                onSettings = { screen.value = Screen.SETTINGS },
            )
            Screen.SEARCH -> SearchScreen(
                vm = vm,
                onBack = { screen.value = Screen.DASHBOARD },
            )
            Screen.TARGET -> TargetScreen(
                vm = vm,
                onBack = { screen.value = Screen.DASHBOARD },
            )
            Screen.SETTINGS -> SettingsScreen(
                vm = vm,
                onBack = { screen.value = Screen.DASHBOARD },
            )
        }

        if (state.loading) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun rememberMutableScreen() =
    androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(Screen.DASHBOARD)
    }

@Composable
private fun DashboardScreen(
    vm: StarIntelViewModel,
    onSearch: () -> Unit,
    onTarget: () -> Unit,
    onSettings: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()

    ScalingLazyColumn(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            Text("STARINTEL", style = MaterialTheme.typography.titleMedium)
        }

        item {
            MetricButton(
                icon = Icons.Default.Article,
                label = "Documents",
                value = state.stats.totalDocuments,
            )
        }

        item {
            MetricButton(
                icon = Icons.Default.Person,
                label = "People",
                value = state.stats.totalPeople,
            )
        }

        items(state.stats.byDType.take(8), key = { it.dtype }) { item ->
            DTypeButton(item)
        }

        item {
            ActionButton(Icons.Default.Search, "Search", onSearch)
        }

        item {
            ActionButton(Icons.Default.TrackChanges, "Create target", onTarget)
        }

        item {
            ActionButton(Icons.Default.Refresh, "Refresh stats", vm::refresh)
        }

        item {
            ActionButton(Icons.Default.Settings, "Settings", onSettings)
        }

        state.message?.let { message ->
            item {
                Text(message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MetricButton(
    icon: ImageVector,
    label: String,
    value: Long,
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {},
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        secondaryLabel = { Text(formatCount(value)) },
        enabled = false,
    )
}

@Composable
private fun DTypeButton(stat: DTypeStat) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {},
        icon = { Icon(dtypeIcon(stat.dtype), contentDescription = null) },
        label = { Text(stat.dtype) },
        secondaryLabel = { Text(formatCount(stat.count)) },
        enabled = false,
    )
}

@Composable
private fun SearchScreen(
    vm: StarIntelViewModel,
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        resultText(result)?.let(vm::search)
    }

    ScalingLazyColumn(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item { Text("Search StarIntel", style = MaterialTheme.typography.titleMedium) }

        item {
            ActionButton(Icons.Default.Search, "Speak / type query") {
                launcher.launch(wearInputIntent("Search StarIntel", "Search query"))
            }
        }

        if (state.searchQuery.isNotBlank()) {
            item { Text("“${state.searchQuery}”", style = MaterialTheme.typography.bodySmall) }
        }

        items(state.searchResults, key = { it.id + it.title }) { result ->
            SearchResultButton(result) {
                result.url?.let { url ->
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
            }
        }

        item { BackButton(onBack) }
    }
}

@Composable
private fun SearchResultButton(
    result: SearchResult,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        icon = { Icon(dtypeIcon(result.dtype), contentDescription = null) },
        label = { Text(result.title, maxLines = 2) },
        secondaryLabel = {
            Text(
                result.summary?.take(72) ?: result.dtype,
                maxLines = 2,
            )
        },
    )
}

@Composable
private fun TargetScreen(
    vm: StarIntelViewModel,
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()

    val nameLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result -> resultText(result)?.let(vm::setTargetName) }

    val typeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result -> resultText(result)?.let(vm::setTargetDType) }

    ScalingLazyColumn(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item { Text("Create target", style = MaterialTheme.typography.titleMedium) }

        item {
            ActionButton(Icons.Default.Person, "Name: ${state.targetName.ifBlank { "tap to set" }}") {
                nameLauncher.launch(wearInputIntent("Target name", "Name or identifier"))
            }
        }

        item {
            ActionButton(Icons.Default.Tune, "Type: ${state.targetDType}") {
                typeLauncher.launch(wearInputIntent("Target dtype", "person, org, domain…"))
            }
        }

        item {
            ActionButton(Icons.Default.TrackChanges, "Create", vm::createTarget)
        }

        state.message?.let { message ->
            item { Text(message, style = MaterialTheme.typography.bodySmall) }
        }

        item { BackButton(onBack) }
    }
}

@Composable
private fun SettingsScreen(
    vm: StarIntelViewModel,
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()

    val endpointLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result -> resultText(result)?.let(vm::setEndpoint) }

    val tokenLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result -> resultText(result)?.let(vm::setToken) }

    ScalingLazyColumn(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item { Text("Settings", style = MaterialTheme.typography.titleMedium) }

        item {
            ActionButton(Icons.Default.Cloud, state.settings.endpoint) {
                endpointLauncher.launch(
                    wearInputIntent("Server endpoint", "http:// or https://"),
                )
            }
        }

        item {
            val next =
                if (state.settings.authMode == AuthMode.NONE) AuthMode.BEARER else AuthMode.NONE
            ActionButton(
                Icons.Default.Settings,
                "Auth: ${state.settings.authMode.name.lowercase()}",
            ) {
                vm.setAuthMode(next)
            }
        }

        item {
            ActionButton(
                Icons.Default.Settings,
                if (state.settings.hasToken) "Token: ••••••••" else "Set bearer token",
            ) {
                tokenLauncher.launch(wearInputIntent("StarIntel token", "API token"))
            }
        }

        state.message?.let { message ->
            item { Text(message, style = MaterialTheme.typography.bodySmall) }
        }

        item { BackButton(onBack) }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label, maxLines = 2) },
    )
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onBack,
        label = { Text("Back") },
        colors = ButtonDefaults.filledTonalButtonColors(),
    )
}

private fun dtypeIcon(dtype: String): ImageVector {
    return when (dtype.lowercase()) {
        "person", "people" -> Icons.Default.Person
        "org", "organization", "company" -> Icons.Default.Business
        "domain", "website", "host" -> Icons.Default.Language
        "url", "link" -> Icons.Default.Link
        "image", "photo" -> Icons.Default.Image
        "code", "source" -> Icons.Default.Code
        else -> Icons.Default.Article
    }
}

private fun formatCount(value: Long): String {
    return when {
        value >= 1_000_000_000 -> "%.1fB".format(value / 1_000_000_000.0)
        value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)
        value >= 1_000 -> "%.1fK".format(value / 1_000.0)
        else -> value.toString()
    }
}
