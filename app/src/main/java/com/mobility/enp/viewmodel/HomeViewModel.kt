package com.mobility.enp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.gson.Gson
import com.mobility.enp.BuildConfig
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.TagOrderInputs
import com.mobility.enp.data.model.home.HomeCardsWithCountry
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.data.model.home.relation.HomeWithDetails
import com.mobility.enp.data.repository.HomeRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.SubmitResultFold
import com.mobility.enp.view.ui_models.home.HomeTollHistoryUI
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TAG
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl

class HomeViewModel(private val repositoryHome: HomeRepository) : ViewModel() {

    private val _homeData = MutableStateFlow<SubmitResult<HomeWithDetails>>(SubmitResult.Loading)
    val homeData: StateFlow<SubmitResult<HomeWithDetails>> get() = _homeData

    private val _homeDetails = MutableStateFlow<HomeWithDetails?>(null)
    val homeDetails: StateFlow<HomeWithDetails?> get() = _homeDetails

    private val _homeTollHistory = MutableStateFlow<List<HomeTollHistoryUI>>(emptyList())
    val homeTollHistory: StateFlow<List<HomeTollHistoryUI>> get() = _homeTollHistory

    private val _profileImage = MutableStateFlow<ProfileImage?>(null)
    val profileImage: StateFlow<ProfileImage?> get() = _profileImage

    private val _homeCards = MutableStateFlow<HomeCardsWithCountry?>(null)
    val homeCards: StateFlow<HomeCardsWithCountry?> get() = _homeCards

    private val _tagOrderUrl = MutableStateFlow<SubmitResultFold<String>>(SubmitResultFold.Idle)
    val tagOrderUrl = _tagOrderUrl.asStateFlow()

    init {
        startObservingHomeCardsForCurrentUser()
    }

    fun loadTagOrderUrl() {
        viewModelScope.launch {
            _tagOrderUrl.value = SubmitResultFold.Loading

            val user = repositoryHome.getOrFetchUserInfo()
            if (user == null) {
                val url = BuildConfig.TAG_ORDER_BASE_URL
                _tagOrderUrl.value = SubmitResultFold.Success(url)
                return@launch
            }

            val isCompany = user.customerType == 2

            val inputs = TagOrderInputs(
                customerType = user.customerType,
                city = user.city,
                postalCode = user.postalCode,
                email = user.email,
                phone = user.phone,
                firstName = user.firstName.takeIf { !isCompany },
                lastName = user.lastName.takeIf { !isCompany },
                companyName = user.companyName.takeIf { isCompany },
                mb = user.mb.takeIf { isCompany },
                pib = user.pib.takeIf { isCompany }
            )

            val json = Gson().toJson(inputs)
            val url = BuildConfig.TAG_ORDER_BASE_URL
                .toHttpUrl()
                .newBuilder()
                .addQueryParameter("inputs", json)
                .build()

            _tagOrderUrl.value = SubmitResultFold.Success(url.toString())
        }
    }

    fun clearTagOrderUrl() {
        _tagOrderUrl.value = SubmitResultFold.Idle
    }

    fun fetchHomeData() {
        viewModelScope.launch {

            _homeData.value = SubmitResult.Loading

            val localHomeData = repositoryHome.getLocalAllHomeData()
            localHomeData?.let { updateHomeData(it) }


            val homeDataDeferred = async { repositoryHome.getHomeDataFromServer() }
            val homeCardsDeferred = async { repositoryHome.getCardsFromServer() }

            val homeDataResult = homeDataDeferred.await()
            val homeCardsResult = homeCardsDeferred.await()

            if (homeDataResult.isSuccess) {
                val homeEntity = homeDataResult.getOrNull()
                homeEntity?.let { updateHomeData(it) }
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

            if (homeCardsResult.isFailure) {
                when (homeCardsResult.exceptionOrNull()) {

                    is NetworkError.ServerError,
                    is NetworkError.ApiError -> {
                        Log.e(
                            "HomeViewModel",
                            "Greška pri preuzimanju kartica sa servera",
                            homeCardsResult.exceptionOrNull()
                        )
                    }
                }
            }
        }
    }

    private fun updateHomeData(homeEntity: HomeWithDetails) {
        _homeDetails.value = homeEntity
        _homeTollHistory.value = homeEntity.toUITollHistoryList()
        _homeData.value = SubmitResult.Success(homeEntity)
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

    private fun startObservingHomeCardsForCurrentUser() {
        viewModelScope.launch {
            val user = repositoryHome.getUserForPromotion()
            if (user.isBlank()) return@launch

            repositoryHome.observerHomeCard(user).collect { homeCards ->
                val currentDetails = _homeDetails.value
                _homeCards.value = HomeCardsWithCountry(
                    card = homeCards,
                    countryCode = currentDetails?.home?.countryCode,
                    isFranchiser = currentDetails?.home?.isFranchiser
                )
            }
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