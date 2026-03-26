package com.mobility.enp.data.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.pdf_table.FilterPdf

@Dao
interface PdfDaoHistory {

    @Upsert
    suspend fun upsertData(pdfStream: FilterPdf)

    @Query("DELETE FROM pdf_table_history")
    suspend fun deleteData()

    @Query("SELECT * FROM pdf_table_history")
    suspend fun fetchData(): FilterPdf?


}