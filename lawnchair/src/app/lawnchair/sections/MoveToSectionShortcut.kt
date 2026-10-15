package app.lawnchair.sections

import android.os.Process
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import app.lawnchair.LawnchairLauncher
import app.lawnchair.ui.ModalBottomSheetContent
import app.lawnchair.ui.preferences.components.layout.PreferenceDivider
import app.lawnchair.ui.preferences.components.layout.PreferenceTemplate
import app.lawnchair.views.ComposeBottomSheet
import com.android.launcher3.R
import com.android.launcher3.model.data.AppInfo as ModelAppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.popup.SystemShortcut
import com.android.launcher3.util.ComponentKey
import kotlinx.coroutines.launch

/** The "Move to…" icon menu item: picks the drawer section of an app. */
class MoveToSectionShortcut(
    launcher: LawnchairLauncher,
    itemInfo: ItemInfo,
    originalView: View,
    private val app: ComponentKey,
) : SystemShortcut<LawnchairLauncher>(R.drawable.ic_move_to_section, R.string.move_to_section, launcher, itemInfo, originalView) {

    override fun onClick(v: View) {
        val launcher = mTarget
        val repository = DrawerSectionsRepository.INSTANCE.get(launcher)
        val sections = repository.sections.value
        val currentSection = sections.sectionOf(app)
        ComposeBottomSheet.show(launcher) {
            ModalBottomSheetContent(
                title = { Text(stringResource(R.string.move_to_section)) },
                buttons = {
                    OutlinedButton(onClick = { close(true) }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
            ) {
                LazyColumn {
                    itemsIndexed(sections.sections) { index, section ->
                        if (index > 0) PreferenceDivider(startIndent = 40.dp)
                        PreferenceTemplate(
                            title = { Text(section.title ?: stringResource(R.string.all_apps_personal_tab)) },
                            modifier = Modifier.clickable {
                                close(true)
                                launcher.lifecycleScope.launch { repository.moveApp(app, section.id) }
                            },
                            startWidget = { RadioButton(selected = section.id == currentSection, onClick = null) },
                        )
                    }
                }
            }
        }
    }

    companion object {
        /** Shown for drawer apps of the main profile while there is more than one section. */
        @JvmField
        val FACTORY = SystemShortcut.Factory { launcher: LawnchairLauncher, itemInfo: ItemInfo, view: View ->
            val component = itemInfo.targetComponent
            val hasExtraSections = DrawerSectionsRepository.INSTANCE.get(launcher).sections.value.sections.size > 1
            if (itemInfo !is ModelAppInfo || component == null || itemInfo.user != Process.myUserHandle() || !hasExtraSections) {
                null
            } else {
                MoveToSectionShortcut(launcher, itemInfo, view, ComponentKey(component, itemInfo.user))
            }
        }
    }
}
