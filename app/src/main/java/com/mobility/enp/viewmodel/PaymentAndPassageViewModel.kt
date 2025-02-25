package com.mobility.enp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.cards.response.Card
import com.mobility.enp.data.model.cards.response.CardsResponse
import com.mobility.enp.data.model.cards.response.Country
import com.mobility.enp.data.model.cardsweb.CardWebModel
import com.mobility.enp.data.model.cardsweb.CardsWebUnified
import com.mobility.enp.data.repository.CardRepository
import com.mobility.enp.network.Repository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentAndPassageViewModel(
    private val repository: CardRepository
) : ViewModel() {


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).repositoryCard
                PaymentAndPassageViewModel(
                    repository = myRepository
                )
            }
        }
    }

    private val _getCardDataFlow =
        MutableStateFlow<SubmitResult<CardWebModel>>(SubmitResult.Loading)
    val getCardDataFlow: StateFlow<SubmitResult<CardWebModel>> get() = _getCardDataFlow

    private val _successfullyChangedPrimaryCard = MutableStateFlow<SubmitResult<Boolean>>(SubmitResult.Loading)
    val successfullyChangedPrimaryCard :StateFlow<SubmitResult<Boolean>> get() = _successfullyChangedPrimaryCard


    fun fetchCardFlow() {
        _getCardDataFlow.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getCardData()
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _getCardDataFlow.value = SubmitResult.Empty
                } else {
                    _getCardDataFlow.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _getCardDataFlow.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _getCardDataFlow.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        _getCardDataFlow.value =
                            SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                        Log.d(TAG, "api error ${error.errorResponse.message}")
                    }

                    else -> {}
                }
            }
        }
    }

    fun setNewPrimaryCard(billId: Int){
        _successfullyChangedPrimaryCard.value = SubmitResult.Loading
        viewModelScope.launch (Dispatchers.IO) {
            val result = repository.setNewPrimaryCard(billId)
            if (result.isSuccess){
                val data = result.getOrNull()
                if (data == null){
                    _successfullyChangedPrimaryCard.value = SubmitResult.Empty
                }else{
                    _successfullyChangedPrimaryCard.value = SubmitResult.Success(true)
                }
            }else{
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _successfullyChangedPrimaryCard.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _successfullyChangedPrimaryCard.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        _successfullyChangedPrimaryCard.value =
                            SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                        Log.d(TAG, "api error ${error.errorResponse.message}")
                    }

                    else -> {}
                }
            }
        }
    }

//
//    fun deleteCard(cardId: String, errorBody: MutableLiveData<ErrorBody>) {
//        if (isNetworkAvailable()) {
//            viewModelScope.launch {
//                val userToken = getUserToken()
//                userToken?.let { token ->
//                    try {
//                        //todo fix this
////                        Repository.deleteCard(cardId, token, getApplication(), errorBody)
////                        val updatedPaymentAndPassage =
////                            _paymentAndPassageList.value?.data?.filter { it.id.toString() != cardId }
////                        _paymentAndPassageList.postValue(CardsResponse(updatedPaymentAndPassage))
//                    } catch (e: Exception) {
//                        Log.e(
//                            "PaymentAndPassageViewModel",
//                            "Greška pri brisanju kartice: ${e.message}"
//                        )
//                    }
//                }
//            }
//        } else {
//            _checkNetCards.postValue(false)
//        }
//    }
//


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

}