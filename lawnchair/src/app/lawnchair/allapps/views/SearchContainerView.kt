/*
 * Modifications copyright 2026, Taidaru
 */

package app.lawnchair.allapps.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import app.lawnchair.search.LawnchairSearchUiDelegate
import app.lawnchair.sections.DrawerSectionsController
import com.android.launcher3.allapps.LauncherAllAppsContainerView
import com.android.launcher3.model.data.ItemInfo
import java.util.function.Predicate

class SearchContainerView(context: Context?, attrs: AttributeSet?) : LauncherAllAppsContainerView(context, attrs) {

    override fun createSearchUiDelegate() = LawnchairSearchUiDelegate(this)

    // hammock: drawer sections; created on first use, the extension points run during inflation
    private var sectionsController: DrawerSectionsController? = null
    private val sections get() = sectionsController ?: DrawerSectionsController(this).also { sectionsController = it }

    override fun getExtraSectionCount() = sections.extraSectionCount

    override fun getSectionFilter(section: Int): Predicate<ItemInfo> = sections.sectionFilter(section)

    override fun onBindSectionTabs(tabs: ViewGroup) = sections.bindTabs(tabs)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sections.onAttached()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sections.onDetached()
    }
}
