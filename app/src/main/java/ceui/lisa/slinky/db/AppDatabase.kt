package ceui.lisa.slinky.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ViewHistory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
