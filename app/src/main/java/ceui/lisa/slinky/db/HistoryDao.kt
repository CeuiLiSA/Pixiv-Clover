package ceui.lisa.slinky.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {

    @Query("SELECT * FROM view_history ORDER BY visit_time DESC")
    suspend fun getAll(): List<ViewHistory>

    @Query("SELECT * FROM view_history WHERE object_type = :type ORDER BY visit_time DESC")
    suspend fun getHistoryByType(type: Int): List<ViewHistory>

    @Query("SELECT COUNT(1) FROM view_history WHERE object_type = :type")
    suspend fun getCountByType(type: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViewHistory(viewHistory: ViewHistory)

    @Delete
    fun delete(viewHistory: ViewHistory)
}