package com.mobility.enp.data.room.database

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.reflect.TypeToken
import com.mobility.enp.data.model.api_tool_history.listing.InvoiceData
import com.mobility.enp.data.model.api_tool_history.v2base_model.Data

@TypeConverters
class Converters {

    private val gson = GsonBuilder().setStrictness(Strictness.LENIENT)
        .serializeNulls()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

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
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): ArrayList<String> {
        if (value.isNullOrEmpty()) return arrayListOf()
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromDataMonthlyBills(data: com.mobility.enp.data.model.api_my_invoices.refactor.Data?): String? {
        synchronized(this) {
            val safeData = data?.copy()
            return gson.toJson(safeData)
        }
    }

    @TypeConverter
    fun toDataMonthlyBills(jsonString: String?): com.mobility.enp.data.model.api_my_invoices.refactor.Data? {
        synchronized(this) {
            return gson.fromJson(
                jsonString,
                com.mobility.enp.data.model.api_my_invoices.refactor.Data::class.java
            )
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

    @TypeConverter
    fun fromData(data: Data?): String? {
        val safeCopy = data?.deepImmutableCopy()
        return gson.toJson(safeCopy)
    }

    @TypeConverter
    fun toData(dataString: String?): Data? {
        if (dataString.isNullOrEmpty()) return null
        val type = object : TypeToken<Data>() {}.type
        return gson.fromJson(dataString, type)
    }

}