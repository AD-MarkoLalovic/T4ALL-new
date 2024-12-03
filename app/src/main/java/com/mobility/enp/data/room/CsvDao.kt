package com.mobility.enp.data.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.pdf_table.CsvTable

@Dao
interface CsvDao {

    @Upsert
    suspend fun upsertData(pdfStream: CsvTable)

    @Query("DELETE FROM CSV")
    suspend fun deleteData()

    @Query("SELECT * FROM CSV")
    suspend fun fetchData(): CsvTable

}