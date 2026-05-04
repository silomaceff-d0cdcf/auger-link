package io.silomaceff.augerlink.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Phase 4: persisted LXMF message for one user-added contact conversation.
 *
 * Direction and status are stored as strings (matching enum names) rather
 * than via `@TypeConverter` — Room's converters add a nontrivial amount of
 * boilerplate for two enums of total six values; the manual mapping at the
 * Composable boundary is shorter.
 *
 * `contactDestHash` is the lowercased hex destination hash of the OTHER
 * party in the conversation. Queries use it to filter messages-per-chat.
 */
@Entity(
    tableName = "messages",
    indices = [Index(value = ["contactDestHash"])],
)
data class PersistedMessage(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "contactDestHash")
    val contactDestHash: String,

    /** "Outbound" or "Inbound" — see [MessageDirection]. */
    val direction: String,

    val body: String,

    /** Epoch milliseconds. */
    val sentAt: Long,

    /** "Sending", "Delivered", "Failed", or "Received" — see [MessageStatus]. */
    val status: String,
)
