package io.silomaceff.augerlink.data

import java.time.Instant

/**
 * Phase 3 mock data layer. These models are deliberately stub-shaped — the
 * Phase 4 Room persistence layer + Phase 2 LXMF transport layer will replace
 * the in-memory store with real DAOs + RNS.Identity/Destination wiring.
 *
 * Field shapes are intentionally close to what the testbed's working model
 * looks like (LXMF destination hash as a hex string, message origin tagging
 * inbound vs outbound) so the migration is mechanical when the time comes.
 */

/** A peer the user can message. Phase 4 binds to an LXMF destination hash. */
data class Contact(
    val id: String,
    val displayName: String,
    val destinationHash: String,    // 16-byte LXMF dest hash, hex
    val role: String,                // farmer / bot / mesh-relay / etc.
    val isOnline: Boolean,
    val isVerified: Boolean,
)

/** A single LXMF message. */
enum class MessageStatus { Sending, Delivered, Failed }
enum class MessageDirection { Inbound, Outbound }

data class Message(
    val id: String,
    val conversationId: String,
    val direction: MessageDirection,
    val body: String,
    val sentAt: Instant,
    val status: MessageStatus,
)

/** A conversation thread between the user and one contact. */
data class Conversation(
    val id: String,
    val contactId: String,
    val lastMessagePreview: String,
    val lastMessageAt: Instant,
    val unreadCount: Int,
)
