package com.gyleedev.chatchat.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Query("SELECT * FROM user")
    fun getUsersWithPaging(): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM user")
    fun getUsers(): List<UserEntity>

    @Insert
    fun insertUser(user: UserEntity): Long

    @Update
    fun updateUser(user: UserEntity)

    @Query("DELETE FROM user")
    fun resetUser()
}
