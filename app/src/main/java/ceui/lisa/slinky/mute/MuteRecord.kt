package ceui.lisa.slinky.mute

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

object MuteType {
    const val TAG = 0
    const val USER = 1
    const val ILLUST = 2
    const val NOVEL = 3
}

/**
 * 一条本地屏蔽记录。[key] 对标签是标签名，对用户/作品是 id。
 *
 * 插画和小说的 id 是两条独立的自增序列，同号完全可能，所以主键是 (type, key) 而不是 key。
 */
@Entity(tableName = "mute_record", primaryKeys = ["mute_type", "mute_key"])
data class MuteRecord(
    @ColumnInfo(name = "mute_type") val type: Int,
    @ColumnInfo(name = "mute_key") val key: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "mute_time") val muteTime: Long,
)

@Dao
interface MuteDao {

    @Query("SELECT * FROM mute_record ORDER BY mute_time DESC")
    suspend fun getAll(): List<MuteRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MuteRecord)

    @Query("DELETE FROM mute_record WHERE mute_type = :type AND mute_key = :key")
    suspend fun delete(type: Int, key: String)
}
