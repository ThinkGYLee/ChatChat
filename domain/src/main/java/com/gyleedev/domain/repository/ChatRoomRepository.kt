package com.gyleedev.domain.repository

import androidx.paging.PagingData
import com.gyleedev.domain.model.ChatRoomData
import com.gyleedev.domain.model.ChatRoomLocalData
import com.gyleedev.domain.model.GetChatRoomState
import com.gyleedev.domain.model.ProcessResult
import com.gyleedev.domain.model.RelatedUserLocalData
import kotlinx.coroutines.flow.Flow

interface ChatRoomRepository {
    suspend fun getChatRoom(
        user: RelatedUserLocalData,
        getChatRoomState: GetChatRoomState
    ): GetChatRoomState

    fun checkChatRoomExistsInRemote(relatedUserLocalData: RelatedUserLocalData): Flow<Boolean>
    suspend fun createChatRoomData(): Flow<ChatRoomData?>
    fun createMyUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Flow<ProcessResult>

    fun createFriendUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Flow<ProcessResult>

    fun getChatRoomIdFromRemote(relatedUserLocalData: RelatedUserLocalData): Flow<String?>
    fun getChatRoomFromRemote(relatedUserLocalData: RelatedUserLocalData): Flow<ChatRoomData?>
    suspend fun insertChatRoomToLocal(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Long

    suspend fun makeNewChatRoom(rid: String, receiver: String): Long

    suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData?
    suspend fun resetChatRoomData()
    fun getChatRoomListWithPaging(): Flow<PagingData<ChatRoomLocalData>>
}
