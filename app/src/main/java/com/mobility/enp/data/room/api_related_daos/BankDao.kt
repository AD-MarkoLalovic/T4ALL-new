package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.banks.entity.BanksEntity

@Dao
interface BankDao {
    @Upsert
    suspend fun insertBanks(banks: List<BanksEntity>)

    @Query("SELECT * FROM banks")
    suspend fun getAllBanks(): List<BanksEntity>
}