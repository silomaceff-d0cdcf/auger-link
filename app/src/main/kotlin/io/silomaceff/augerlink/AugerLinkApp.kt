package io.silomaceff.augerlink

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.silomaceff.augerlink.ui.PaletteSamplerScreen
import io.silomaceff.augerlink.ui.chats.ChatScreen
import io.silomaceff.augerlink.ui.chats.ConversationListScreen
import io.silomaceff.augerlink.ui.contacts.ContactDetailScreen
import io.silomaceff.augerlink.ui.contacts.ContactListScreen
import io.silomaceff.augerlink.ui.settings.NetworkSettingsScreen
import io.silomaceff.augerlink.ui.settings.SettingsScreen
import io.silomaceff.augerlink.ui.theme.PaletteLocation
import io.silomaceff.augerlink.ui.theme.PaletteMode
import io.silomaceff.augerlink.ui.theme.PaletteVariant

/** Top-level routes for AugerLink. */
private object Routes {
    const val ChatList = "chats"
    const val Chat = "chats/{convId}"
    const val ContactList = "contacts"
    const val ContactDetail = "contacts/{contactId}"
    const val Settings = "settings"
    const val SettingsNetwork = "settings/network"
    const val SettingsTheme = "settings/theme"

    fun chat(convId: String) = "chats/$convId"
    fun contactDetail(contactId: String) = "contacts/$contactId"
}

private data class TabSpec(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val TABS = listOf(
    TabSpec(Routes.ChatList,    "Chats",    Icons.AutoMirrored.Filled.Chat),
    TabSpec(Routes.ContactList, "Contacts", Icons.Filled.Contacts),
    TabSpec(Routes.Settings,    "Settings", Icons.Filled.Settings),
)

/**
 * Root composable for AugerLink Phase 3 UI shell.
 *
 * Three top-level tabs (Chats / Contacts / Settings) with a bottom navigation
 * bar. Detail screens (ChatScreen, ContactDetailScreen) are pushed onto the
 * nav stack of the relevant tab's start route. Tab switching uses
 * [NavController.navigate] with `popUpTo(start)` to keep one back-stack
 * per tab without duplicating destinations on rapid taps.
 */
@Composable
fun AugerLinkApp(
    paletteMode: PaletteMode,
    resolvedVariant: PaletteVariant,
    paletteLocation: PaletteLocation,
    onPaletteModeChange: (PaletteMode) -> Unit,
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { AugerLinkBottomBar(navController) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ChatList,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.ChatList) {
                ConversationListScreen(
                    onOpenConversation = { navController.navigate(Routes.chat(it)) },
                    onComposeNew = { navController.navigate(Routes.ContactList) },
                )
            }
            composable(Routes.Chat) { backStackEntry ->
                val convId = backStackEntry.arguments?.getString("convId").orEmpty()
                ChatScreen(
                    conversationId = convId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.ContactList) {
                ContactListScreen(
                    onOpenContact = { navController.navigate(Routes.contactDetail(it)) },
                    onAddContact = { /* Phase 4: paste destination hash */ },
                )
            }
            composable(Routes.ContactDetail) { backStackEntry ->
                val cId = backStackEntry.arguments?.getString("contactId").orEmpty()
                ContactDetailScreen(
                    contactId = cId,
                    onBack = { navController.popBackStack() },
                    onOpenChat = { contactId ->
                        // For Phase 3 mock: find the conversation that targets this contact and open it
                        val conv = io.silomaceff.augerlink.data.MockStore.conversations
                            .firstOrNull { it.contactId == contactId }
                        if (conv != null) {
                            navController.navigate(Routes.chat(conv.id))
                        }
                    },
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    onOpenNetwork = { navController.navigate(Routes.SettingsNetwork) },
                    onOpenTheme = { navController.navigate(Routes.SettingsTheme) },
                )
            }
            composable(Routes.SettingsNetwork) {
                NetworkSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SettingsTheme) {
                PaletteSamplerScreen(
                    mode = paletteMode,
                    resolvedVariant = resolvedVariant,
                    location = paletteLocation,
                    onModeChange = onPaletteModeChange,
                )
            }
        }
    }
}

@Composable
private fun AugerLinkBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        TABS.forEach { tab ->
            val selected = currentRoute?.startsWith(tab.route.substringBefore("/")) == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
