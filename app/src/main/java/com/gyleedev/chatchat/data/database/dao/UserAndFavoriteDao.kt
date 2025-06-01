package com.gyleedev.chatchat.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gyleedev.chatchat.data.database.entity.UserAndFavoriteEntity
import com.gyleedev.chatchat.data.database.entity.UserEntity
import com.gyleedev.domain.model.UserRelationState
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAndFavoriteDao {

    @Transaction
    @Query("SELECT * FROM user")
    fun getUsersAndFavoriteWithPaging(): PagingSource<Int, UserEntity>

    @Transaction
    @Query("SELECT * FROM user")
    fun getUsers(): List<UserEntity>

    @Transaction
    @Insert
    fun insertUser(user: UserEntity): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUsers(users: List<UserEntity>)

    @Transaction
    @Query("SELECT * FROM user LIMIT 1")
    fun getLastUser(): UserEntity

    @Transaction
    @Query("SELECT * FROM user WHERE uid = :uid")
    fun getUserAndFavoriteByUid(uid: String): Flow<UserAndFavoriteEntity?>

    @Transaction
    @Update
    suspend fun updateUser(user: UserEntity)

    @Transaction
    @Query("DELETE FROM user")
    fun resetUser()

    @Transaction
    @Query("SELECT * FROM user WHERE relation = :userRelationState")
    fun getFriendsPaging(userRelationState: UserRelationState = UserRelationState.FRIEND): PagingSource<Int, UserEntity>

    @Transaction
    @Query("SELECT * FROM user where favoriteState = :favoriteState")
    fun getFavoritesPaging(favoriteState: Boolean = true): PagingSource<Int, UserAndFavoriteEntity>

    @Transaction
    @Query("SELECT * FROM user WHERE relation = :userRelationState")
    fun getHideUsersPaging(userRelationState: UserRelationState = UserRelationState.HIDE): PagingSource<Int, UserEntity>

    @Transaction
    @Query("SELECT COUNT(*) FROM user WHERE relation = :userRelationState")
    suspend fun getFriendsCount(userRelationState: UserRelationState = UserRelationState.FRIEND): Long

    @Transaction
    @Query("SELECT * FROM user")
    fun getFriends(): List<UserEntity>

    @Transaction
    @Query("SELECT * FROM user WHERE name LIKE :query AND relation = :relation")
    fun getFriendsWithName(
        query: String,
        relation: UserRelationState = UserRelationState.FRIEND
    ): PagingSource<Int, UserEntity>

    @Transaction
    @Query("SELECT * FROM user WHERE name LIKE :query AND relation = :relation")
    fun getHideFriendsWithName(
        query: String,
        relation: UserRelationState = UserRelationState.HIDE
    ): PagingSource<Int, UserEntity>

    // TODO fts4 관련 쿼리문 수정할것
    @Transaction
    @Query("SELECT * FROM user JOIN user_fts ON (user.id = user_fts.id) WHERE user_fts.name MATCH :query AND relation = :relation")
    fun getHideFriendsWithNameFullText(
        query: String,
        relation: UserRelationState = UserRelationState.HIDE
    ): PagingSource<Int, UserEntity>

    @Transaction
    @Query("SELECT * FROM user")
    fun getRelatedUsers(): List<UserEntity>

    @Transaction
    @Query("SELECT * FROM user")
    fun getAllRelatedUsersAsFlow(): Flow<List<UserEntity>>

    @Transaction
    @Query("DELETE FROM user")
    suspend fun resetUserDatabase()

    @Transaction
    @Query("DELETE FROM user where uid = :uid")
    suspend fun deleteFriend(uid: String)
}
