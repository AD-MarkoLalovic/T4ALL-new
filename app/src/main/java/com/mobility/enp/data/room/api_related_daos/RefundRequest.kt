package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobility.enp.data.model.api_my_profile.refund_request.entity.DataRefundRequestEntity

@Dao
interface RefundRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefundRequest(refundRequest: List<DataRefundRequestEntity>)

    @Query("SELECT * from refund_request")
    suspend fun getAllRefundRequest(): List<DataRefundRequestEntity>

    @Query("DELETE from refund_request")
    suspend fun deleteRefundRequests()
}