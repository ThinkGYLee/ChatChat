package com.gyleedev.chatchat.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FriendDao {

    @Query("SELECT * FROM friend")
    fun getUsersWithPaging(): PagingSource<Int, FriendEntity>

    @Query("SELECT * FROM friend")
    fun getUsers(): List<FriendEntity>

    @Insert
    fun insertUser(user: FriendEntity): Long

    @Update
    fun updateUser(user: FriendEntity)

    @Query("DELETE FROM friend")
    fun resetUser()
}
