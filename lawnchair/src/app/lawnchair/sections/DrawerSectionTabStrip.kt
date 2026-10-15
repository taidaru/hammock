package app.lawnchair.sections

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.LinearLayout
import com.android.launcher3.workprofile.PersonalWorkSlidingTabStrip
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The drawer tab strip. Tabs share the width equally while their titles fit; otherwise every tab
 * takes its natural width and the strip scrolls horizontally, keeping the active tab in view.
 */
class DrawerSectionTabStrip(context: Context, attrs: AttributeSet?) : PersonalWorkSlidingTabStrip(context, attrs) {

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val titlePadding = (TITLE_PADDING_DP * resources.displayMetrics.density).roundToInt()
    private var contentWidth = 0
    private var downX = 0f
    private var lastX = 0f
    private var dragging = false
    private var activePage = 0

    private val maxScroll get() = (contentWidth - width).coerceAtLeast(0)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val tabs = (0 until childCount).map { getChildAt(it) }.filter { it.visibility != View.GONE }
        val unspecified = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val naturalWidths = tabs.map { tab ->
            tab.measure(unspecified, unspecified)
            val lp = tab.layoutParams as LinearLayout.LayoutParams
            tab.measuredWidth + 2 * titlePadding + lp.marginStart + lp.marginEnd
        }
        // Equal widths first, to learn the width the strip gets.
        tabs.forEach { setTabWidth(it, width = 0, weight = 1f) }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val available = measuredWidth - paddingStart - paddingEnd
        if (naturalWidths.sum() <= available) {
            contentWidth = measuredWidth
        } else {
            tabs.forEach { tab ->
                tab.measure(unspecified, unspecified)
                setTabWidth(tab, width = tab.measuredWidth + 2 * titlePadding, weight = 0f)
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            contentWidth = naturalWidths.sum() + paddingStart + paddingEnd
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        scrollX = scrollX.coerceIn(0, maxScroll)
    }

    override fun setActiveMarker(activePage: Int) {
        super.setActiveMarker(activePage)
        this.activePage = activePage
        post { scrollToActiveTab() }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (maxScroll == 0) return false
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                lastX = ev.x
                dragging = false
            }

            MotionEvent.ACTION_MOVE -> if (abs(ev.x - downX) > touchSlop) {
                dragging = true
                lastX = ev.x
                parent?.requestDisallowInterceptTouchEvent(true)
            }
        }
        return dragging
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (maxScroll == 0) return super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                scrollX = (scrollX + (lastX - event.x).toInt()).coerceIn(0, maxScroll)
                lastX = event.x
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> dragging = false
        }
        return true
    }

    private fun scrollToActiveTab() {
        if (maxScroll == 0) return
        val tab = getChildAt(activePage) ?: return
        val target = when {
            tab.left - paddingStart < scrollX -> tab.left - paddingStart
            tab.right + paddingEnd > scrollX + width -> tab.right + paddingEnd - width
            else -> return
        }
        ObjectAnimator.ofInt(this, "scrollX", target.coerceIn(0, maxScroll)).start()
    }

    private fun setTabWidth(tab: View, width: Int, weight: Float) {
        // Changing the fields of the existing params keeps this inside one measure pass.
        val lp = tab.layoutParams as LinearLayout.LayoutParams
        lp.width = width
        lp.weight = weight
    }

    private companion object {
        /** Space between a tab title and the tab edge, on each side. */
        const val TITLE_PADDING_DP = 12
    }
}
