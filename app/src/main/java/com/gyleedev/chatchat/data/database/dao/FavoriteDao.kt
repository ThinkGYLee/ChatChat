package com.gyleedev.chatchat.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gyleedev.chatchat.data.database.entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorite")
    fun getUsersWithPaging(): PagingSource<Int, FavoriteEntity>

    @Query("SELECT * FROM favorite WHERE user_entity_id = :userEntityId")
    fun getFavoriteByUid(userEntityId: Long): FavoriteEntity

    @Update
    fun updateFavorite(favoriteEntity: FavoriteEntity)

    @Insert
    fun insertFavorite(favoriteEntity: FavoriteEntity): Long

    @Query("SELECT COUNT(*) FROM favorite WHERE favorite_state = :favoriteState")
    fun getFavoriteCount(favoriteState: Boolean = true): Int
}
