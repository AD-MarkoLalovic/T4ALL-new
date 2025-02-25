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
import com.mobility.enp.data.model.home.cards.added_cards.entity.AddedCardsEntity
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.data.model.home.relation.HomeWithDetails
import com.mobility.enp.data.repository.HomeRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.ui_models.home.HomeTollHistoryUI
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TAG
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

    private val _isTollHistoryEmpty = MutableStateFlow(false)
    val isTollHistoryEmpty: StateFlow<Boolean> get() = _isTollHistoryEmpty

    private val _isInvoiceEmpty = MutableStateFlow(false)
    val isInvoiceEmpty: StateFlow<Boolean> get() = _isInvoiceEmpty


    fun fetchHomeData() {
        viewModelScope.launch {

            _homeData.value = SubmitResult.Loading

            val localHomeData = repositoryHome.getLocalAllHomeData()
            localHomeData?.let { updateHomeData(it) }

            val localHomeCards = repositoryHome.getHomeCards()
            val localAddedCards = repositoryHome.getLocalAddedCards()
            _homeCards.value = filterCards(localHomeCards, localAddedCards)

            val homeDataDeferred = async { repositoryHome.getHomeDataFromServer() }
            val homeCardsDeferred = async { repositoryHome.getCardsFromServer() }
            val homeAddedCardsDeferred = async { repositoryHome.getAddedCardsFromServer() }

            val homeDataResult = homeDataDeferred.await()
            val homeCardsResult = homeCardsDeferred.await()
            val userAddedCardsResult = homeAddedCardsDeferred.await()

            if (homeDataResult.isSuccess) {
                val homeEntity = homeDataResult.getOrNull()

                if (homeEntity == null) {
                    _homeData.value = SubmitResult.Empty
                } else {
                    updateHomeData(homeEntity)
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
                                Log.d(
                                    "API_TOKEN HomeViewModel",
                                    "invalid token detected login out user"
                                )
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
                _homeCards.value = filterCards(homeCardsEntity, userAddedCards)
            }
        }
    }

    private fun updateHomeData(homeEntity: HomeWithDetails) {
        _isTollHistoryEmpty.value = homeEntity.tollHistory.isEmpty()
        _isInvoiceEmpty.value = homeEntity.invoice.isEmpty()
        _homeDetails.value = homeEntity
        _homeTollHistory.value = homeEntity.toUITollHistoryList()
        _homeData.value = SubmitResult.Success(homeEntity)
    }

    private fun filterCards(
        homeCards: List<HomeCardsEntity>?,
        addedCards: List<AddedCardsEntity>?
    ): List<HomeCardsEntity> {
        return homeCards?.filter { homeCard ->
            addedCards?.none { it.countryCode == homeCard.code } ?: true
        } ?: emptyList()
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