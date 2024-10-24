package com.gyleedev.chatchat.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gyleedev.chatchat.data.database.entity.FriendEntity

@Dao
interface FriendDao {

    @Query("SELECT * FROM friend")
    fun getUsersWithPaging(): PagingSource<Int, FriendEntity>

    @Query("SELECT * FROM friend")
    fun getUsers(): List<FriendEntity>

    @Insert
    fun insertUser(user: FriendEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUsers(users: List<FriendEntity>)

    @Query("SELECT * FROM friend LIMIT 1")
    fun getLastFriend(): FriendEntity

    @Query("SELECT * FROM friend WHERE uid = :uid")
    fun getFriendByUid(uid: String): FriendEntity

    @Update
    fun updateUser(user: FriendEntity)

    @Query("DELETE FROM friend")
    fun resetUser()

    @Query("SELECT * FROM friend")
    fun getFriends(): PagingSource<Int, FriendEntity>

    @Query("SELECT COUNT(*) FROM friend")
    fun getFriendsCount(): Long
}
