package ceui.lisa.slinky.network

import android.content.Context
import androidx.room.Room
import ceui.lisa.slinky.db.AppDatabase

object RoomDB {

    private lateinit var database: AppDatabase

    fun attach(context: Context) {
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "slinky-db"
        ).build()
    }

    fun db(): AppDatabase {
        return database
    }
}