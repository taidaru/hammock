/*
 * Modifications copyright 2026, Taidaru
 */

package app.lawnchair.qsb.providers

import app.lawnchair.animateToAllApps
import app.lawnchair.preferences.PreferenceManager
import app.lawnchair.qsb.ThemingMethod
import com.android.launcher3.Launcher
import com.android.launcher3.R

data object Startpage : QsbSearchProvider(
    id = "startpage",
    name = R.string.search_provider_startpage,
    icon = R.drawable.ic_startpage,
    themingMethod = ThemingMethod.TINT,
    packageName = "",
    // hammock: removed the Lawnchair partner segment
    website = "https://startpage.com/",
    type = QsbSearchProviderType.LOCAL,
    sponsored = false,
) {
    override suspend fun launch(launcher: Launcher, forceWebsite: Boolean) {
        val prefs = PreferenceManager.getInstance(launcher)
        val useWebSuggestions = prefs.searchResultStartPageSuggestion.get()

        if (useWebSuggestions) {
            launcher.animateToAllApps()
            launcher.appsView.searchUiManager.editText?.showKeyboard()
        } else {
            super.launch(launcher, forceWebsite)
        }
    }
}
