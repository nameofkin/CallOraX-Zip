package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CallOraTopBar
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DialerScreen
import com.example.ui.screens.InCallScreen
import com.example.ui.screens.NotepadScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecentsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VoiceRecordingsScreen
import com.example.ui.theme.CallOraTheme
import com.example.ui.theme.CalloraViolet
import com.example.viewmodel.CallOraViewModel
import com.example.viewmodel.CallState

enum class AppScreen {
    SPLASH,
    AUTH,
    MAIN,
    IN_CALL,
    PREMIUM,
    ADMIN,
    RECORDINGS,
    NOTEPAD
}

enum class MainTab(val title: String) {
    DIALER("Dialer"),
    RECENTS("Recents"),
    CONTACTS("Contacts"),
    COMMUNITY("Community"),
    PROFILE("Profile")
}

@Composable
fun CallOraApp(
    viewModel: CallOraViewModel = viewModel()
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val callState by viewModel.callState.collectAsState()
    val authMessage by viewModel.authMessage.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var selectedTab by remember { mutableStateOf(MainTab.DIALER) }
    val snackbarHostState = remember { SnackbarHostState() }

    // React to incoming or active call
    LaunchedEffect(callState) {
        if (callState != CallState.IDLE) {
            currentScreen = AppScreen.IN_CALL
        }
    }

    // Automatically navigate when login session changes
    LaunchedEffect(currentUser) {
        if (currentUser != null && currentScreen == AppScreen.AUTH) {
            currentScreen = AppScreen.MAIN
        } else if (currentUser == null && currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.IN_CALL) {
            currentScreen = AppScreen.AUTH
        }
    }

    // Show authMessage snackbar
    LaunchedEffect(authMessage) {
        authMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearAuthMessage()
        }
    }

    CallOraTheme(selectedTheme = currentTheme) {
        when (currentScreen) {
            AppScreen.SPLASH -> {
                SplashScreen(
                    onTimeout = {
                        currentScreen = if (currentUser != null) AppScreen.MAIN else AppScreen.AUTH
                    }
                )
            }

            AppScreen.AUTH -> {
                AuthScreen(
                    viewModel = viewModel,
                    onAuthSuccess = { currentScreen = AppScreen.MAIN }
                )
            }

            AppScreen.IN_CALL -> {
                InCallScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.MAIN }
                )
            }

            AppScreen.PREMIUM -> {
                Scaffold(
                    topBar = {
                        CallOraTopBar(
                            user = currentUser,
                            onPremiumClick = {},
                            onAdminClick = { currentScreen = AppScreen.ADMIN },
                            onThemeClick = { selectedTab = MainTab.PROFILE; currentScreen = AppScreen.MAIN }
                        )
                    },
                    bottomBar = {
                        CallOraBottomNav(
                            selectedTab = selectedTab,
                            onTabSelected = {
                                selectedTab = it
                                currentScreen = AppScreen.MAIN
                            }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        PremiumScreen(viewModel = viewModel)
                    }
                }
            }

            AppScreen.ADMIN -> {
                AdminScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.MAIN }
                )
            }

            AppScreen.RECORDINGS -> {
                Scaffold(
                    topBar = {
                        CallOraTopBar(
                            user = currentUser,
                            onPremiumClick = { currentScreen = AppScreen.PREMIUM },
                            onAdminClick = { currentScreen = AppScreen.ADMIN },
                            onThemeClick = { selectedTab = MainTab.PROFILE; currentScreen = AppScreen.MAIN }
                        )
                    },
                    bottomBar = {
                        CallOraBottomNav(
                            selectedTab = selectedTab,
                            onTabSelected = {
                                selectedTab = it
                                currentScreen = AppScreen.MAIN
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        VoiceRecordingsScreen(viewModel = viewModel)
                    }
                }
            }

            AppScreen.NOTEPAD -> {
                Scaffold(
                    topBar = {
                        CallOraTopBar(
                            user = currentUser,
                            onPremiumClick = { currentScreen = AppScreen.PREMIUM },
                            onAdminClick = { currentScreen = AppScreen.ADMIN },
                            onThemeClick = { selectedTab = MainTab.PROFILE; currentScreen = AppScreen.MAIN }
                        )
                    },
                    bottomBar = {
                        CallOraBottomNav(
                            selectedTab = selectedTab,
                            onTabSelected = {
                                selectedTab = it
                                currentScreen = AppScreen.MAIN
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NotepadScreen(viewModel = viewModel)
                    }
                }
            }

            AppScreen.MAIN -> {
                Scaffold(
                    topBar = {
                        CallOraTopBar(
                            user = currentUser,
                            onPremiumClick = { currentScreen = AppScreen.PREMIUM },
                            onAdminClick = { currentScreen = AppScreen.ADMIN },
                            onThemeClick = { selectedTab = MainTab.PROFILE }
                        )
                    },
                    bottomBar = {
                        CallOraBottomNav(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            MainTab.DIALER -> DialerScreen(
                                viewModel = viewModel,
                                onNavigateToInCall = { currentScreen = AppScreen.IN_CALL }
                            )
                            MainTab.RECENTS -> RecentsScreen(
                                viewModel = viewModel,
                                onNavigateToInCall = { currentScreen = AppScreen.IN_CALL }
                            )
                            MainTab.CONTACTS -> ContactsScreen(
                                viewModel = viewModel,
                                onNavigateToInCall = { currentScreen = AppScreen.IN_CALL }
                            )
                            MainTab.COMMUNITY -> CommunityScreen(viewModel = viewModel)
                            MainTab.PROFILE -> ProfileScreen(
                                viewModel = viewModel,
                                onNavigateToRecordings = { currentScreen = AppScreen.RECORDINGS },
                                onNavigateToNotes = { currentScreen = AppScreen.NOTEPAD },
                                onNavigateToPremium = { currentScreen = AppScreen.PREMIUM },
                                onNavigateToAdmin = { currentScreen = AppScreen.ADMIN }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallOraBottomNav(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            selected = selectedTab == MainTab.DIALER,
            onClick = { onTabSelected(MainTab.DIALER) },
            icon = { Icon(Icons.Default.Dialpad, contentDescription = "Dialer", modifier = Modifier.size(22.dp)) },
            label = { Text("Dialer", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CalloraViolet,
                selectedTextColor = CalloraViolet,
                indicatorColor = CalloraViolet.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_tab_dialer")
        )

        NavigationBarItem(
            selected = selectedTab == MainTab.RECENTS,
            onClick = { onTabSelected(MainTab.RECENTS) },
            icon = { Icon(Icons.Default.History, contentDescription = "Recents", modifier = Modifier.size(22.dp)) },
            label = { Text("Recents", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CalloraViolet,
                selectedTextColor = CalloraViolet,
                indicatorColor = CalloraViolet.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_tab_recents")
        )

        NavigationBarItem(
            selected = selectedTab == MainTab.CONTACTS,
            onClick = { onTabSelected(MainTab.CONTACTS) },
            icon = { Icon(Icons.Default.People, contentDescription = "Contacts", modifier = Modifier.size(22.dp)) },
            label = { Text("Contacts", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CalloraViolet,
                selectedTextColor = CalloraViolet,
                indicatorColor = CalloraViolet.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_tab_contacts")
        )

        NavigationBarItem(
            selected = selectedTab == MainTab.COMMUNITY,
            onClick = { onTabSelected(MainTab.COMMUNITY) },
            icon = { Icon(Icons.Default.Campaign, contentDescription = "Community", modifier = Modifier.size(22.dp)) },
            label = { Text("Community", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CalloraViolet,
                selectedTextColor = CalloraViolet,
                indicatorColor = CalloraViolet.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_tab_community")
        )

        NavigationBarItem(
            selected = selectedTab == MainTab.PROFILE,
            onClick = { onTabSelected(MainTab.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(22.dp)) },
            label = { Text("Profile", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CalloraViolet,
                selectedTextColor = CalloraViolet,
                indicatorColor = CalloraViolet.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_tab_profile")
        )
    }
}
