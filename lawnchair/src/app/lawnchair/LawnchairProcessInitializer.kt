/*
 * Modifications copyright 2026, Taidaru
 */

package app.lawnchair

import android.content.Context
import android.content.pm.PackageManager
import android.os.UserManager
import android.util.Log
import androidx.annotation.Keep
import androidx.arch.core.util.Function
import app.lawnchair.bugreport.LawnchairBugReporter
import app.lawnchair.theme.color.tokens.ColorTokens
import com.android.launcher3.MainProcessInitializer
import com.android.launcher3.Utilities
import com.android.launcher3.icons.ThemedIconDrawable

// hammock: extends MainProcessInitializer, QuickstepProcessInitializer is gone
@Keep
class LawnchairProcessInitializer(context: Context) : MainProcessInitializer() {

    override fun init(context: Context) {
        // hammock: ported from QuickstepProcessInitializer (b/120550382): the launcher does not run in a work profile
        if (Utilities.ATLEAST_R && context.getSystemService(UserManager::class.java)?.isManagedProfile == true) {
            context.packageManager.setApplicationEnabledSetting(
                context.packageName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0,
            )
            Log.w("LawnchairProcessInit", "Disabling ${context.packageName}, unable to run in a managed profile")
            return
        }
        LawnchairBugReporter.INSTANCE.get(context)
        ThemedIconDrawable.COLORS_LOADER = Function {
            if (Utilities.isDarkTheme(it)) {
                intArrayOf(
                    ColorTokens.Accent2_800.resolveColor(it),
                    ColorTokens.Accent1_200.resolveColor(it),
                )
            } else {
                intArrayOf(
                    ColorTokens.Accent1_100.resolveColor(it),
                    ColorTokens.Accent1_700.resolveColor(it),
                )
            }
        }
        super.init(context)
    }
}
