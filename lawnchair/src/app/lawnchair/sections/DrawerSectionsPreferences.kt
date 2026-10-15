package app.lawnchair.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.controls.PreferenceClickConfirmation
import app.lawnchair.ui.preferences.components.controls.TextPreferenceDialog
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import app.lawnchair.ui.preferences.components.layout.PreferenceTemplate
import app.lawnchair.ui.preferences.components.reorderable.ReorderableDragHandle
import app.lawnchair.ui.preferences.components.reorderable.ReorderablePreferenceGroup
import app.lawnchair.ui.util.bottomSheetHandler
import com.android.launcher3.R
import kotlinx.coroutines.launch

/** Settings screen to add, rename, reorder and delete drawer sections. */
@Composable
fun DrawerSectionsPreferences(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = remember { DrawerSectionsRepository.INSTANCE.get(context) }
    val sections by repository.sections.collectAsState()
    val scope = rememberCoroutineScope()
    val bottomSheetHandler = bottomSheetHandler

    val defaultMainTitle = stringResource(R.string.all_apps_personal_tab)
    val mainTitle = sections.sections.first().title ?: defaultMainTitle
    // Local order, so a drag settles at once instead of waiting for the database round trip.
    var extraSections by remember(sections) { mutableStateOf(sections.sections.drop(1)) }

    val renameLabel = stringResource(R.string.drawer_section_rename)
    val newSectionLabel = stringResource(R.string.drawer_section_new)
    val deleteLabel = stringResource(R.string.drawer_section_delete)

    fun editTitle(label: String, initialTitle: String, onConfirm: (String) -> Unit) {
        bottomSheetHandler.show {
            TextPreferenceDialog(
                title = label,
                initialValue = initialTitle,
                onDismissRequest = { bottomSheetHandler.hide() },
                onConfirm = { onConfirm(it.trim()) },
            )
        }
    }

    PreferenceLayout(
        label = stringResource(R.string.drawer_sections_label),
        backArrowVisible = !LocalIsExpandedScreen.current,
        modifier = modifier,
    ) {
        PreferenceGroup(
            heading = stringResource(R.string.drawer_main_section),
            description = stringResource(R.string.drawer_main_section_description),
        ) {
            PreferenceTemplate(
                title = { Text(mainTitle) },
                modifier = Modifier.clickable {
                    editTitle(renameLabel, mainTitle) { title ->
                        // An empty or default title keeps the localized default name.
                        val stored = if (title == defaultMainTitle) "" else title
                        scope.launch { repository.renameSection(MAIN_SECTION_ID, stored) }
                    }
                },
            )
        }

        if (extraSections.isNotEmpty()) {
            ReorderablePreferenceGroup(
                label = stringResource(R.string.drawer_extra_sections),
                items = extraSections,
                defaultList = extraSections,
                onOrderChange = { order ->
                    extraSections = order
                    scope.launch { repository.reorderSections(order.map { it.id }) }
                },
            ) { section, _, _, onDraggingChange ->
                val title = section.title.orEmpty()
                val deleteText = stringResource(R.string.drawer_section_delete_description, title, mainTitle)
                PreferenceTemplate(
                    title = { Text(title) },
                    modifier = Modifier.clickable {
                        editTitle(renameLabel, title) { newTitle ->
                            if (newTitle.isNotEmpty()) scope.launch { repository.renameSection(section.id, newTitle) }
                        }
                    },
                    contentModifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 16.dp)
                        .padding(start = 16.dp),
                    startWidget = {
                        ReorderableDragHandle(scope = this, onDragStop = { onDraggingChange(false) })
                    },
                    endWidget = {
                        IconButton(
                            onClick = {
                                bottomSheetHandler.show {
                                    PreferenceClickConfirmation(
                                        title = deleteLabel,
                                        text = deleteText,
                                        onDismissRequest = { bottomSheetHandler.hide() },
                                        onConfirm = { scope.launch { repository.deleteSection(section.id) } },
                                    )
                                }
                            },
                            modifier = Modifier.padding(end = 4.dp),
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = deleteLabel)
                        }
                    },
                    applyPaddings = false,
                )
            }
        }

        PreferenceGroup {
            PreferenceTemplate(
                title = { Text(stringResource(R.string.drawer_section_add)) },
                modifier = Modifier.clickable {
                    editTitle(newSectionLabel, "") { title ->
                        if (title.isNotEmpty()) scope.launch { repository.addSection(title) }
                    }
                },
            )
        }
    }
}
