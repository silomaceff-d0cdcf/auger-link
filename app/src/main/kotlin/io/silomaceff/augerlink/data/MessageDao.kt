package io.silomaceff.augerlink.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    /**
     * Live feed of messages for one contact, oldest first. The Flow emits
     * a new list every time the underlying table changes — Compose
     * subscribes via `collectAsState` and re-renders the chat.
     */
    @Query("SELECT * FROM messages WHERE contactDestHash = :destHash ORDER BY sentAt ASC")
    fun observeForContact(destHash: String): Flow<List<PersistedMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: PersistedMessage)

    /**
     * Used for in-place status transitions (Sending → Delivered/Failed).
     * `id` is the primary key, so this replaces the row in place.
     */
    @Update
    suspend fun update(message: PersistedMessage)

    @Query("DELETE FROM messages WHERE contactDestHash = :destHash")
    suspend fun clearForContact(destHash: String)
}
