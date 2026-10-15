package app.lawnchair.sections.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.android.launcher3.util.ComponentKey
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawerSectionDao {

    @Query("SELECT * FROM DrawerSections ORDER BY rank")
    fun observeSections(): Flow<List<DrawerSectionEntity>>

    @Query("SELECT * FROM DrawerSectionApps")
    fun observeApps(): Flow<List<DrawerSectionAppEntity>>

    @Query("SELECT COUNT(*) FROM DrawerSections")
    suspend fun countSections(): Int

    @Insert
    suspend fun insertSection(section: DrawerSectionEntity): Long

    @Query("UPDATE DrawerSections SET title = :title WHERE id = :id")
    suspend fun renameSection(id: Long, title: String)

    @Query("DELETE FROM DrawerSections WHERE id = :id")
    suspend fun deleteSection(id: Long)

    @Query("UPDATE DrawerSections SET rank = :rank WHERE id = :id")
    suspend fun setRank(id: Long, rank: Int)

    @Transaction
    suspend fun reorder(ids: List<Long>) {
        ids.forEachIndexed { rank, id -> setRank(id, rank) }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun assignApp(app: DrawerSectionAppEntity)

    @Query("DELETE FROM DrawerSectionApps WHERE app = :app")
    suspend fun unassignApp(app: ComponentKey)

    @RawQuery
    suspend fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
}
