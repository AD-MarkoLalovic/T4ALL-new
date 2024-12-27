package com.mobility.enp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.cards.response.CardsResponse
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentAndPassageViewModel(application: Application) : AndroidViewModel(application) {

    private val database: DRoom = DRoom.getRoomInstance(application)

    private val _paymentAndPassageList = MutableLiveData<CardsResponse>()
    val paymentAndPassageList: MutableLiveData<CardsResponse> get() = _paymentAndPassageList

    private val _successfullyChangedPrimaryCard = MutableLiveData<Boolean>()
    val successfullyChangedPrimaryCard: LiveData<Boolean> get() = _successfullyChangedPrimaryCard

    private val _checkNetCards = MutableLiveData<Boolean>()
    val checkNetCards: LiveData<Boolean> get() = _checkNetCards

    private val _dataLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> get() = _dataLoading


    private suspend fun getUserToken(): String? {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers().accessToken
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return Repository.isNetworkAvailable(getApplication())
    }

    fun fetchCard(errorBody: MutableLiveData<ErrorBody>) {
        _dataLoading.value = true

        if (isNetworkAvailable()) {
            viewModelScope.launch {
                val userToken = getUserToken()
                userToken?.let { token ->
                    Repository.getCreditCards(
                        _paymentAndPassageList,
                        token,
                        errorBody,
                        getApplication()
                    )
                }
                _dataLoading.value = false
            }
        } else {
            _checkNetCards.postValue(false)
        }
    }

    fun deleteCard(cardId: String, errorBody: MutableLiveData<ErrorBody>) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                val userToken = getUserToken()
                userToken?.let { token ->
                    try {
                        Repository.deleteCard(cardId, token, getApplication(), errorBody)
                        val updatedPaymentAndPassage =
                            _paymentAndPassageList.value?.data?.filter { it.id.toString() != cardId }
                        _paymentAndPassageList.postValue(CardsResponse(updatedPaymentAndPassage))
                    } catch (e: Exception) {
                        Log.e(
                            "PaymentAndPassageViewModel",
                            "Greška pri brisanju kartice: ${e.message}"
                        )
                    }
                }
            }
        } else {
            _checkNetCards.postValue(false)
        }
    }

    fun setNewPrimaryCard(billId: Int, errorBody: MutableLiveData<ErrorBody>) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                val userToken = getUserToken()
                userToken?.let { token ->
                    Repository.setPrimaryCard(
                        token,
                        billId,
                        errorBody,
                        _successfullyChangedPrimaryCard,
                        getApplication()
                    )
                }

            }
        } else {
            _checkNetCards.postValue(false)
        }
    }

    suspend fun cardLimitByUserType(): List<String> {
        val list = database.promotionsDao().getPromotionsList()
        val countriesAvailable = list.mapNotNull { promotion -> promotion.countryCode }
        return countriesAvailable
    }

}