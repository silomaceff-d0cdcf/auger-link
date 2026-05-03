package io.silomaceff.augerlink.data

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * In-memory mock data store for Phase 3 UI shell. Replaced in Phase 4 by Room
 * + LXMF wiring. Designed so screens can render real-looking content from
 * day one (no "Coming soon" placeholders) and so Phase 4's drop-in is
 * obvious — same shape, real source.
 *
 * The destination hashes are PLACEHOLDERS (16-byte hex but synthetic — none
 * of these are real LXMF identities). Real testbed contacts will be wired
 * in Phase 5 when service-owned crypto identity lands.
 */
object MockStore {

    private val now: Instant = Instant.parse("2026-05-02T21:30:00Z")

    val contacts: List<Contact> = listOf(
        Contact(
            id = "c1",
            displayName = "silo-demo-bot",
            destinationHash = "deadbeefcafef00d0102030405060708",
            role = "demo bot",
            isOnline = true,
            isVerified = true,
        ),
        Contact(
            id = "c2",
            displayName = "Maria — Field 3",
            destinationHash = "1a2b3c4d5e6f70819212223242526272",
            role = "farmer",
            isOnline = true,
            isVerified = true,
        ),
        Contact(
            id = "c3",
            displayName = "Frank — Tractor",
            destinationHash = "8a7b6c5d4e3f201f1e1d1c1b1a191817",
            role = "farmer (voice)",
            isOnline = false,
            isVerified = true,
        ),
        Contact(
            id = "c4",
            displayName = "FarmHack mesh",
            destinationHash = "f00dcafebabe1234abcdef0123456789",
            role = "mesh relay",
            isOnline = true,
            isVerified = false,
        ),
        Contact(
            id = "c5",
            displayName = "Dorn — Coordinator",
            destinationHash = "c0ffee0102030405060708090a0b0c0d",
            role = "alliance coordinator",
            isOnline = false,
            isVerified = true,
        ),
    )

    fun contactById(id: String): Contact? = contacts.firstOrNull { it.id == id }

    val conversations: List<Conversation> = listOf(
        Conversation(
            id = "conv-1",
            contactId = "c1",
            lastMessagePreview = "moisture: 14% — ready for storage",
            lastMessageAt = now.minus(3, ChronoUnit.MINUTES),
            unreadCount = 1,
        ),
        Conversation(
            id = "conv-2",
            contactId = "c2",
            lastMessagePreview = "you: moving augers from bin 3 to bin 5",
            lastMessageAt = now.minus(42, ChronoUnit.MINUTES),
            unreadCount = 0,
        ),
        Conversation(
            id = "conv-3",
            contactId = "c3",
            lastMessagePreview = "🎙️ voice — 0:08",
            lastMessageAt = now.minus(2, ChronoUnit.HOURS),
            unreadCount = 0,
        ),
        Conversation(
            id = "conv-4",
            contactId = "c4",
            lastMessagePreview = "5 peers connected · throughput 890 msg/s",
            lastMessageAt = now.minus(6, ChronoUnit.HOURS),
            unreadCount = 0,
        ),
        Conversation(
            id = "conv-5",
            contactId = "c5",
            lastMessagePreview = "you: scheduling next alliance call",
            lastMessageAt = now.minus(1, ChronoUnit.DAYS).minus(3, ChronoUnit.HOURS),
            unreadCount = 0,
        ),
    )

    fun conversationById(id: String): Conversation? = conversations.firstOrNull { it.id == id }

    /** Messages keyed by conversationId. */
    val messagesByConversation: Map<String, List<Message>> = mapOf(
        "conv-1" to listOf(
            Message("m1-1", "conv-1", MessageDirection.Outbound,
                "soil moisture report?", now.minus(15, ChronoUnit.MINUTES), MessageStatus.Delivered),
            Message("m1-2", "conv-1", MessageDirection.Inbound,
                "field 3 surface: 22% — irrigation OK", now.minus(15, ChronoUnit.MINUTES).plus(4, ChronoUnit.SECONDS), MessageStatus.Delivered),
            Message("m1-3", "conv-1", MessageDirection.Inbound,
                "field 5 surface: 18% — schedule for tonight", now.minus(15, ChronoUnit.MINUTES).plus(6, ChronoUnit.SECONDS), MessageStatus.Delivered),
            Message("m1-4", "conv-1", MessageDirection.Outbound,
                "grain bin status?", now.minus(4, ChronoUnit.MINUTES), MessageStatus.Delivered),
            Message("m1-5", "conv-1", MessageDirection.Inbound,
                "moisture: 14% — ready for storage", now.minus(3, ChronoUnit.MINUTES), MessageStatus.Delivered),
        ),
        "conv-2" to listOf(
            Message("m2-1", "conv-2", MessageDirection.Inbound,
                "augers loaded", now.minus(45, ChronoUnit.MINUTES), MessageStatus.Delivered),
            Message("m2-2", "conv-2", MessageDirection.Inbound,
                "headed to bin 3 first?", now.minus(44, ChronoUnit.MINUTES), MessageStatus.Delivered),
            Message("m2-3", "conv-2", MessageDirection.Outbound,
                "moving augers from bin 3 to bin 5", now.minus(42, ChronoUnit.MINUTES), MessageStatus.Delivered),
        ),
        "conv-3" to listOf(
            Message("m3-1", "conv-3", MessageDirection.Inbound,
                "🎙️ voice — 0:08", now.minus(2, ChronoUnit.HOURS), MessageStatus.Delivered),
        ),
        "conv-4" to listOf(
            Message("m4-1", "conv-4", MessageDirection.Inbound,
                "5 peers connected · throughput 890 msg/s",
                now.minus(6, ChronoUnit.HOURS), MessageStatus.Delivered),
        ),
        "conv-5" to listOf(
            Message("m5-1", "conv-5", MessageDirection.Outbound,
                "scheduling next alliance call", now.minus(1, ChronoUnit.DAYS), MessageStatus.Delivered),
        ),
    )

    fun messagesFor(conversationId: String): List<Message> =
        messagesByConversation[conversationId] ?: emptyList()
}
