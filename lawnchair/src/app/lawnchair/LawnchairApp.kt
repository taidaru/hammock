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

package app.lawnchair

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.FileProvider
import app.lawnchair.backup.LawnchairBackup
import app.lawnchair.flowerpot.Flowerpot
import app.lawnchair.preferences.PreferenceManager
import app.lawnchair.util.restartLauncher
import app.lawnchair.util.unsafeLazy
import com.android.launcher3.BuildConfig
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.Utilities
import java.io.File

class LawnchairApp : Application() {
    // hammock: removed the Recents provider (QuickSwitch) check, Hammock is never the Recents provider
    private val isAtleastT = Utilities.ATLEAST_T
    internal var accessibilityService: LawnchairAccessibilityService? = null
    val isVibrateOnIconAnimation: Boolean by unsafeLazy { getSystemUiBoolean("config_vibrateOnIconAnimation", false) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Flowerpot.Manager.getInstance(this)
    }

    // hammock: removed the status bar clock toggle, it needs WRITE_SECURE_SETTINGS and the Recents provider role

    fun onLauncherAppStateCreated() {
        registerActivityLifecycleCallbacks(activityHandler)
    }

    fun restart(recreateLauncher: Boolean = true) {
        if (recreateLauncher) {
            activityHandler.finishAll()
        } else {
            restartLauncher(this)
        }
    }

    fun renameRestoredDb(dbName: String) {
        val restoredDbFile = getDatabasePath(LawnchairBackup.RESTORED_DB_FILE_NAME)
        if (!restoredDbFile.exists()) return
        val dbFile = getDatabasePath(dbName)
        restoredDbFile.renameTo(dbFile)
    }

    fun migrateDbName(dbName: String) {
        val dbFile = getDatabasePath(dbName)
        if (dbFile.exists()) return
        val prefs = PreferenceManager.INSTANCE.get(this)
        val dbJournalFile = getJournalFile(dbFile)
        val oldDbSlot = prefs.sp.getString("pref_currentDbSlot", "a")
        val oldDbName = if (oldDbSlot == "a") "launcher.db" else "launcher.db_b"
        val oldDbFile = getDatabasePath(oldDbName)
        val oldDbJournalFile = getJournalFile(oldDbFile)
        if (oldDbFile.exists()) {
            oldDbFile.copyTo(dbFile)
            oldDbJournalFile.copyTo(dbJournalFile)
            oldDbFile.delete()
            oldDbJournalFile.delete()
        }
    }

    fun cleanUpDatabases() {
        val idp = InvariantDeviceProfile.INSTANCE.get(this)
        val dbName = idp.dbFile
        val dbFile = getDatabasePath(dbName)
        dbFile?.parentFile?.listFiles()?.forEach { file ->
            val name = file.name
            if (name.startsWith("launcher") && !name.startsWith(dbName)) {
                file.delete()
            }
        }
    }

    private fun getJournalFile(file: File): File = File(file.parentFile, "${file.name}-journal")

    private fun getSystemUiBoolean(resName: String, fallback: Boolean): Boolean {
        val systemUiPackage = "com.android.systemui"
        val res = packageManager.getResourcesForApplication(systemUiPackage)

        @SuppressLint("DiscouragedApi")
        val resId = res.getIdentifier(resName, "bool", systemUiPackage)
        if (resId == 0) {
            return fallback
        }
        return res.getBoolean(resId)
    }

    private val activityHandler = object : ActivityLifecycleCallbacks {
        private val activities = HashSet<Activity>()
        private var foregroundActivity: Activity? = null

        fun finishAll() {
            HashSet(activities).forEach { it.finish() }
        }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {
            foregroundActivity = activity
        }

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (activity == foregroundActivity) foregroundActivity = null
            activities.remove(activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(activity)
        }
    }

    // hammock: removed checkRecentsComponent, Hammock is never the Recents provider

    fun isAccessibilityServiceBound(): Boolean = accessibilityService != null

    fun performGlobalAction(action: Int): Boolean {
        return accessibilityService?.performGlobalAction(action) ?: run {
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .let(::startActivity)
            false
        }
    }

    companion object {
        private const val TAG = "LawnchairApp"

        @JvmStatic
        lateinit var instance: LawnchairApp
            private set

        // hammock: removed isRecentsEnabled, Hammock is never the Recents provider

        @JvmStatic
        val isAtleastT: Boolean get() = instance.isAtleastT

        // hammock: removed the QuickSwitch incompatibility warning, Hammock is never the Recents provider

        fun getUriForFile(context: Context, file: File): Uri {
            return FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        }
    }
}

val Context.lawnchairApp get() = applicationContext as LawnchairApp
