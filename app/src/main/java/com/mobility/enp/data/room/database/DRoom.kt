package com.mobility.enp.data.room.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mobility.enp.Config
import com.mobility.enp.data.model.IntroPageStatus
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.api_home_page.homedata.HomeScreenData
import com.mobility.enp.data.model.api_home_page.homedata.Promotion
import com.mobility.enp.data.model.api_my_invoices.MyInvoicesResponse
import com.mobility.enp.data.model.api_my_profile.basic_information.entity.BasicInfoEntity
import com.mobility.enp.data.model.api_my_profile.refund_request.entity.DataRefundRequestEntity
import com.mobility.enp.data.model.api_my_profile.refund_request.tags.entity.TagsRefundRequestEntity
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.api_room_models.UserLanguage
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.listing.ToolHistoryListing
import com.mobility.enp.data.model.banks.entity.BanksEntity
import com.mobility.enp.data.model.home.entity.HomeEntity
import com.mobility.enp.data.model.notification.NotificationModel
import com.mobility.enp.data.model.pdf_table.CsvTable
import com.mobility.enp.data.model.pdf_table.PdfTable
import com.mobility.enp.data.room.CsvDao
import com.mobility.enp.data.room.LastUser
import com.mobility.enp.data.room.LastUserDao
import com.mobility.enp.data.room.LoginDao
import com.mobility.enp.data.room.PdfDao
import com.mobility.enp.data.room.UserLanguageDao
import com.mobility.enp.data.room.api_related_daos.BankDao
import com.mobility.enp.data.room.api_related_daos.BasicInfoDao
import com.mobility.enp.data.room.api_related_daos.FcmTokenDao
import com.mobility.enp.data.room.api_related_daos.HistoryIndexDao
import com.mobility.enp.data.room.api_related_daos.HistoryListingDao
import com.mobility.enp.data.room.api_related_daos.HomeDao
import com.mobility.enp.data.room.api_related_daos.HomeScreenDao
import com.mobility.enp.data.room.api_related_daos.IntroPageStatusDao
import com.mobility.enp.data.room.api_related_daos.MyInvoicesDao
import com.mobility.enp.data.room.api_related_daos.ProfileImageDao
import com.mobility.enp.data.room.api_related_daos.PromotionsDao
import com.mobility.enp.data.room.api_related_daos.RefundRequestDao
import com.mobility.enp.data.room.api_related_daos.TagsRefundRequestDao
import com.mobility.enp.data.room.notification.NotificationDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserLoginResponseRoomTable::class, FcmToken::class, UserLanguage::class, NotificationModel::class, HomeScreenData::class, IndexData::class, ToolHistoryListing::class,
        IntroPageStatus::class, ProfileImage::class, MyInvoicesResponse::class, PdfTable::class, Promotion::class, LastUser::class, BanksEntity::class, DataRefundRequestEntity::class, CsvTable::class, TagsRefundRequestEntity::class,
        BasicInfoEntity::class, HomeEntity::class],
    version = 161,
    exportSchema = false
)  // changes on tables require  version of database to be incremented  // also requires database data destruction or migration
@TypeConverters(Converters::class)
abstract class DRoom : RoomDatabase() {

    abstract fun loginDao(): LoginDao // modified for api response
    abstract fun homeDao(): HomeDao
    abstract fun pdfDao(): PdfDao
    abstract fun languageDao(): UserLanguageDao
    abstract fun fcmToken(): FcmTokenDao
    abstract fun toolHistoryDao(): HistoryIndexDao
    abstract fun toolListingDao(): HistoryListingDao
    abstract fun introStateDao(): IntroPageStatusDao
    abstract fun profileImageDao(): ProfileImageDao
    abstract fun myInvoicesDao(): MyInvoicesDao
    abstract fun notificationDao(): NotificationDao
    abstract fun promotionsDao(): PromotionsDao
    abstract fun lastUserDao(): LastUserDao // dont delete this on logout
    abstract fun refundRequestDao(): RefundRequestDao
    abstract fun csvTableDao(): CsvDao
    abstract fun bankDao(): BankDao
    abstract fun tagsRefundRequest(): TagsRefundRequestDao
    abstract fun basicInfoDao(): BasicInfoDao
    abstract fun homeScreenDao(): HomeScreenDao


    companion object {
        private var instance: DRoom? = null
        const val TAG = "ROOM"

        fun getRoomInstance(context: Context): DRoom {
            if (instance == null) {
                synchronized(DRoom::class) {
                    instance = buildDatabase(context)
                    prepopulateDatabase(instance!!)
                }
            }
            return instance!!
        }

        private fun prepopulateDatabase(db: DRoom) {
            CoroutineScope(Dispatchers.IO).launch {

                val getInvoicesTable = instance?.languageDao()?.getTableSize()
                if (getInvoicesTable == 0) {
                    db.languageDao().insert(UserLanguage("en"))
                }
            }
        }

        fun buildDatabase(context: Context): DRoom {  // its a singleton
            if (instance == null) {
                Log.d(TAG, "getRoomInstance: null creating new instance")
                synchronized(DRoom::class) {  // factory is cypher for sql
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DRoom::class.java,
                        Config.TABLE_NAME
                    ).fallbackToDestructiveMigration().build()
                }
            }
            return instance!!
        }

    }

    suspend fun clearAllData() {
        homeDao().deleteData()
        loginDao().deleteAll()
        notificationDao().deleteAll()
        fcmToken().deleteTable()
        toolHistoryDao().deleteData()
        toolListingDao().deleteData()
        myInvoicesDao().deleteDataMonthlyInvoices()
        pdfDao().deleteData()
        promotionsDao().deleteAllPromotions()
        refundRequestDao().deleteRefundRequests()
        tagsRefundRequest().deleteTagsRefundRequest()
        basicInfoDao().deleteBasicInfo()
        homeScreenDao().deleteHomeScreenData()
    }

}
