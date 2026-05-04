package io.silomaceff.augerlink.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Phase 4: Room database for AugerLink. Currently holds only the message
 * history; later phases will add a Contact table (replacing the
 * AugerLinkPrefs.contacts wire-format encode/decode hack with proper
 * relational storage), and conversation summary rows.
 */
@Database(
    entities = [PersistedMessage::class],
    version = 1,
    exportSchema = false,
)
abstract class AugerLinkDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AugerLinkDatabase? = null

        fun get(context: Context): AugerLinkDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AugerLinkDatabase::class.java,
                    "augerlink.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
