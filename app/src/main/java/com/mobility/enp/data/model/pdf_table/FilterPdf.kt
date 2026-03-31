package com.mobility.enp.data.model.pdf_table

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_table_history")
data class FilterPdf(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val pdfData: ByteArray,
    val createdAt: Long = System.currentTimeMillis()
)