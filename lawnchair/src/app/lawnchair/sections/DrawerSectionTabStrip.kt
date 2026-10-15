package app.lawnchair.sections

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.android.launcher3.workprofile.PersonalWorkSlidingTabStrip
import kotlin.math.roundToInt

/**
 * The drawer tab strip. It shows a window of at most [MAX_VISIBLE_TABS] equal tabs that follows
 * the pager and keeps the active tab centered where the strip edges allow it. Tabs outside the
 * window wait beyond the screen edges and slide in and out like the app pages.
 */
class DrawerSectionTabStrip(context: Context, attrs: AttributeSet?) : PersonalWorkSlidingTabStrip(context, attrs) {

    private val titlePadding = (TITLE_PADDING_DP * resources.displayMetrics.density).roundToInt()
    private val location = IntArray(2)

    /** Pager position in pages, e.g. 1.5 halfway between the second and the third page. */
    private var pageProgress = 0f

    private val visibleTabs get() = (0 until childCount).map { getChildAt(it) }.filter { it.visibility != View.GONE }

    init {
        clipToPadding = false
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        (child as? TextView)?.apply {
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPaddingRelative(
                paddingStart.coerceAtLeast(titlePadding),
                paddingTop,
                paddingEnd.coerceAtLeast(titlePadding),
                paddingBottom,
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val tabs = visibleTabs
        tabs.forEach { setTabWidth(it, width = 0, weight = 1f) }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (tabs.size <= MAX_VISIBLE_TABS) return

        // Every tab gets the width it would have with exactly MAX_VISIBLE_TABS equal tabs.
        val margins = tabs.sumOf {
            val lp = it.layoutParams as LinearLayout.LayoutParams
            lp.marginStart + lp.marginEnd
        }
        val gap = margins / (tabs.size - 1)
        val tabWidth = (measuredWidth - paddingStart - paddingEnd - gap * (MAX_VISIBLE_TABS - 1)) / MAX_VISIBLE_TABS
        tabs.forEach { setTabWidth(it, tabWidth, weight = 0f) }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        followPages()
    }

    override fun setScroll(currentScroll: Int, totalScroll: Int) {
        super.setScroll(currentScroll, totalScroll)
        val lastPage = visibleTabs.size - 1
        pageProgress = if (totalScroll > 0 && lastPage > 0) {
            currentScroll.toFloat() / totalScroll * lastPage
        } else {
            0f
        }
        followPages()
    }

    // ponytail: LTR only; in RTL the window does not move and extra tabs stay out of view
    private fun followPages() {
        val tabs = visibleTabs
        if (tabs.size <= MAX_VISIBLE_TABS || isLayoutRtl) {
            tabs.forEach { it.translationX = 0f }
            return
        }
        val windowStart = (pageProgress - (MAX_VISIBLE_TABS - 1) / 2).coerceIn(0f, (tabs.size - MAX_VISIBLE_TABS).toFloat())
        val step = (tabs[1].left - tabs[0].left).toFloat()
        // Distances from the window edges to the screen edges: past them a tab is out of view.
        getLocationInWindow(location)
        val insetStart = location[0] + paddingStart
        val insetEnd = rootView.width - location[0] - width + paddingEnd
        tabs.forEachIndexed { i, tab ->
            val slot = i - windowStart
            val beyond = when {
                slot < 0 -> -insetStart * (-slot).coerceAtMost(1f)
                slot > MAX_VISIBLE_TABS - 1 -> insetEnd * (slot - (MAX_VISIBLE_TABS - 1)).coerceAtMost(1f)
                else -> 0f
            }
            tab.translationX = -windowStart * step + beyond
        }
    }

    private fun setTabWidth(tab: View, width: Int, weight: Float) {
        // Changing the fields of the existing params keeps this inside one measure pass.
        val lp = tab.layoutParams as LinearLayout.LayoutParams
        lp.width = width
        lp.weight = weight
    }

    private companion object {
        /** The least space between a tab title and the tab edge, on each side. */
        const val TITLE_PADDING_DP = 16

        const val MAX_VISIBLE_TABS = 3
    }
}
