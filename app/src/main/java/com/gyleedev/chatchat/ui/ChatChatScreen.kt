package com.gyleedev.chatchat.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gyleedev.chatchat.ui.chatlist.ChatListScreen
import com.gyleedev.chatchat.ui.friendlist.FriendListScreen
import com.gyleedev.chatchat.ui.login.LoginScreen
import com.gyleedev.chatchat.ui.signin.SignInScreen

sealed class BottomNavItem(
    val icons: ImageVector,
    val screenRoute: String
) {

    data object LOGIN : BottomNavItem(
        Icons.Outlined.Settings,
        com.gyleedev.chatchat.ui.LOGIN
    )

    data object SIGNIN : BottomNavItem(
        Icons.Outlined.Settings,
        com.gyleedev.chatchat.ui.SIGNIN
    )

    data object FRIENDLIST : BottomNavItem(
        Icons.Outlined.AccountCircle,
        com.gyleedev.chatchat.ui.FRIENDLIST
    )

    data object CHATLIST : BottomNavItem(
        Icons.Outlined.Email,
        com.gyleedev.chatchat.ui.CHATLIST
    )
}

@Composable
fun ChatChatScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = { BottomNavigation(navController = navController, modifier = Modifier) },
        modifier = modifier.fillMaxSize()
    ) { paddingValue ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.FRIENDLIST.screenRoute,
            modifier = Modifier
                .padding(paddingValue)
                .consumeWindowInsets(paddingValue)
        ) {
            composable(route = BottomNavItem.FRIENDLIST.screenRoute) {
                FriendListScreen(modifier = Modifier.fillMaxSize())
            }

            composable(route = BottomNavItem.CHATLIST.screenRoute) {
                ChatListScreen(modifier = Modifier.fillMaxSize())
            }

            composable(route = BottomNavItem.LOGIN.screenRoute) {
                LoginScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSignInClicked = { navController.navigate(BottomNavItem.SIGNIN.screenRoute) }
                )
            }

            composable(route = BottomNavItem.SIGNIN.screenRoute) {
                SignInScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun BottomNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.FRIENDLIST,
        BottomNavItem.CHATLIST,
        BottomNavItem.LOGIN
    )
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icons,
                        contentDescription = null
                    )
                },
                selected = currentRoute == item.screenRoute,
                alwaysShowLabel = false,
                onClick = {
                    navController.navigate(item.screenRoute) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = NavigationBarDefaults.containerColor
                )
            )
        }
    }
}
