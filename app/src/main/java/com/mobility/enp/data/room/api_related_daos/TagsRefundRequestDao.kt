package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_my_profile.refund_request.tags.entity.TagsRefundRequestEntity

@Dao
interface TagsRefundRequestDao {

    @Upsert
    suspend fun insertTagsRefundRequest(tags: List<TagsRefundRequestEntity>)

    @Query("SELECT * FROM tags_refund_request")
    suspend fun getTagsRefundRequest(): List<TagsRefundRequestEntity>

    @Query("DELETE FROM tags_refund_request")
    suspend fun deleteTagsRefundRequest()
}