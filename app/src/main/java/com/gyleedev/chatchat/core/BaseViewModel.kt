package com.gyleedev.chatchat.core

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.domain.FetchState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.net.SocketException
import java.net.UnknownHostException

abstract class BaseViewModel : ViewModel() {
    private val _fetchState = MutableSharedFlow<FetchState>()
    val fetchState: SharedFlow<FetchState> = _fetchState

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        when (throwable) {
            is SocketException -> emitException(FetchState.BAD_INTERNET)
            is HttpException -> emitException(FetchState.PARSE_ERROR)
            is UnknownHostException -> emitException(FetchState.WRONG_CONNECTION)
            else -> emitException(FetchState.FAIL)
        }
    }

    private fun emitException(fetchState: FetchState) {
        viewModelScope.launch {
            _fetchState.emit(fetchState)
        }
    }
}
