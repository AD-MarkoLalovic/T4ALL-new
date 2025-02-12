package com.mobility.enp.util

import android.content.Context
import android.content.res.AssetManager
import java.io.IOException

object AssetHelper {

    fun getFileNames(context: Context, folderPath: String): List<String> {
        val fileNames = mutableListOf<String>()
        val assetManager: AssetManager = context.assets

        try {
            val files = assetManager.list(folderPath)
            if (files != null) {
                for (file in files) {
                    fileNames.add("$folderPath/$file")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return fileNames
    }

}