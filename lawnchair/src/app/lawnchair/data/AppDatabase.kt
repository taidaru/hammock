/*
 * Modifications copyright 2026, Taidaru
 */

package app.lawnchair.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import app.lawnchair.data.iconoverride.IconOverride
import app.lawnchair.data.iconoverride.IconOverrideDao
import app.lawnchair.data.wallpaper.Wallpaper
import app.lawnchair.data.wallpaper.service.WallpaperDao
import app.lawnchair.sections.data.DrawerSectionAppEntity
import app.lawnchair.sections.data.DrawerSectionDao
import app.lawnchair.sections.data.DrawerSectionEntity
import app.lawnchair.util.MainThreadInitializedObject
import kotlinx.coroutines.runBlocking

// hammock: drawer section tables replace the drawer folder tables
@Database(
    entities = [IconOverride::class, Wallpaper::class, DrawerSectionEntity::class, DrawerSectionAppEntity::class],
    version = 4,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun iconOverrideDao(): IconOverrideDao
    abstract fun wallpaperDao(): WallpaperDao

    // hammock: drawer sections replace drawer folders
    abstract fun drawerSectionDao(): DrawerSectionDao

    suspend fun checkpoint() {
        iconOverrideDao().checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
        wallpaperDao().checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
        drawerSectionDao().checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
    }

    fun checkpointSync() {
        runBlocking {
            checkpoint()
        }
    }

    companion object {
        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `Wallpapers` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `imagePath` TEXT NOT NULL,
                `rank` INTEGER NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `checksum` TEXT
            )
                    """.trimIndent(),
                )

                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `Folders` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `hide` INTEGER NOT NULL,
                `rank` INTEGER NOT NULL,
                `timestamp` INTEGER NOT NULL
            )
                    """.trimIndent(),
                )

                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `FolderItems` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `folderId` INTEGER NOT NULL,
                `rank` INTEGER NOT NULL,
                `item_info` TEXT,
                `timestamp` INTEGER NOT NULL,
                FOREIGN KEY(`folderId`) REFERENCES `Folders`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
            )
                    """.trimIndent(),
                )

                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_FolderItems_folderId` ON `FolderItems` (`folderId`)",
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS index_FolderItems_folderId ON FolderItems(folderId)")
            }
        }

        // hammock: drawer section tables replace the drawer folder tables; folder data is not migrated
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `FolderItems`")
                database.execSQL("DROP TABLE IF EXISTS `Folders`")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `DrawerSections` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`title` TEXT NOT NULL, `rank` INTEGER NOT NULL)",
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `DrawerSectionApps` (`app` TEXT NOT NULL, `sectionId` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`app`), FOREIGN KEY(`sectionId`) REFERENCES `DrawerSections`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_DrawerSectionApps_sectionId` ON `DrawerSectionApps` (`sectionId`)",
                )
            }
        }

        val INSTANCE = MainThreadInitializedObject { context ->
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "preferences",
            ).addMigrations(MIGRATION_1_3).addMigrations(MIGRATION_2_3)
                // hammock: drawer sections replace drawer folders
                .addMigrations(MIGRATION_3_4)
                .build()
        }
    }
}
