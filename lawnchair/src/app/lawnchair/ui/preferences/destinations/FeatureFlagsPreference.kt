/*
 * Modifications copyright 2026, Taidaru
 */

package app.lawnchair.ui.preferences.destinations

import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.core.content.edit
import app.lawnchair.backup.ui.restoreBackupOpener
import app.lawnchair.ui.preferences.LocalNavController
import app.lawnchair.ui.preferences.components.controls.ClickablePreference
import app.lawnchair.ui.preferences.components.layout.PreferenceLayoutLazyColumn
import app.lawnchair.ui.preferences.components.layout.PreferenceTemplate
import app.lawnchair.ui.preferences.navigation.CreateBackup
import com.android.launcher3.Utilities
import com.android.launcher3.util.OnboardingPrefs.ALL_APPS_VISITED_COUNT
import com.android.launcher3.util.OnboardingPrefs.HOME_BOUNCE_COUNT
import com.android.launcher3.util.OnboardingPrefs.HOME_BOUNCE_SEEN

@Composable
fun FeatureFlagsPreference(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    PreferenceLayoutLazyColumn(
        modifier = modifier,
        label = "Feature Flags",
    ) {
        preferenceCategory("Server flags", "Long press to reset")
        item(key = "server flag info") {
            PreferenceTemplate(
                title = {
                    Text("To change server flags, update the .aconfig files found at the aconfig/ directory, then rebuild Launcher3.")
                },
            )
        }

        // hammock: removed the Gesture Navigation Sandbox, it launched the removed Quickstep tutorial
        preferenceCategory(
            "Onboarding Flows",
            "Reset these if you want to see the education again.",
        )
        items(
            onboardingPrefs,
            key = { it.title },
        ) { item ->
            OnboardingPreference(
                title = item.title,
                onEdit = {
                    Utilities.getPrefs(context)
                        .edit {
                            item.keys.forEach { key -> remove(key) }
                        }
                    Toast.makeText(context, "Reset $item.title", Toast.LENGTH_SHORT).show()
                },
            )
        }

        preferenceCategory(
            "Workspace grid layout",
            // hammock: Hammock app name
            "To share your current workspace, use Hammock's backup and restore system.",
        )
        item(key = "open_backup_system") {
            val navController = LocalNavController.current
            ClickablePreference(
                "Export",
            ) {
                navController.navigate(CreateBackup)
            }
        }
        item(key = "workspace_import") {
            val opener = restoreBackupOpener()
            ClickablePreference(
                "Import",
                onClick = opener,
            )
        }
    }
}

// onboarding preferences
private data class OnboardingPref(
    val title: String,
    val keys: List<String>,
)

private val onboardingPrefs = listOf(
    OnboardingPref(
        "All Apps Bounce",
        listOf(HOME_BOUNCE_SEEN.sharedPrefKey, HOME_BOUNCE_COUNT.sharedPrefKey),
    ),
    // hammock: removed the Hybrid Hotseat and Taskbar education resets, those features were Quickstep
    OnboardingPref("All Apps Visited Count", listOf(ALL_APPS_VISITED_COUNT.sharedPrefKey)),
)

@Composable
fun OnboardingPreference(
    title: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ClickablePreference(
        label = title,
        subtitle = "Tap to reset",
        modifier = modifier,
    ) {
        onEdit()
    }
}

// hammock: removed the Quickstep gesture sandbox actions and IntentPreference

private fun LazyListScope.preferenceCategory(heading: String, description: String? = null) {
    item(key = heading) {
        PreferenceTemplate(
            title = {
                Text(
                    heading,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { this.heading() },
                )
            },
            description = {
                description?.let { Text(description) }
            },
        )
    }
}
