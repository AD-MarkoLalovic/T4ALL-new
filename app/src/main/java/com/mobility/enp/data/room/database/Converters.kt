package com.mobility.enp.data.room.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobility.enp.data.model.api_home_page.homedata.Data
import com.mobility.enp.data.model.api_my_invoices.DataMonthly
import com.mobility.enp.data.model.api_tool_history.listing.InvoiceData
import java.io.ByteArrayOutputStream

@TypeConverters
class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromDrawable(drawable: Drawable?): String? {
        if (drawable == null) return null
        val bitmap = (drawable as BitmapDrawable).bitmap
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    @TypeConverter
    fun toDrawable(base64String: String?): Drawable? {
        if (base64String == null) return null
        val bytes = Base64.decode(base64String, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return BitmapDrawable(null, bitmap)
    }

    @TypeConverter
    fun fromData(data: Data?): String? {
        return gson.toJson(data)
    }

    @TypeConverter
    fun toData(jsonString: String?): Data? {
        return gson.fromJson(jsonString, Data::class.java)
    }

    @TypeConverter
    fun fromDataHistory(data: com.mobility.enp.data.model.api_tool_history.index.Data?): String? {
        return gson.toJson(data)
    }

    @TypeConverter
    fun toDataHistory(jsonString: String?): com.mobility.enp.data.model.api_tool_history.index.Data? {
        return gson.fromJson(
            jsonString,
            com.mobility.enp.data.model.api_tool_history.index.Data::class.java
        )
    }

    @TypeConverter
    fun fromDataHistoryListing(data: InvoiceData): String? {
        return gson.toJson(data)
    }

    @TypeConverter
    fun toDataHistoryListing(jsonString: String?): InvoiceData? {
        return gson.fromJson(jsonString, InvoiceData::class.java)
    }

    @TypeConverter
    fun fromDataMonthlyBills(data: DataMonthly): String? {
        return gson.toJson(data)
    }

    @TypeConverter
    fun toDataMonthlyBills(jsonString: String?): DataMonthly? {
        return gson.fromJson(jsonString, DataMonthly::class.java)
    }

    @TypeConverter
    fun fromListInt(list: List<Int>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toListInt(jsonString: String?): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(jsonString, type)
    }

}