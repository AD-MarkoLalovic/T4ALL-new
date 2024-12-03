package com.mobility.enp.data.model.pdf_table

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "CSV")
class CsvTable(
    @PrimaryKey(autoGenerate = true) val id: Int, val data: ByteArray
)
