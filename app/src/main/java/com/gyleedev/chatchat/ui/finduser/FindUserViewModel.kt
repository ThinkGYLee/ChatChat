package com.gyleedev.chatchat.ui.finduser

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.usecase.GetUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindUserViewModel @Inject constructor(
    private val useCase: GetUserDataUseCase
) : BaseViewModel() {

    private val _emailQuery = MutableStateFlow("")
    val emailQuery: StateFlow<String> = _emailQuery

    private val _emailIsAvailable = MutableStateFlow(false)
    val emailIsAvailable: StateFlow<Boolean> = _emailIsAvailable

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _searchFailure = MutableSharedFlow<Unit>()
    val searchFailure: SharedFlow<Unit> = _searchFailure

    fun editEmail(email: String) {
        viewModelScope.launch {
            _emailQuery.emit(email)
            emailValidator()
        }
    }

    private fun emailValidator() {
        viewModelScope.launch {
            val pattern = android.util.Patterns.EMAIL_ADDRESS
            _emailIsAvailable.emit(pattern.matcher(_emailQuery.value).matches())
        }
    }

    fun fetchUserData() {
        viewModelScope.launch {
            val fetchUserdata = useCase(emailQuery.value)
            fetchUserdata.collect { value ->
                if (value == null) {
                    _searchFailure.emit(Unit)
                } else {
                    _userData.emit(value)
                }
            }
        }
    }
}
