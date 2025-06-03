package com.gyleedev.feature

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
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gyleedev.feature.blockmanage.BlockManageScreen
import com.gyleedev.feature.chatlist.ChatListScreen
import com.gyleedev.feature.chatroom.ChatRoomScreen
import com.gyleedev.feature.finduser.FindUserScreen
import com.gyleedev.feature.friendedit.FriendEditScreen
import com.gyleedev.feature.friendinfo.FriendInfoScreen
import com.gyleedev.feature.friendlist.FriendListScreen
import com.gyleedev.feature.friendmanage.FriendManageScreen
import com.gyleedev.feature.hidemanage.HideManageScreen
import com.gyleedev.feature.login.LoginScreen
import com.gyleedev.feature.myinfo.MyInfoScreen
import com.gyleedev.feature.myinfoedit.MyInfoEditScreen
import com.gyleedev.feature.setting.SettingScreen
import com.gyleedev.feature.setting.changelanguage.ChangeLanguageScreen
import com.gyleedev.feature.setting.changetheme.ChangeThemeScreen
import com.gyleedev.feature.setting.conversation.ConversationScreen
import com.gyleedev.feature.setting.manageaccount.ManageAccountScreen
import com.gyleedev.feature.setting.myinformation.MyInformationScreen
import com.gyleedev.feature.signin.SignInScreen

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
            composable(route = BottomNavItem.FriendList.screenRoute) {
                FriendListScreen(
                    onMyInfoClick = { navController.navigate("${BottomNavItem.MyInfo.screenRoute}/$it") },
                    onFriendClick = { navController.navigate("${BottomNavItem.FriendInfo.screenRoute}/$it") },
                    onFindUserButtonClick = { navController.navigate(BottomNavItem.FindUser.screenRoute) },
                    onEditFriendClick = { navController.navigate(BottomNavItem.FriendInfoEdit.screenRoute) },
                    onManageFriendClick = { navController.navigate(BottomNavItem.FriendManage.screenRoute) },
                    onSettingClick = { navController.navigate(BottomNavItem.Setting.screenRoute) }
                )
            }

            composable(route = BottomNavItem.ChatList.screenRoute) {
                ChatListScreen(
                    onClick = { navController.navigate("${BottomNavItem.ChatRoom.screenRoute}/$it") },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(route = BottomNavItem.Login.screenRoute) {
                LoginScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSignInClicked = { navController.navigate(BottomNavItem.Signin.screenRoute) },
                    onLogInComplete = {
                        navController.navigate(BottomNavItem.FriendList.screenRoute) {
                            popUpTo(BottomNavItem.Login.screenRoute) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = BottomNavItem.Signin.screenRoute) {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSignInComplete = {
                        navController.navigate(BottomNavItem.ChatList.screenRoute) {
                            popUpTo(BottomNavItem.Login.screenRoute) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable(route = BottomNavItem.Setting.screenRoute) {
                SettingScreen(
                    onLogoutRequest = {
                        navController.navigate(BottomNavItem.Login.screenRoute) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    },
                    onLanguageRequest = {
                        navController.navigate(BottomNavItem.ChangeLanguage.screenRoute)
                    },
                    onThemeRequest = {
                        navController.navigate(BottomNavItem.ChangeTheme.screenRoute)
                    },
                    onConversationRequest = {
                        navController.navigate(BottomNavItem.Conversation.screenRoute)
                    },
                    onManageAccountRequest = {
                        navController.navigate(BottomNavItem.ManageAccount.screenRoute)
                    },
                    onMyInformationRequest = {
                        navController.navigate(BottomNavItem.MyInformationSetting.screenRoute)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            composable(route = BottomNavItem.FindUser.screenRoute) {
                FindUserScreen(
                    onProcessComplete = {
                        navController.navigate(BottomNavItem.FriendList.screenRoute) {
                            popUpTo(
                                BottomNavItem.FriendList.screenRoute
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
                route = "${BottomNavItem.ChatRoom.screenRoute}/{friend}",
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
                route = "${BottomNavItem.FriendInfo.screenRoute}/{friend}",
                arguments = listOf(
                    navArgument("friend") {
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) {
                FriendInfoScreen(
                    onCloseKeyPressed = { navController.navigateUp() },
                    onChatRoomClick = { navController.navigate("${BottomNavItem.ChatRoom.screenRoute}/$it") }
                )
            }

            composable(
                route = "${BottomNavItem.MyInfo.screenRoute}/{myInfo}",
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
                    onProfileEditClick = { navController.navigate("${BottomNavItem.MyInfoEdit.screenRoute}/$it") }
                )
            }

            composable(
                route = "${BottomNavItem.MyInfoEdit.screenRoute}/{myInfo}",
                arguments = listOf(
                    navArgument("myInfo") {
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) {
                MyInfoEditScreen(
                    onBackKeyPressed = { navController.navigateUp() }
                )
            }

            composable(
                route = BottomNavItem.FriendInfoEdit.screenRoute
            ) {
                FriendEditScreen(
                    onBackPressKeyClick = { navController.navigateUp() }
                )
            }

            composable(
                route = BottomNavItem.FriendManage.screenRoute
            ) {
                FriendManageScreen(
                    onBackPressKeyClick = { navController.navigateUp() },
                    onBlockedClick = { navController.navigate(BottomNavItem.BlockedUserManage.screenRoute) },
                    onHideClick = { navController.navigate(BottomNavItem.HidenUserManage.screenRoute) }
                )
            }

            composable(
                route = BottomNavItem.HidenUserManage.screenRoute
            ) {
                HideManageScreen(
                    onBackPressKeyClick = { navController.navigateUp() }
                )
            }

            composable(
                route = BottomNavItem.BlockedUserManage.screenRoute
            ) {
                BlockManageScreen(
                    onBackPressKeyClick = { navController.navigateUp() }
                )
            }

            composable(
                route = BottomNavItem.ChangeLanguage.screenRoute
            ) {
                ChangeLanguageScreen(
                    onBackPress = { navController.navigateUp() }
                )
            }

            composable(
                route = BottomNavItem.ChangeTheme.screenRoute
            ) {
                ChangeThemeScreen(
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable(
                route = BottomNavItem.Conversation.screenRoute
            ) {
                ConversationScreen(
                    onBackButtonClicked = { navController.navigateUp() }
                )
            }

            composable(
                route = BottomNavItem.ManageAccount.screenRoute
            ) {
                ManageAccountScreen(
                    onBackButtonClicked = { navController.navigateUp() }
                )
            }

            composable(
                route = BottomNavItem.MyInformationSetting.screenRoute
            ) {
                MyInformationScreen(
                    onBackButtonClicked = { navController.navigateUp() }
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
        BottomNavItem.FriendList,
        BottomNavItem.ChatList,
        BottomNavItem.Setting
    )
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                alwaysShowLabel = true,
                onClick = {
                    navController.navigate(item.screenRoute) {
                        // 시작 스크린 제외한 모든 스택을 pop 하여 백스택이 많이 쌓이는 것을 방지
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // 같은 스크린이 여러번 쌓이는 것을 방지
                        launchSingleTop = true
                        // 같은 아이템이 선택됐을 때 원래 상태를 복원
                        restoreState = true
                    }
                },
                label = {
                    val label = when (item.screenRoute) {
                        FRIENDLIST -> {
                            stringResource(R.string.navigation_bar_label_friend)
                        }

                        CHATLIST -> {
                            stringResource(R.string.navigation_bar_label_chat)
                        }

                        SETTING -> {
                            stringResource(R.string.navigation_bar_label_setting)
                        }

                        else -> {
                            ""
                        }
                    }
                    Text(text = label)
                }
            )
        }
    }
}

sealed class BottomNavItem(
    val icons: ImageVector,
    val screenRoute: String
) {

    data object Login : BottomNavItem(
        Icons.Outlined.Settings,
        LOGIN
    )

    data object Signin : BottomNavItem(
        Icons.Outlined.Settings,
        SIGNIN
    )

    data object FriendList : BottomNavItem(
        Icons.Outlined.AccountCircle,
        FRIENDLIST
    )

    data object ChatList : BottomNavItem(
        Icons.Outlined.Email,
        CHATLIST
    )

    data object Setting : BottomNavItem(
        Icons.Outlined.Settings,
        SETTING
    )

    data object FindUser : BottomNavItem(
        Icons.Outlined.Settings,
        FINDUSER
    )

    data object ChatRoom : BottomNavItem(
        Icons.Outlined.Menu,
        CHATROOM
    )

    data object MyInfo : BottomNavItem(
        Icons.Outlined.ThumbUp,
        MYINFO
    )

    data object FriendInfo : BottomNavItem(
        Icons.Outlined.ThumbUp,
        FRIENDINFO
    )

    data object MyInfoEdit : BottomNavItem(
        Icons.Outlined.ThumbUp,
        MYINFOEDIT
    )

    data object FriendInfoEdit : BottomNavItem(
        Icons.Outlined.ThumbUp,
        FRIENDEDIT
    )

    data object FriendManage : BottomNavItem(
        Icons.Outlined.ThumbUp,
        FRIENDMANAGE
    )

    data object HidenUserManage : BottomNavItem(
        Icons.Outlined.ThumbUp,
        HIDENUSERMANAGE
    )

    data object BlockedUserManage : BottomNavItem(
        Icons.Outlined.ThumbUp,
        BLOCKEDUSERMANAGE
    )

    data object ChangeLanguage : BottomNavItem(
        Icons.Outlined.ThumbUp,
        CHANGELANGUAGE
    )

    data object ChangeTheme : BottomNavItem(
        Icons.Outlined.ThumbUp,
        CHANGETHEME
    )

    data object Conversation : BottomNavItem(
        Icons.Outlined.ThumbUp,
        CONVERSATION
    )

    data object ManageAccount : BottomNavItem(
        Icons.Outlined.ThumbUp,
        MANAGEACCOUNT
    )

    data object MyInformationSetting : BottomNavItem(
        Icons.Outlined.ThumbUp,
        MYINFORMATIONSETTING
    )
}
