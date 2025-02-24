package com.mobility.enp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.cards.response.Card
import com.mobility.enp.data.model.cards.response.CardsResponse
import com.mobility.enp.data.model.cards.response.Country
import com.mobility.enp.data.model.cardsweb.CardWebModel
import com.mobility.enp.data.model.cardsweb.CardsWebUnified
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentAndPassageViewModel(application: Application) : AndroidViewModel(application) {

    private val database: DRoom = DRoom.getRoomInstance(application)

    private val _paymentAndPassageList = MutableLiveData<CardWebModel>()
    val paymentAndPassageList: MutableLiveData<CardWebModel> get() = _paymentAndPassageList

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
                    Repository.getCreditCardsWeb(
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
                        //todo fix this
//                        Repository.deleteCard(cardId, token, getApplication(), errorBody)
//                        val updatedPaymentAndPassage =
//                            _paymentAndPassageList.value?.data?.filter { it.id.toString() != cardId }
//                        _paymentAndPassageList.postValue(CardsResponse(updatedPaymentAndPassage))
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

    fun objectTransformer(input: CardWebModel?): CardsResponse {
        val cardsRS: List<CardsWebUnified> = input?.data?.cardsRS ?: emptyList()
        val cardsME: List<CardsWebUnified> = input?.data?.cardsME ?: emptyList()
        val cardsMK: List<CardsWebUnified> = input?.data?.cardsMK ?: emptyList()

        val sortedList: ArrayList<Card> = arrayListOf()
        sortedList.addAll(transformCard(cardsRS))
        sortedList.addAll(transformCard(cardsME))
        sortedList.addAll(transformCard(cardsMK))

        val cardResponse = CardsResponse(sortedList)
        return cardResponse
    }

    private fun transformCard(cardList: List<CardsWebUnified>): List<Card> {
        val list: ArrayList<Card> = arrayListOf()
        for (card in cardList) {
            val cardObject = Card(
                card.active,
                card.details,
                card.cardType,
                Country(card.country?.code, card.country?.name, false),
                card.defaultCard,
                card.id
            )
            list.add(cardObject)
        }
        return list
    }


    suspend fun cardLimitByUserType(): List<String> {
        val list = database.promotionsDao().getPromotionsList()
        val countriesAvailable = list.mapNotNull { promotion -> promotion.countryCode }
        return countriesAvailable
    }

}