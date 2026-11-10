package ceui.lisa.slinky.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ceui.lisa.slinky.mute.MuteDao
import ceui.lisa.slinky.mute.MuteRecord

@Database(entities = [ViewHistory::class, MuteRecord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun muteDao(): MuteDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `mute_record` (`mute_type` INTEGER NOT NULL, " +
                    "`mute_key` TEXT NOT NULL, `display_name` TEXT NOT NULL, " +
                    "`mute_time` INTEGER NOT NULL, PRIMARY KEY(`mute_type`, `mute_key`))"
        )
    }
}
