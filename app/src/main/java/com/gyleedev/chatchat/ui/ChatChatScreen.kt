package com.gyleedev.chatchat.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ThumbUp
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gyleedev.chatchat.ui.chatlist.ChatListScreen
import com.gyleedev.chatchat.ui.chatroom.ChatRoomScreen
import com.gyleedev.chatchat.ui.finduser.FindUserScreen
import com.gyleedev.chatchat.ui.friendinfo.FriendInfoScreen
import com.gyleedev.chatchat.ui.friendlist.FriendListScreen
import com.gyleedev.chatchat.ui.login.LoginScreen
import com.gyleedev.chatchat.ui.myinfo.MyInfoScreen
import com.gyleedev.chatchat.ui.myinfoedit.MyInfoEditScreen
import com.gyleedev.chatchat.ui.setting.SettingScreen
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

    data object SETTING : BottomNavItem(
        Icons.Outlined.Settings,
        com.gyleedev.chatchat.ui.SETTING
    )

    data object FINDUSER : BottomNavItem(
        Icons.Outlined.Settings,
        com.gyleedev.chatchat.ui.FINDUSER
    )

    data object CHATROOM : BottomNavItem(
        Icons.Outlined.Menu,
        com.gyleedev.chatchat.ui.CHATROOM
    )

    data object MYINFO : BottomNavItem(
        Icons.Outlined.ThumbUp,
        com.gyleedev.chatchat.ui.MYINFO
    )

    data object FRIENDINFO : BottomNavItem(
        Icons.Outlined.ThumbUp,
        com.gyleedev.chatchat.ui.FRIENDINFO
    )

    data object MYINFOEDIT : BottomNavItem(
        Icons.Outlined.ThumbUp,
        com.gyleedev.chatchat.ui.MYINFOEDIT
    )
}

@Composable
fun ChatChatScreen(
    startDestination: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var isBottomBarVisible: Boolean

    navController.currentBackStackEntryAsState().value?.destination?.route.let { route ->
        isBottomBarVisible = when (route) {
            FRIENDLIST -> true
            CHATLIST -> true
            SETTING -> true
            else -> false
        }
    }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                BottomNavigation(
                    navController = navController,
                    modifier = Modifier
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValue ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(paddingValue)
                .consumeWindowInsets(paddingValue)
        ) {
            composable(route = BottomNavItem.FRIENDLIST.screenRoute) {
                FriendListScreen(
                    onMyInfoClick = { navController.navigate("${BottomNavItem.MYINFO.screenRoute}/$it") },
                    onFriendClick = { navController.navigate("${BottomNavItem.FRIENDINFO.screenRoute}/$it") },
                    onFindUserButtonClick = { navController.navigate(BottomNavItem.FINDUSER.screenRoute) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(route = BottomNavItem.CHATLIST.screenRoute) {
                ChatListScreen(
                    onClick = { navController.navigate("${BottomNavItem.CHATROOM.screenRoute}/$it") },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(route = BottomNavItem.LOGIN.screenRoute) {
                LoginScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSignInClicked = { navController.navigate(BottomNavItem.SIGNIN.screenRoute) },
                    onLogInComplete = {
                        navController.navigate(BottomNavItem.FRIENDLIST.screenRoute) {
                            popUpTo(BottomNavItem.LOGIN.screenRoute) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = BottomNavItem.SIGNIN.screenRoute) {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSignInComplete = {
                        navController.navigate(BottomNavItem.CHATLIST.screenRoute) {
                            popUpTo(BottomNavItem.LOGIN.screenRoute) { inclusive = true }
                        }
                    }
                )
            }
            composable(route = BottomNavItem.SETTING.screenRoute) {
                SettingScreen(modifier = Modifier.fillMaxSize())
            }

            composable(route = BottomNavItem.FINDUSER.screenRoute) {
                FindUserScreen(
                    onFindComplete = {
                        navController.navigate(BottomNavItem.FRIENDLIST.screenRoute) {
                            popUpTo(
                                BottomNavItem.FRIENDLIST.screenRoute
                            ) {
                                inclusive = true
                            }
                        }
                    },
                    onBackPressKeyClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(
                route = "${BottomNavItem.CHATROOM.screenRoute}/{friend}",
                arguments = listOf(
                    navArgument("friend") {
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) {
                ChatRoomScreen(onBackPressKeyClick = { navController.navigateUp() })
            }

            composable(
                route = "${BottomNavItem.FRIENDINFO.screenRoute}/{friend}",
                arguments = listOf(
                    navArgument("friend") {
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) {
                FriendInfoScreen(
                    onCloseKeyPressed = { navController.navigateUp() },
                    onChatRoomClick = { navController.navigate("${BottomNavItem.CHATROOM.screenRoute}/$it") }
                )
            }

            composable(
                route = "${BottomNavItem.MYINFO.screenRoute}/{myInfo}",
                arguments = listOf(
                    navArgument("myInfo") {
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) {
                MyInfoScreen(
                    onCloseKeyPressed = { navController.navigateUp() },
                    onChatRoomClick = {},
                    onProfileEditClick = { navController.navigate("${BottomNavItem.MYINFOEDIT.screenRoute}/$it") }
                )
            }

            composable(
                route = "${BottomNavItem.MYINFOEDIT.screenRoute}/{myInfo}",
                arguments = listOf(
                    navArgument("myInfo") {
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) {
                MyInfoEditScreen(
                    onBackKeyPressed = { navController.navigateUp() },
                    onChatRoomClick = {}
                )
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
        BottomNavItem.SETTING
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
