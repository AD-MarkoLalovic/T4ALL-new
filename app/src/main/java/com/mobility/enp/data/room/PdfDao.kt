package com.mobility.enp.data.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.pdf_table.PdfTable

@Dao
interface PdfDao {

    @Upsert
    suspend fun upsertData(pdfStream: PdfTable)

    @Query("DELETE FROM PDF")
    suspend fun deleteData()

    @Query("SELECT * FROM PDF")
    suspend fun fetchData(): PdfTable?

}