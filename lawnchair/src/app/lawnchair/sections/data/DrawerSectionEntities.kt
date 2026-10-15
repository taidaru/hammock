package app.lawnchair.sections.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.launcher3.util.ComponentKey

/** An extra drawer section. The main section is implicit and not stored here. */
@Entity(tableName = "DrawerSections")
data class DrawerSectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val rank: Int,
)

/** Puts an app into an extra section; apps without a row are in the main section. */
@Entity(
    tableName = "DrawerSectionApps",
    foreignKeys = [
        ForeignKey(
            entity = DrawerSectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sectionId")],
)
data class DrawerSectionAppEntity(
    @PrimaryKey val app: ComponentKey,
    val sectionId: Long,
)
