package com.mobility.enp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_home_page.homedata.HomeScreenData
import com.mobility.enp.data.model.api_home_page.homedata.Promotion
import com.mobility.enp.data.model.cards.response.CardsResponse
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.countries.CountriesModel
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database: DRoom = DRoom.getRoomInstance(application)
    private var _countryData = MutableLiveData<CountriesModel>()
    val countryData: LiveData<CountriesModel> get() = _countryData

    private val _promotionsList: MutableLiveData<List<Promotion>> = MutableLiveData()
    val promotionList: LiveData<List<Promotion>> get() = _promotionsList

    private val _userCreditCards: MutableLiveData<CardsResponse> = MutableLiveData()
    val userCreditCards: LiveData<CardsResponse> get() = _userCreditCards

    fun checkStoredPromotions() {
        viewModelScope.launch(Dispatchers.IO) {
            val promotions = database.promotionsDao().getPromotionsList()
            _promotionsList.postValue(promotions)
        }
    }

    fun getCreditCards(
        errorBody: MutableLiveData<ErrorBody>,
    ) {
        if (Repository.isNetworkAvailable(getApplication())) {
            viewModelScope.launch(Dispatchers.IO) {
                val userToken = getUserToken()
                userToken?.let {
                    Repository.getCreditCards(
                        _userCreditCards, it.accessToken, errorBody, getApplication()
                    )
                }
            }
        }
    }

    fun getUserHomeData(
        context: Context,
        errorBody: MutableLiveData<ErrorBody>,
        homeUserData: MutableLiveData<HomeScreenData>,
        isInternetAvailable: MutableLiveData<Boolean>
    ) {
        if (Repository.isNetworkAvailable(context)) {
            viewModelScope.launch {
                val userToken = getUserToken()
                userToken?.let {
                    Repository.getUserHomeData(
                        homeUserData, it.accessToken, context, errorBody
                    )
                }
            }
        } else {
            isInternetAvailable.postValue(false)
        }
    }

    fun getUserAllowedCountries(
        errorBody: MutableLiveData<ErrorBody>
    ) {
        viewModelScope.launch {
            val token = getUserToken()
            token.accessToken?.let { accessToken ->
                if (Repository.isNetworkAvailable(getApplication())) {
                    Repository.getUserCountries(_countryData, errorBody, accessToken)
                }
            }
        }
    }

    suspend fun getUserToken(): UserLoginResponseRoomTable {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers()
        }
    }

    fun insertHomeData(homeScreenData: HomeScreenData) {
        viewModelScope.launch {
            database.homeDao().insertData(homeScreenData)
        }
    }

    suspend fun fetchHomeData(): HomeScreenData {
        return withContext(Dispatchers.IO) {
            database.homeDao().fetchData()
        }
    }

    fun savePromotion(promotion: List<Promotion>) {
        viewModelScope.launch(Dispatchers.IO) {
            val storedPromotions = database.promotionsDao().getPromotionsList()

            val newPromotions = promotion.filter { newPromotion ->
                storedPromotions.none { storedPromo -> storedPromo.countryCode == newPromotion.countryCode }
            }

            if (newPromotions.isNotEmpty()) {
                database.promotionsDao().upsertPromotion(newPromotions)
            }
            
            checkStoredPromotions()
        }
    }

    fun createPromotion(context: Context, code: String?): Promotion? {
        return when {
            code?.contains("RS") == true -> Promotion(
                context.getString(R.string.serbian_passage),
                context.getString(R.string.tag_device_payment_method_serbia),
                0,
                "",
                "RS",
                false
            )

            code?.contains("MK") == true -> Promotion(
                context.getString(R.string.north_macedonian_passage),
                context.getString(R.string.tag_device_payment_method_north_macedonia),
                0,
                "",
                "MK",
                false
            )

            code?.contains("ME") == true -> Promotion(
                context.getString(R.string.montenegro_passage),
                context.getString(R.string.tag_device_payment_method_montenegro),
                0,
                "",
                "ME",
                false
            )

            else -> null
        }
    }

    fun userDeletedPromotion(promotion: Promotion) {
        viewModelScope.launch(Dispatchers.IO) {
            database.promotionsDao().deletedByUser(promotion)
            val newList = database.promotionsDao().getPromotionsList()
            _promotionsList.postValue(newList)
        }
    }

    fun upsertPromotion(promotion: Promotion) {
        viewModelScope.launch(Dispatchers.IO) {
            database.promotionsDao().upsertSinglePromotion(promotion)
        }
    }

    fun reloadPromotionList() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = database.promotionsDao().getPromotionsList()
            _promotionsList.postValue(list)
        }
    }

}