package com.noop.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noop.R
import com.noop.ingest.HealthConnectImporter.WorkoutFilter

/**
 * Which Health Connect exercise sessions count as workouts: one switch per writing app and per exercise
 * type the last import found. Imported workouts cannot be hidden or retyped afterwards, so this is where
 * an auto-detected walk from Google Fit is kept out while a Suunto ride comes in. Applies on the next import.
 */
@Composable
internal fun HealthConnectWorkoutFilter(
    apps: Set<String>,
    types: Set<String>,
    filter: WorkoutFilter,
    onFilterChange: (WorkoutFilter) -> Unit,
) {
    if (apps.isEmpty() && types.isEmpty()) return
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            uiString(R.string.health_connect_workout_filter_title),
            style = NoopType.subhead,
            color = Palette.textPrimary,
        )
        Text(
            uiString(R.string.health_connect_workout_filter_detail),
            style = NoopType.footnote,
            color = Palette.textTertiary,
        )
        if (apps.isNotEmpty()) {
            Text(uiString(R.string.health_connect_workout_filter_apps), style = NoopType.footnote, color = Palette.textSecondary)
            apps.sortedBy { appLabel(context, it).lowercase() }.forEach { pkg ->
                FilterSwitch(appLabel(context, pkg), pkg !in filter.excludedApps) { on ->
                    onFilterChange(filter.copy(excludedApps = if (on) filter.excludedApps - pkg else filter.excludedApps + pkg))
                }
            }
        }
        if (types.isNotEmpty()) {
            Text(uiString(R.string.health_connect_workout_filter_types), style = NoopType.footnote, color = Palette.textSecondary)
            types.sorted().forEach { type ->
                FilterSwitch(type, type !in filter.excludedTypes) { on ->
                    onFilterChange(filter.copy(excludedTypes = if (on) filter.excludedTypes - type else filter.excludedTypes + type))
                }
            }
        }
    }
}

@Composable
private fun FilterSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(label, style = NoopType.subhead, color = Palette.textPrimary, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Palette.surfaceBase,
                checkedTrackColor = Palette.accent,
                uncheckedThumbColor = Palette.textSecondary,
                uncheckedTrackColor = Palette.surfaceInset,
                uncheckedBorderColor = Palette.hairline,
            ),
        )
    }
}

/** The writing app's name. Package visibility can hide an installed app, so the usual writers are named here. */
private fun appLabel(context: Context, pkg: String): String = KNOWN_APPS[pkg] ?: runCatching {
    val pm = context.packageManager
    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
}.getOrDefault(pkg)

private val KNOWN_APPS = mapOf(
    "com.google.android.apps.fitness" to "Google Fit",
    "com.google.android.apps.healthdata" to "Health Connect",
    "com.google.android.healthconnect.controller" to "Health Connect",
    "com.stt.android.suunto" to "Suunto",
    "com.garmin.android.apps.connectmobile" to "Garmin Connect",
    "com.samsung.android.app.health" to "Samsung Health",
    "com.strava" to "Strava",
    "com.polar.polarflow" to "Polar Flow",
    "nl.appyhapps.healthsync" to "Health Sync",
    "com.fitbit.FitbitMobile" to "Fitbit",
)
