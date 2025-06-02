package com.gyleedev.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gyleedev.data.database.entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorite")
    fun getUsersWithPaging(): PagingSource<Int, FavoriteEntity>

    @Query("SELECT * FROM favorite WHERE user_entity_id = :userEntityId")
    fun getFavoriteByUserEntityId(userEntityId: Long): FavoriteEntity

    @Query("SELECT * FROM favorite WHERE favorite_number> :favoriteNumber")
    fun getFavoritesForSort(favoriteNumber: Long): List<FavoriteEntity>

    @Update
    fun updateFavorite(favoriteEntity: FavoriteEntity)

    @Insert
    fun insertFavorite(favoriteEntity: FavoriteEntity): Long

    @Query("SELECT COUNT(*) FROM favorite WHERE favorite_state = :favoriteState")
    fun getFavoriteCount(favoriteState: Boolean = true): Int
}
