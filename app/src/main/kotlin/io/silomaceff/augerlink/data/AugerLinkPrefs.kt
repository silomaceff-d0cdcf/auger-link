package io.silomaceff.augerlink.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "augerlink_settings")

/**
 * Phase 4 step 1: persistent user-facing settings.
 *
 * The TCP target list (`tcp_targets_csv`) is the comma-separated set of
 * `host:port` peers that AugerCommsRouter passes to Reticulum's
 * `[[TCPClientInterface]]` blocks at init. Empty string means "no TCP
 * peers" (AutoInterface multicast still works on its own).
 *
 * Phase 4 step 4 adds the user contact list — pasteable destination
 * hashes the user can message. Stored as a single CSV-of-pairs string
 * rather than a separate Proto schema; simple keeps deserialization
 * mechanical.
 */
object AugerLinkPrefs {
    private val KEY_TCP_TARGETS = stringPreferencesKey("tcp_targets_csv")
    private val KEY_CONTACTS = stringPreferencesKey("user_contacts_v1")

    fun tcpTargetsFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[KEY_TCP_TARGETS] ?: "" }

    suspend fun readTcpTargets(context: Context): String =
        tcpTargetsFlow(context).first()

    suspend fun writeTcpTargets(context: Context, value: String) {
        context.dataStore.edit { it[KEY_TCP_TARGETS] = value }
    }

    /**
     * Stored contact list. Wire format: pipe-separated records, each record
     * is "name\tdestHashHex". Pipes / tabs in the name are stripped at
     * write time. Empty string = no contacts.
     *
     * Wire format chosen over JSON to keep this dependency-free; the schema
     * is intentionally trivial (two fields per record, no nesting).
     */
    fun contactsFlow(context: Context): Flow<List<UserContact>> =
        context.dataStore.data.map { prefs ->
            decodeContacts(prefs[KEY_CONTACTS] ?: "")
        }

    suspend fun readContacts(context: Context): List<UserContact> =
        contactsFlow(context).first()

    suspend fun writeContacts(context: Context, contacts: List<UserContact>) {
        context.dataStore.edit { it[KEY_CONTACTS] = encodeContacts(contacts) }
    }

    suspend fun addContact(context: Context, contact: UserContact) {
        val existing = readContacts(context)
        // Replace by destinationHashHex (unique key) or append.
        val out = existing.filterNot { it.destinationHashHex.equals(contact.destinationHashHex, ignoreCase = true) } + contact
        writeContacts(context, out)
    }

    private fun decodeContacts(blob: String): List<UserContact> {
        if (blob.isBlank()) return emptyList()
        return blob.split('|').mapNotNull { record ->
            val parts = record.split('\t', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val name = parts[0]
            val hash = parts[1].trim().lowercase()
            if (hash.isBlank()) return@mapNotNull null
            UserContact(name = name, destinationHashHex = hash)
        }
    }

    private fun encodeContacts(contacts: List<UserContact>): String =
        contacts.joinToString("|") { c ->
            // Sanitize: strip pipes/tabs from name (record separators).
            val cleanName = c.name.replace('|', ' ').replace('\t', ' ')
            "$cleanName\t${c.destinationHashHex.lowercase()}"
        }
}

/** A user-added contact reachable over LXMF. */
data class UserContact(
    val name: String,
    val destinationHashHex: String,
)
