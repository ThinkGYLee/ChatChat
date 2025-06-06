package com.gyleedev.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gyleedev.data.database.entity.UserEntity
import com.gyleedev.domain.model.UserRelationState
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM user")
    fun getUsersWithPaging(): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM user")
    fun getUsers(): List<UserEntity>

    @Insert
    fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUsers(users: List<UserEntity>)

    @Query("SELECT * FROM user LIMIT 1")
    fun getLastUser(): UserEntity

    @Query("SELECT * FROM user WHERE uid = :uid")
    fun getUserInfoByUid(uid: String): Flow<UserEntity?>

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM user")
    fun resetUser()

    @Query("SELECT * FROM user WHERE relation = :userRelationState")
    fun getFriendsPaging(userRelationState: UserRelationState = UserRelationState.FRIEND): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM user WHERE relation = :userRelationState")
    fun getHideUsersPaging(userRelationState: UserRelationState = UserRelationState.HIDE): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM user WHERE relation = :userRelationState")
    fun getBlockedUsersPaging(userRelationState: UserRelationState = UserRelationState.BLOCKED): PagingSource<Int, UserEntity>

    @Query("SELECT COUNT(*) FROM user WHERE relation = :userRelationState")
    fun getFriendsCount(userRelationState: UserRelationState = UserRelationState.FRIEND): Long

    @Query("SELECT * FROM user")
    fun getFriends(): List<UserEntity>

    @Query("SELECT * FROM user WHERE name LIKE :query AND relation = :relation")
    fun getFriendsWithName(
        query: String,
        relation: UserRelationState = UserRelationState.FRIEND
    ): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM user WHERE name LIKE :query AND relation = :relation")
    fun getHideFriendsWithName(
        query: String,
        relation: UserRelationState = UserRelationState.HIDE
    ): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM user WHERE name LIKE :query AND relation = :relation")
    fun getBlockedFriendsWithName(
        query: String,
        relation: UserRelationState = UserRelationState.BLOCKED
    ): PagingSource<Int, UserEntity>

    // TODO fts4 관련 쿼리문 수정할것
    @Query("SELECT * FROM user JOIN user_fts ON (user.id = user_fts.id) WHERE user_fts.name MATCH :query AND relation = :relation")
    fun getHideFriendsWithNameFullText(
        query: String,
        relation: UserRelationState = UserRelationState.HIDE
    ): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM user")
    fun getRelatedUsers(): List<UserEntity>

    @Query("SELECT * FROM user")
    fun getAllRelatedUsersAsFlow(): Flow<List<UserEntity>>

    @Query("DELETE FROM user")
    suspend fun resetUserDatabase()

    @Query("DELETE FROM user where uid = :uid")
    suspend fun deleteFriend(uid: String)
}
