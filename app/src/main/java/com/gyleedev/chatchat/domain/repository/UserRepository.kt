package com.gyleedev.chatchat.domain.repository

import androidx.paging.PagingData
import com.gyleedev.chatchat.domain.model.ChangeRelationResult
import com.gyleedev.chatchat.domain.model.LogInResult
import com.gyleedev.chatchat.domain.model.ProcessResult
import com.gyleedev.chatchat.domain.model.RelatedUserLocalData
import com.gyleedev.chatchat.domain.model.RelatedUserRemoteData
import com.gyleedev.chatchat.domain.model.SearchUserResult
import com.gyleedev.chatchat.domain.model.SignInResult
import com.gyleedev.chatchat.domain.model.UserData
import com.gyleedev.chatchat.domain.model.UserRelationState
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsersFromLocal(): List<UserData>

    suspend fun signInUser(id: String, password: String, nickname: String): Flow<UserData?>
    suspend fun loginRequest(id: String, password: String): Flow<LogInResult>
    suspend fun logoutRequest()

    fun fetchUserExists(): Boolean
    suspend fun writeUserToRemote(user: UserData): Flow<SignInResult>
    suspend fun getMyDataFromRemote(): Flow<UserData?>
    suspend fun addRelatedUserToRemote(
        user: UserData,
        userRelation: UserRelationState = UserRelationState.FRIEND
    ): Flow<Boolean>

    suspend fun getMyRelatedUserListFromRemote(): Flow<List<RelatedUserRemoteData>?>
    suspend fun insertMyRelationsToLocal(list: List<RelatedUserRemoteData>)
    suspend fun insertFriendToLocal(user: UserData): Flow<Boolean>
    fun getFriends(): Flow<PagingData<RelatedUserLocalData>>
    fun getFavorites(): Flow<PagingData<RelatedUserLocalData>>
    suspend fun getFriendsCount(): Long

    fun getHideFriends(): Flow<PagingData<RelatedUserLocalData>>
    fun getBlockedFriends(): Flow<PagingData<RelatedUserLocalData>>

    fun getFriendById(uid: String): Flow<RelatedUserLocalData>
    fun getFriendAndFavoriteByUid(uid: String): Flow<RelatedUserLocalData>

    fun getMyUidFromLogInData(): String?

    suspend fun updateMyUserInfo(user: UserData): Flow<Boolean>
    suspend fun getUserInfoFromRemote(uid: String): Flow<UserData?>
    fun getRelatedUserListFromLocal(): Flow<List<RelatedUserLocalData>>
    suspend fun updateRelatedUserInfoWithUserEntity(userData: RelatedUserLocalData)
    suspend fun updateUserInfoByUid(uid: String)
    suspend fun resetFriendData()
    suspend fun resetMyUserData()
    fun setMyUserInformation(userData: UserData)
    suspend fun deleteFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult
    suspend fun changeRelatedUserRemote(relatedUserLocalData: RelatedUserLocalData): Flow<ProcessResult>
    fun getRelatedUsersForChatRoomList(): List<RelatedUserLocalData>
    suspend fun changeRelatedUserLocal(relatedUserLocalData: RelatedUserLocalData): Flow<ChangeRelationResult>
    suspend fun hideFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult
    suspend fun blockRelatedUserRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult
    suspend fun blockUnknownUserRequest(userData: UserData): ChangeRelationResult
    suspend fun userToFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult
    fun getFriendsWithName(query: String): Flow<PagingData<RelatedUserLocalData>>
    fun getHideFriendsWithName(query: String): Flow<PagingData<RelatedUserLocalData>>
    fun getBlockedFriendsWithName(query: String): Flow<PagingData<RelatedUserLocalData>>
    fun getHideFriendsWithFullTextName(query: String): Flow<PagingData<RelatedUserLocalData>>
    fun updateUserAndFavorite(relatedUserLocalData: RelatedUserLocalData): Flow<Boolean>
    fun getMyUserDataFromPreference(): UserData
    fun addUserToRemoteBlockedEntity(relatedUserLocalData: RelatedUserLocalData): Flow<ProcessResult>
    suspend fun searchUserRequest(email: String): Flow<SearchUserResult>
    suspend fun searchUser(email: String): Flow<UserData?>
}
