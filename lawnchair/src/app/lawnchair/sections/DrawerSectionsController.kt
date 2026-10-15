package app.lawnchair.sections

import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import app.lawnchair.font.FontManager
import app.lawnchair.theme.color.tokens.ColorStateListTokens
import app.lawnchair.theme.drawable.DrawableTokens
import com.android.launcher3.R
import com.android.launcher3.allapps.ActivityAllAppsContainerView
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.util.ComponentKey
import java.util.function.Predicate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Feeds drawer sections into the app drawer's section extension points. */
class DrawerSectionsController(private val appsView: ActivityAllAppsContainerView<*>) {

    private val repository = DrawerSectionsRepository.INSTANCE.get(appsView.context)
    private var sections = repository.sections.value
    private var scope: CoroutineScope? = null

    val extraSectionCount get() = sections.sections.size - 1

    fun onAttached() {
        scope = MainScope().also {
            it.launch {
                repository.sections.collect { newSections ->
                    if (newSections != sections) {
                        sections = newSections
                        appsView.onSectionsChanged()
                    }
                }
            }
        }
    }

    fun onDetached() {
        scope?.cancel()
        scope = null
    }

    fun sectionFilter(section: Int): Predicate<ItemInfo> {
        val sectionId = sections.sections[section].id
        return Predicate { info ->
            val component = info.targetComponent ?: return@Predicate sectionId == MAIN_SECTION_ID
            sections.sectionOf(ComponentKey(component, info.user)) == sectionId
        }
    }

    /** Puts a tab per extra section after the personal tab and titles the main section tab. */
    fun bindTabs(tabs: ViewGroup) {
        for (i in tabs.childCount - 1 downTo 0) {
            if (tabs.getChildAt(i).tag == SECTION_TAB_TAG) tabs.removeViewAt(i)
        }
        val personalTab = tabs.findViewById<Button>(R.id.tab_personal) ?: return
        sections.sections.first().title?.let { personalTab.text = it }
        val personalParams = personalTab.layoutParams as LinearLayout.LayoutParams
        sections.sections.drop(1).forEachIndexed { index, section ->
            // The same theme style as the personal tab in all_apps_personal_work_tabs.xml.
            val tab = Button(tabs.context, null, android.R.attr.borderlessButtonStyle).apply {
                tag = SECTION_TAB_TAG
                text = section.title
                isAllCaps = false
                setTextSize(TypedValue.COMPLEX_UNIT_PX, personalTab.textSize)
                background = DrawableTokens.AllAppsTabsBackground.resolve(context)
                setTextColor(ColorStateListTokens.AllAppsTabText.resolve(context))
                FontManager.INSTANCE.get(context).setCustomFont(this, R.id.font_body_medium)
                layoutParams = LinearLayout.LayoutParams(personalParams).apply {
                    marginStart = personalParams.marginEnd
                    marginEnd = personalParams.marginEnd
                }
                gravity = personalTab.gravity
                includeFontPadding = personalTab.includeFontPadding
                minHeight = personalTab.minHeight
                minimumHeight = personalTab.minimumHeight
                setPaddingRelative(
                    personalTab.paddingStart,
                    personalTab.paddingTop,
                    personalTab.paddingEnd,
                    personalTab.paddingBottom,
                )
            }
            tabs.addView(tab, 1 + index)
        }
    }

    private companion object {
        const val SECTION_TAB_TAG = "drawer_section_tab"
    }
}
