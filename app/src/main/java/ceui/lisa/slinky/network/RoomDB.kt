package ceui.lisa.slinky.network

import android.content.Context
import androidx.room.Room
import ceui.lisa.slinky.db.AppDatabase
import ceui.lisa.slinky.db.MIGRATION_1_2

object RoomDB {

    private lateinit var database: AppDatabase

    fun attach(context: Context) {
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "slinky-db"
        ).addMigrations(MIGRATION_1_2).build()
    }

    fun db(): AppDatabase {
        return database
    }
}