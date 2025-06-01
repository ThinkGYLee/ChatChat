package com.gyleedev.chatchat.domain.repository

import androidx.paging.PagingSource
import com.gyleedev.chatchat.data.database.entity.ChatRoomEntity
import com.gyleedev.chatchat.domain.model.ChatRoomData
import com.gyleedev.chatchat.domain.model.ChatRoomLocalData
import com.gyleedev.chatchat.domain.model.RelatedUserLocalData
import kotlinx.coroutines.flow.Flow

interface ChatRoomRepository {
    fun checkChatRoomExistsInRemote(relatedUserLocalData: RelatedUserLocalData): Flow<Boolean>
    suspend fun createChatRoomData(): Flow<ChatRoomData?>
    suspend fun createMyUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    )

    suspend fun createFriendUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    )

    fun getChatRoomIdFromRemote(relatedUserLocalData: RelatedUserLocalData): Flow<String?>
    fun getChatRoomFromRemote(relatedUserLocalData: RelatedUserLocalData): Flow<ChatRoomData?>
    suspend fun insertChatRoomToLocal(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Long

    suspend fun makeNewChatRoom(rid: String, receiver: String): Long

    suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData
    suspend fun resetChatRoomData()
    fun getChatRoomListWithPaging(): PagingSource<Int, ChatRoomEntity>
}
