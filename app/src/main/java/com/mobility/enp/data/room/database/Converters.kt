package com.mobility.enp.data.room.database

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.reflect.TypeToken
import com.mobility.enp.data.model.api_home_page.homedata.Data
import com.mobility.enp.data.model.api_my_invoices.DataMonthly
import com.mobility.enp.data.model.api_tool_history.listing.InvoiceData

@TypeConverters
class Converters {

    private val gson = GsonBuilder().setStrictness(Strictness.LENIENT)
        .serializeNulls()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

    @TypeConverter
    fun fromData(data: Data?): String? {
        synchronized(this) {  // Ensures that no other thread modifies data while Gson is serializing it.
            val safeData = data?.copy() // Ensure a safe copy before serialization
            return gson.toJson(safeData)
        }
    }

    @TypeConverter
    fun toData(jsonString: String?): Data? {
        synchronized(this) {
            return gson.fromJson(jsonString, Data::class.java)
        }
    }

    @TypeConverter
    fun fromDataHistory(data: com.mobility.enp.data.model.api_tool_history.index.Data?): String? {
        synchronized(this) {
            val safeData = data?.copy()
            return gson.toJson(safeData)
        }
    }

    @TypeConverter
    fun toDataHistory(jsonString: String?): com.mobility.enp.data.model.api_tool_history.index.Data? {
        synchronized(this) {
            return gson.fromJson(
                jsonString,
                com.mobility.enp.data.model.api_tool_history.index.Data::class.java
            )
        }
    }

    @TypeConverter
    fun fromDataHistoryListing(data: InvoiceData?): String? {
        synchronized(this) {
            val safeData = data?.copy()
            return gson.toJson(safeData)
        }
    }

    @TypeConverter
    fun toDataHistoryListing(jsonString: String?): InvoiceData? {
        synchronized(this) {
            return gson.fromJson(jsonString, InvoiceData::class.java)
        }
    }

    @TypeConverter
    fun fromDataMonthlyBills(data: DataMonthly?): String? {
        synchronized(this) {
            val safeData = data?.copy()
            return gson.toJson(safeData)
        }
    }

    @TypeConverter
    fun toDataMonthlyBills(jsonString: String?): DataMonthly? {
        synchronized(this) {
            return gson.fromJson(jsonString, DataMonthly::class.java)
        }
    }

    @TypeConverter
    fun fromListInt(list: List<Int>?): String? {
        synchronized(this) {
            return gson.toJson(list)
        }
    }

    @TypeConverter
    fun toListInt(jsonString: String?): List<Int> {
        synchronized(this) {
            val type = object : TypeToken<List<Int>>() {}.type
            return gson.fromJson(jsonString, type)
        }
    }

}