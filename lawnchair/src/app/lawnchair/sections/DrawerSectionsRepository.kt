package app.lawnchair.sections

import android.content.Context
import app.lawnchair.data.AppDatabase
import app.lawnchair.preferences2.PreferenceManager2
import app.lawnchair.sections.data.DrawerSectionAppEntity
import app.lawnchair.sections.data.DrawerSectionEntity
import app.lawnchair.util.MainThreadInitializedObject
import com.android.launcher3.util.ComponentKey
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus

/** The most drawer sections there can be, the main section included. */
const val MAX_DRAWER_SECTIONS = 5

const val MAIN_SECTION_ID = 0L

/** [title] is null for the main section until the user renames it; the UI shows the default name then. */
data class DrawerSection(val id: Long, val title: String?)

data class DrawerSections(
    /** The main section first, then the extra sections in user order. */
    val sections: List<DrawerSection>,
    private val appSections: Map<ComponentKey, Long>,
) {
    fun sectionOf(app: ComponentKey): Long = appSections[app] ?: MAIN_SECTION_ID

    companion object {
        val MAIN_ONLY = DrawerSections(listOf(DrawerSection(MAIN_SECTION_ID, null)), emptyMap())
    }
}

class DrawerSectionsRepository(context: Context) {

    private val scope = MainScope() + CoroutineName("DrawerSectionsRepository")
    private val dao = AppDatabase.INSTANCE.get(context).drawerSectionDao()
    private val mainSectionTitle = PreferenceManager2.getInstance(context).drawerMainSectionTitle

    val sections: StateFlow<DrawerSections> = combine(
        dao.observeSections(),
        dao.observeApps(),
        mainSectionTitle.get(),
    ) { extraSections, apps, mainTitle ->
        DrawerSections(
            sections = listOf(DrawerSection(MAIN_SECTION_ID, mainTitle.ifEmpty { null })) +
                extraSections.map { DrawerSection(it.id, it.title) },
            appSections = apps.associate { it.app to it.sectionId },
        )
    }
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, DrawerSections.MAIN_ONLY)

    /** Adds an extra section at the end; returns false when [MAX_DRAWER_SECTIONS] is reached. */
    suspend fun addSection(title: String): Boolean {
        val extraCount = dao.countSections()
        if (extraCount + 1 >= MAX_DRAWER_SECTIONS) return false
        dao.insertSection(DrawerSectionEntity(title = title, rank = extraCount))
        return true
    }

    suspend fun renameSection(id: Long, title: String) {
        if (id == MAIN_SECTION_ID) {
            mainSectionTitle.set(title)
        } else {
            dao.renameSection(id, title)
        }
    }

    /** Deletes an extra section; its apps go back to the main section. */
    suspend fun deleteSection(id: Long) {
        require(id != MAIN_SECTION_ID) { "The main section cannot be deleted" }
        dao.deleteSection(id)
    }

    /** Orders the extra sections; the main section always stays first. */
    suspend fun reorderSections(extraSectionIds: List<Long>) = dao.reorder(extraSectionIds)

    suspend fun moveApp(app: ComponentKey, sectionId: Long) {
        if (sectionId == MAIN_SECTION_ID) {
            dao.unassignApp(app)
        } else {
            dao.assignApp(DrawerSectionAppEntity(app, sectionId))
        }
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::DrawerSectionsRepository)
    }
}
