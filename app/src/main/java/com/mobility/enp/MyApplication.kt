package com.mobility.enp

import android.app.Application
import com.mobility.enp.data.repository.AuthRepository
import com.mobility.enp.data.repository.CardRepository
import com.mobility.enp.data.repository.FranchiserRepository
import com.mobility.enp.data.repository.HomeRepository
import com.mobility.enp.data.repository.PassageHistoryRepository
import com.mobility.enp.data.repository.UserRepository
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

    val franchiseRepository: FranchiserRepository by lazy {
        FranchiserRepository(database, this)
    }

}

