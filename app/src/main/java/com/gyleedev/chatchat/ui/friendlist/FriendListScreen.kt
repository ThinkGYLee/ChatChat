package com.gyleedev.chatchat.ui.friendlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.chatchat.R
import com.gyleedev.chatchat.domain.UserData
import com.skydoves.landscapist.glide.GlideImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(
    onFindUserButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FriendListViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchState.collect {
        }
    }

    val myUserData = viewModel.myUserData.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "친구")
                },
                actions = {
                    IconButton(onClick = { onFindUserButtonClick() }) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "add friend")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            if (myUserData.value != null) {
                MyUserData(onClick = {}, userData = myUserData.value!!)
            }

            Row {
                Text(
                    text = "친구",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "4",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(text = "이금용")
            }
            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(text = "이호용")
            }
            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(text = "엄마")
            }
            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(text = "아빠")
            }
        }
    }
}

@Composable
fun MyUserData(onClick: () -> Unit, userData: UserData, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(imageModel = { userData.picture.ifBlank { R.drawable.icons8__ } })
        Spacer(modifier = Modifier.width(20.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Text(text = userData.name)
            if (userData.status.isNotBlank()) {
                Text(
                    text = userData.status,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
