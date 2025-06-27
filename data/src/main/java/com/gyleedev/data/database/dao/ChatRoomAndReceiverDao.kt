package com.gyleedev.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gyleedev.data.database.entity.ChatRoomAndReceiverEntity
import com.gyleedev.data.database.entity.ChatRoomEntity

@Dao
interface ChatRoomAndReceiverDao {

    @Transaction
    @Query(
        """
        SELECT * FROM chatroom
        WHERE isGroup = :isGroup AND id IN (
            SELECT chatroom_entity_id FROM receiver WHERE receiver = :receiver
        )
    """
    )
    fun getChatRoomAndReceiverByUid(
        receiver: String,
        isGroup: Boolean = false
    ): ChatRoomAndReceiverEntity?

    @Transaction
    @Query("SELECT * FROM chatroom WHERE rid = :rid")
    fun getChatRoomAndReceiverByRid(rid: String): ChatRoomAndReceiverEntity?

    @Transaction
    @Query("SELECT * FROM chatroom")
    fun getChatRoomsWithPaging(): PagingSource<Int, ChatRoomAndReceiverEntity>
}
