package com.mobility.enp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.data.model.home.relation.HomeWithDetails
import com.mobility.enp.data.repository.HomeRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.ui_models.home.HomeTollHistoryUI
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TAG
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TOKEN
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repositoryHome: HomeRepository) : ViewModel() {

    private val _homeData = MutableStateFlow<SubmitResult<HomeWithDetails>>(SubmitResult.Loading)
    val homeData: StateFlow<SubmitResult<HomeWithDetails>> get() = _homeData

    private val _homeDetails = MutableStateFlow<HomeWithDetails?>(null)
    val homeDetails: StateFlow<HomeWithDetails?> get() = _homeDetails

    private val _homeTollHistory = MutableStateFlow<List<HomeTollHistoryUI>>(emptyList())
    val homeTollHistory: StateFlow<List<HomeTollHistoryUI>> get() = _homeTollHistory

    private val _profileImage = MutableStateFlow<ProfileImage?>(null)
    val profileImage: StateFlow<ProfileImage?> get() = _profileImage

    private val _homeCards = MutableStateFlow<List<HomeCardsEntity>?>(null)
    val homeCards: StateFlow<List<HomeCardsEntity>?> get() = _homeCards


    fun fetchHomeData() {
        viewModelScope.launch {

            val localHomeData = repositoryHome.getLocalAllHomeData()
            localHomeData?.let {
                _homeData.value = SubmitResult.Success(it)
                _homeDetails.value = it
                _homeTollHistory.value = it.toUITollHistoryList()
            }

            val localHomeCards = repositoryHome.getHomeCards()
            val localAddedCards = repositoryHome.getLocalAddedCards()
            localHomeCards?.let { homeCards ->
                localAddedCards?.let { addedCards ->
                    val filteredCards = homeCards.filter { homeCards ->
                        homeCards.code !in addedCards.map { it.countryCode }
                    }
                    _homeCards.value = filteredCards
                }
            }

            val homeDataDeferred = async { repositoryHome.getHomeDataFromServer() }
            val homeCardsDeferred = async { repositoryHome.getCardsFromServer() }
            val homeAddedCardsDeferred = async { repositoryHome.getAddedCardsFromServer() }

            val homeDataResult = homeDataDeferred.await()
            val homeCardsResult = homeCardsDeferred.await()
            val userAddedCardsResult  = homeAddedCardsDeferred.await()

            if (homeDataResult.isSuccess) {
                val homeEntity = homeDataResult.getOrNull()

                if (homeEntity == null) {
                    _homeData.value = SubmitResult.Empty
                } else {
                    _homeData.value = SubmitResult.Success(homeEntity)
                    _homeDetails.value = homeEntity
                    _homeTollHistory.value = homeEntity.toUITollHistoryList()
                }
            } else {
                when (val error = homeDataResult.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "HomeViewModel",
                            "Greška tokom preuzimanja podataka za pocetnu stranu",
                            error
                        )
                        _homeData.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _homeData.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(TOKEN, "invalid token detected login out user")
                                _homeData.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _homeData.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(TAG, "HomeViewModel api error ${error.errorResponse.message}")
                            }
                        }
                    }
                }
            }

            if (homeCardsResult.isSuccess && userAddedCardsResult.isSuccess) {
                val homeCardsEntity = homeCardsResult.getOrNull()
                val userAddedCards = userAddedCardsResult.getOrNull()
                homeCardsEntity?.let { homeCards ->
                    userAddedCards?.let { addedCards ->
                        val filteredCards = homeCards.filter { homeCards ->
                            homeCards.code !in addedCards.map { it.countryCode }
                        }
                        _homeCards.value = filteredCards
                    }
                }
            }
        }
    }

    fun loadProfileImage(displayName: String) {
        viewModelScope.launch {
            val image = repositoryHome.getProfileImage(displayName)
            _profileImage.value = image
        }
    }

    fun updateDeleteHomeCard(card: HomeCardsEntity) {
        viewModelScope.launch {
            repositoryHome.updateHomeCard(card)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).repositoryHome
                HomeViewModel(
                    repositoryHome = myRepository
                )
            }
        }
    }
}