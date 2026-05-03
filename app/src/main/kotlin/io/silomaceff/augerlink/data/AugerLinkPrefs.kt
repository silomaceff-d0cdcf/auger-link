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
 */
object AugerLinkPrefs {
    private val KEY_TCP_TARGETS = stringPreferencesKey("tcp_targets_csv")

    fun tcpTargetsFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[KEY_TCP_TARGETS] ?: "" }

    suspend fun readTcpTargets(context: Context): String =
        tcpTargetsFlow(context).first()

    suspend fun writeTcpTargets(context: Context, value: String) {
        context.dataStore.edit { it[KEY_TCP_TARGETS] = value }
    }
}
