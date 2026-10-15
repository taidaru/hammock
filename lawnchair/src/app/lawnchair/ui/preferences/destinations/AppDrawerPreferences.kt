/*
 * Copyright 2021, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications copyright 2026, Taidaru
 */

package app.lawnchair.ui.preferences.destinations

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences.preferenceManager
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.sections.DrawerSectionsRepository
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.NavigationActionPreference
import app.lawnchair.ui.preferences.components.SuggestionsPreference
import app.lawnchair.ui.preferences.components.colorpreference.ColorPreference
import app.lawnchair.ui.preferences.components.controls.SliderPreference
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import app.lawnchair.ui.preferences.components.layout.DividerColumn
import app.lawnchair.ui.preferences.components.layout.ExpandAndShrink
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import app.lawnchair.ui.preferences.navigation.AppDrawerHiddenApps
import app.lawnchair.ui.preferences.navigation.AppDrawerSections
import com.android.launcher3.R

object AppDrawerRoutes {
    const val HIDDEN_APPS = "hiddenApps"
}

@Composable
fun AppDrawerPreferences(
    modifier: Modifier = Modifier,
) {
    val prefs = preferenceManager()
    val prefs2 = preferenceManager2()
    val context = LocalContext.current
    val resources = context.resources

    PreferenceLayout(
        label = stringResource(id = R.string.app_drawer_label),
        backArrowVisible = !LocalIsExpandedScreen.current,
        modifier = modifier,
    ) {
        // hammock: removed the drawer layout (categories) and folder settings, drawer sections replace them
        PreferenceGroup(heading = stringResource(id = R.string.general_label)) {
            // hammock: entry to the drawer sections screen
            val sectionCount = DrawerSectionsRepository.INSTANCE.get(context).sections.collectAsState().value.sections.size
            NavigationActionPreference(
                label = stringResource(id = R.string.drawer_sections_label),
                subtitle = resources.getQuantityString(R.plurals.drawer_sections_count, sectionCount, sectionCount),
                destination = AppDrawerSections,
            )
            val hiddenApps = prefs2.hiddenApps.getAdapter().state.value
            NavigationActionPreference(
                label = stringResource(id = R.string.hidden_apps_label),
                subtitle = resources.getQuantityString(R.plurals.apps_count, hiddenApps.size, hiddenApps.size),
                destination = AppDrawerHiddenApps,
            )
            SearchBarPreference(SearchRoute.DRAWER_SEARCH, showLabel = false)
            SuggestionsPreference()
        }
        PreferenceGroup(heading = stringResource(R.string.style)) {
            ColorPreference(preference = prefs2.appDrawerBackgroundColor)
            SliderPreference(
                label = stringResource(id = R.string.background_opacity),
                adapter = prefs.drawerOpacity.getAdapter(),
                step = 0.1f,
                valueRange = 0F..1F,
                showAsPercentage = true,
            )
            SwitchPreference(
                label = stringResource(id = R.string.pref_all_apps_search_bar_background),
                adapter = prefs2.appDrawerSearchBarBackground.getAdapter(),
            )
        }
        PreferenceGroup(heading = stringResource(id = R.string.grid)) {
            SliderPreference(
                label = stringResource(id = R.string.app_drawer_columns),
                adapter = prefs2.drawerColumns.getAdapter(),
                step = 1,
                valueRange = 3..10,
            )
            SliderPreference(
                adapter = prefs2.drawerCellHeightFactor.getAdapter(),
                label = stringResource(id = R.string.row_height_label),
                valueRange = 0.3F..1.5F,
                step = 0.1F,
                showAsPercentage = true,
            )
            SliderPreference(
                adapter = prefs2.drawerLeftRightMarginFactor.getAdapter(),
                label = stringResource(id = R.string.app_drawer_indent_label),
                valueRange = 0.0F..1.5F,
                step = 0.05F,
                showAsPercentage = true,
            )
        }
        PreferenceGroup(heading = stringResource(id = R.string.icons)) {
            SliderPreference(
                label = stringResource(id = R.string.icon_sizes),
                adapter = prefs2.drawerIconSizeFactor.getAdapter(),
                step = 0.1f,
                valueRange = 0.5F..1.5F,
                showAsPercentage = true,
            )
            val showDrawerLabels = prefs2.showIconLabelsInDrawer.getAdapter()
            SwitchPreference(
                adapter = showDrawerLabels,
                label = stringResource(id = R.string.show_labels),
            )
            ExpandAndShrink(visible = showDrawerLabels.state.value) {
                DividerColumn {
                    SliderPreference(
                        label = stringResource(id = R.string.label_size),
                        adapter = prefs2.drawerIconLabelSizeFactor.getAdapter(),
                        step = 0.1F,
                        valueRange = 0.5F..1.5F,
                        showAsPercentage = true,
                    )
                    SwitchPreference(
                        adapter = prefs2.twoLineAllApps.getAdapter(),
                        label = stringResource(R.string.twoline_label),
                    )
                }
            }
        }
        PreferenceGroup(heading = stringResource(id = R.string.advanced)) {
            SwitchPreference(
                label = stringResource(id = R.string.pref_all_apps_bulk_icon_loading_title),
                description = stringResource(id = R.string.pref_all_apps_bulk_icon_loading_description),
                adapter = prefs.allAppBulkIconLoading.getAdapter(),
            )
            SwitchPreference(
                label = stringResource(id = R.string.pref_all_apps_remember_position_title),
                description = stringResource(id = R.string.pref_all_apps_remember_position_description),
                adapter = prefs2.rememberPosition.getAdapter(),
            )
            SwitchPreference(
                label = stringResource(id = R.string.pref_all_apps_show_scrollbar_title),
                adapter = prefs2.showScrollbar.getAdapter(),
            )
        }
    }
}

// hammock: removed DrawerLayoutPreference, drawer categories and folders give way to drawer sections
