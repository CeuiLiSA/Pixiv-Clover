package ceui.lisa.slinky.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "view_history")
data class ViewHistory(
    @PrimaryKey val objectId: Long,
    @ColumnInfo(name = "visit_time") val visitTime: Long,
    @ColumnInfo(name = "object_json") val objectJson: String,
    @ColumnInfo(name = "object_type") val objectType: Int
)
