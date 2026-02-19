package com.mobility.enp.data.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mobility.enp.Config
import com.mobility.enp.data.model.IntroPageStatus
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.api_my_invoices.refactor.MyInvoicesResponse
import com.mobility.enp.data.model.api_my_profile.basic_information.entity.BasicInfoEntity
import com.mobility.enp.data.model.api_my_profile.refund_request.entity.DataRefundRequestEntity
import com.mobility.enp.data.model.api_my_profile.refund_request.tags.entity.TagsRefundRequestEntity
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2AllowedCountries
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseCroatia
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseCroatiaResult
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseResult
import com.mobility.enp.data.model.banks.entity.BanksEntity
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.data.model.home.entity.HomeEntity
import com.mobility.enp.data.model.home.entity.InvoiceHomeEntity
import com.mobility.enp.data.model.home.entity.InvoiceHomeTotalCurrencyEntity
import com.mobility.enp.data.model.home.entity.TollHistoryHomeEntity
import com.mobility.enp.data.model.notification.NotificationModel
import com.mobility.enp.data.model.pdf_table.CsvTable
import com.mobility.enp.data.model.pdf_table.PdfTable
import com.mobility.enp.data.room.CsvDao
import com.mobility.enp.data.room.LastUser
import com.mobility.enp.data.room.LastUserDao
import com.mobility.enp.data.room.LoginDao
import com.mobility.enp.data.room.PdfDao
import com.mobility.enp.data.room.api_related_daos.BankDao
import com.mobility.enp.data.room.api_related_daos.BasicInfoDao
import com.mobility.enp.data.room.api_related_daos.FcmTokenDao
import com.mobility.enp.data.room.api_related_daos.HomeCardsDao
import com.mobility.enp.data.room.api_related_daos.HomeScreenDao
import com.mobility.enp.data.room.api_related_daos.IntroPageStatusDao
import com.mobility.enp.data.room.api_related_daos.MyInvoicesDao
import com.mobility.enp.data.room.api_related_daos.ProfileImageDao
import com.mobility.enp.data.room.api_related_daos.RefundRequestDao
import com.mobility.enp.data.room.api_related_daos.TagsRefundRequestDao
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2AllowedCountryDao
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2Dao
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2DaoCroatia
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2DaoCroatiaResult
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2DaoResult
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2TagsSerials
import com.mobility.enp.data.room.notification.NotificationDao

@Database(
    entities = [UserLoginResponseRoomTable::class, FcmToken::class, NotificationModel::class, IndexData::class,
        IntroPageStatus::class, ProfileImage::class, MyInvoicesResponse::class, PdfTable::class, LastUser::class, BanksEntity::class, DataRefundRequestEntity::class, CsvTable::class, TagsRefundRequestEntity::class,
        BasicInfoEntity::class, HomeEntity::class, V2HistoryTagResponse::class, TollHistoryHomeEntity::class, InvoiceHomeEntity::class, InvoiceHomeTotalCurrencyEntity::class,
        HomeCardsEntity::class, V2HistoryTagResponseCroatia::class, V2AllowedCountries::class,
        V2HistoryTagResponseResult::class, V2HistoryTagResponseCroatiaResult::class],
    version = 254,
    exportSchema = false
)  // changes on tables require  version of database to be incremented  // also requires database data destruction or migration
@TypeConverters(Converters::class)
abstract class DRoom : RoomDatabase() {

    abstract fun loginDao(): LoginDao // modified for api response
    abstract fun pdfDao(): PdfDao
    abstract fun fcmToken(): FcmTokenDao
    abstract fun introStateDao(): IntroPageStatusDao
    abstract fun profileImageDao(): ProfileImageDao
    abstract fun myInvoicesDao(): MyInvoicesDao
    abstract fun notificationDao(): NotificationDao
    abstract fun lastUserDao(): LastUserDao // dont delete this on logout
    abstract fun refundRequestDao(): RefundRequestDao
    abstract fun csvTableDao(): CsvDao
    abstract fun bankDao(): BankDao
    abstract fun tagsRefundRequest(): TagsRefundRequestDao
    abstract fun basicInfoDao(): BasicInfoDao
    abstract fun homeScreenDao(): HomeScreenDao
    abstract fun homeCardsDao(): HomeCardsDao
    abstract fun toolHistoryDaoSerials(): ToolHistoryV2TagsSerials
    abstract fun historyV2PassageDao(): ToolHistoryV2Dao
    abstract fun historyV2PassageDaoResult(): ToolHistoryV2DaoResult
    abstract fun historyPassageDaoV2Croatia(): ToolHistoryV2DaoCroatia
    abstract fun historyPassageDaoV2CroatiaResult(): ToolHistoryV2DaoCroatiaResult
    abstract fun historyV2AllowedCountriesDao(): ToolHistoryV2AllowedCountryDao

    companion object {
        private var instance: DRoom? = null

        fun getRoomInstance(context: Context): DRoom {
            if (instance == null) {
                synchronized(DRoom::class) {
                    instance = buildDatabase(context)
                }
            }
            return instance!!
        }

        fun buildDatabase(context: Context): DRoom {
            if (instance == null) {
                synchronized(DRoom::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DRoom::class.java,
                        Config.TABLE_NAME
                    ).fallbackToDestructiveMigration(true).build()
                }
            }
            return instance!!
        }

    }

    suspend fun clearAllData() {
        loginDao().deleteAll()
        notificationDao().deleteAll()
        fcmToken().deleteTable()
        toolHistoryDaoSerials().deleteData()
        myInvoicesDao().deleteDataMonthlyInvoices()
        pdfDao().deleteData()
        refundRequestDao().deleteRefundRequests()
        tagsRefundRequest().deleteTagsRefundRequest()
        basicInfoDao().deleteBasicInfo()
        homeScreenDao().deleteHomeScreenData()
        historyPassageDaoV2Croatia().deleteData()
        historyV2PassageDao().deleteData()
        historyV2AllowedCountriesDao().clear()
        historyV2PassageDaoResult().deleteData()
        historyPassageDaoV2CroatiaResult().deleteData()
    }

}
