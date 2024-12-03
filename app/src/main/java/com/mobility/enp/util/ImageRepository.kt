package com.mobility.enp.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.room.database.DRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageRepository(private val context: Context) {

    private val database: DRoom = DRoom.getRoomInstance(context)

    suspend fun saveImageToStorage(bitmap: Bitmap?, displayName: String) {
        bitmap?.let {
            val imagePath = saveImageToFile(bitmap, displayName, context)
            imagePath?.let { newImagePath ->
                try {
                    withContext(Dispatchers.IO) {
                        // Dobivanje starog zapisa slike
                        val oldProfileImage =
                            database.profileImageDao().getProfileImage(displayName)

                        // Brišem staru sliku ako postoji
                        oldProfileImage?.imagePath?.let { oldImagePath ->
                            deleteImageFile(oldImagePath)
                        }

                        // Brišem stari zapis slike
                        oldProfileImage?.let {
                            database.profileImageDao().deleteImage(displayName)
                        }

                        // Dodajem novi zapis s novom putanjom
                        val newProfileImage = ProfileImage(displayName, newImagePath)
                        database.profileImageDao().insertImage(newProfileImage)
                    }
                } catch (e: Exception) {
                    Log.e("ImageRepository", "Error saving image: ${e.message}")
                }
            }
        }
    }

    private suspend fun saveImageToFile(
        bitmap: Bitmap,
        displayName: String,
        context: Context
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                directory?.mkdirs()  // Stvori sve potrebne direktorije ako ne postoje

                // // Generiranje jedinstvenog imena za datoteku s slikom
                val safeDisplayName = displayName.replace(
                    "[^a-zA-Z0-9.-]".toRegex(),
                    "_"
                )  // Zamjena nevaljanih znakova
                val uniqueFileName = "${safeDisplayName}_${System.currentTimeMillis()}.jpg"
                val file = File(directory, "$uniqueFileName.jpg")

                val outputStream = FileOutputStream(file)
                outputStream.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
                    it.flush()
                }

                val imagePath = file.absolutePath
                imagePath  // Vrati putanju slike ako je uspešno spremljena
            } catch (e: IOException) {
                null  // Vrati null u slučaju greške
            }
        }
    }

    suspend fun getAndSetProfileImage(imageView: ImageView, displayName: String) {
        val lastImage = withContext(Dispatchers.IO) {
            database.profileImageDao().getProfileImage(displayName)
        }

        lastImage?.let { profileImage ->
            displayProfileImage(imageView, profileImage)
        }
    }

    private fun displayProfileImage(imageView: ImageView, profileImage: ProfileImage) {
        val imageFile = File(profileImage.imagePath)
        if (imageFile.exists()) {

            val glideOption = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Omogućava keširanje učitanih slika
                .override(imageView.width, imageView.height) // Velicina slike
                .centerCrop() //Nacin skaliranja
                .transform(CircleCrop())
                .format(DecodeFormat.PREFER_RGB_565) // Preferirani format slike
            // ARGB_8888 za bolji kvalitet slike

            Glide.with(imageView.context)
                .load(imageFile)
                .apply(glideOption)
                .into(imageView)
        }
    }

    private fun deleteImageFile(imagePath: String) {
        val imageFile = File(imagePath)
        if (imageFile.exists()) {
            if (imageFile.delete()) {
                Log.d("ImageRepository", "Image deleted successfully")
            } else {
                Log.e("ImageRepository", "Failed to delete image")
            }
        } else {
            Log.w("ImageRepository", "Image file does not exist at path: $imagePath")
        }
    }

}
