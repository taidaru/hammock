package app.lawnchair.transitions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import com.android.app.animation.Interpolators
import com.android.launcher3.CellLayout
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAnimUtils.SCALE_PROPERTY
import com.android.launcher3.LauncherState
import com.android.launcher3.util.Executors.MAIN_EXECUTOR

/**
 * Scales (and in All Apps also fades) the launcher content while the system plays its default
 * app open animation, like Pixel Launcher does in its remote animation. A third-party launcher
 * gets no callback when the system animation ends, so the content is restored after
 * [RESTORE_DELAY_MS], when the app window covers the launcher, or when the launcher resumes.
 */
class AppLaunchContentAnimator(private val launcher: Launcher) {

    private var restoreContent: (() -> Unit)? = null
    private val restoreRunnable = Runnable { restore() }

    fun animateOut() {
        restore()
        val targetScale = launcher.deviceProfile.workspaceContentScale
        val animator = AnimatorSet()
        val views: List<View>
        val restoreViews: () -> Unit

        if (launcher.isInState(LauncherState.ALL_APPS)) {
            val appsView = launcher.appsView
            val startAlpha = appsView.alpha
            val startScale = SCALE_PROPERTY.get(appsView)
            views = listOf(appsView)
            animator.play(
                ObjectAnimator.ofFloat(appsView, View.ALPHA, startAlpha, 0f).apply {
                    duration = CONTENT_ALPHA_DURATION_MS
                    interpolator = Interpolators.LINEAR
                },
            )
            animator.play(
                ObjectAnimator.ofFloat(appsView, SCALE_PROPERTY, startScale, targetScale).apply {
                    duration = CONTENT_SCALE_DURATION_MS
                    interpolator = Interpolators.AGGRESSIVE_EASE
                },
            )
            restoreViews = {
                appsView.alpha = startAlpha
                SCALE_PROPERTY.set(appsView, startScale)
            }
        } else {
            views = buildList {
                launcher.workspace.forEachVisiblePage { add((it as CellLayout).shortcutsAndWidgets) }
                add(launcher.hotseat)
            }
            views.forEach { view ->
                animator.play(
                    ObjectAnimator.ofFloat(view, SCALE_PROPERTY, 1f, targetScale).apply {
                        duration = CONTENT_SCALE_DURATION_MS
                        interpolator = Interpolators.DECELERATE_1_5
                    },
                )
            }
            restoreViews = { views.forEach { SCALE_PROPERTY.set(it, 1f) } }
        }

        launcher.pauseExpensiveViewUpdates()
        views.forEach { it.setLayerType(View.LAYER_TYPE_HARDWARE, null) }
        restoreContent = {
            animator.cancel()
            restoreViews()
            views.forEach { it.setLayerType(View.LAYER_TYPE_NONE, null) }
            launcher.resumeExpensiveViewUpdates()
        }
        animator.start()
        MAIN_EXECUTOR.handler.postDelayed(restoreRunnable, RESTORE_DELAY_MS)
    }

    fun restore() {
        MAIN_EXECUTOR.handler.removeCallbacks(restoreRunnable)
        restoreContent?.invoke()
        restoreContent = null
    }

    private companion object {
        // Durations from AOSP QuickstepTransitionManager.
        const val CONTENT_SCALE_DURATION_MS = 350L
        const val CONTENT_ALPHA_DURATION_MS = 217L
        const val RESTORE_DELAY_MS = 500L
    }
}
