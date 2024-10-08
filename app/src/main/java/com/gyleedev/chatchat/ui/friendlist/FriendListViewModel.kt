package com.gyleedev.chatchat.ui.friendlist

import com.google.firebase.auth.FirebaseAuth
import com.gyleedev.chatchat.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FriendListViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : BaseViewModel() {

}
