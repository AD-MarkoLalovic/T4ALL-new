package com.mobility.enp

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mobility.enp.data.repository.AuthRepository
import com.mobility.enp.data.repository.BillsRepository
import com.mobility.enp.data.repository.CardRepository
import com.mobility.enp.data.repository.FranchiserRepository
import com.mobility.enp.data.repository.HomeRepository
import com.mobility.enp.data.repository.PassageHistoryRepository
import com.mobility.enp.data.repository.ProfileRepository
import com.mobility.enp.data.repository.UserRepository
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2TagsSerials
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2AllowedCountryDao
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2Dao
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2DaoCroatia
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2DaoResult
import com.mobility.enp.data.room.database.DRoom

class MyApplication : Application() {

    private val database: DRoom by lazy { DRoom.getRoomInstance(this) }

    val repositoryUser: UserRepository by lazy {
        UserRepository(database, this)
    }
    val repositoryAuth: AuthRepository by lazy {
        AuthRepository(database, this)
    }

    val repositoryCard: CardRepository by lazy {
        CardRepository(database, this)
    }

    val repositoryHome: HomeRepository by lazy {
        HomeRepository(database, this)
    }

    val passageHistoryRepository: PassageHistoryRepository by lazy {
        PassageHistoryRepository(database, this)
    }

    val v2TagsDao: ToolHistoryV2TagsSerials by lazy {
        database.toolHistoryDaoSerials()
    }
    val v2HistoryDao: ToolHistoryV2Dao by lazy {
        database.historyV2PassageDao()
    }

    val v2HistoryDaoResult: ToolHistoryV2DaoResult by lazy {
        database.historyV2PassageDaoResult()
    }

    val v2CroatiaDao: ToolHistoryV2DaoCroatia by lazy {
        database.historyPassageDaoV2Croatia()
    }

    val v2AllowedCountriesDao: ToolHistoryV2AllowedCountryDao by lazy {
        database.historyV2AllowedCountriesDao()
    }

    val franchiseRepository: FranchiserRepository by lazy {
        FranchiserRepository(database, this)
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(database, this)
    }

    val billsRepository: BillsRepository by lazy {
        BillsRepository(database, this)
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled =
            !BuildConfig.DEBUG

    }
}

