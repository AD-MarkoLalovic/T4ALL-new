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
import com.mobility.enp.data.model.home.entity.HomeEntity
import com.mobility.enp.data.model.home.relation.HomeWithDetails
import com.mobility.enp.data.repository.HomeRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TAG
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TOKEN
import kotlinx.coroutines.launch

class HomeViewModel(private val repositoryHome: HomeRepository) : ViewModel() {

    private val _homeData = MutableLiveData<SubmitResult<HomeWithDetails>>().apply {
        value = SubmitResult.Loading
    }
    val homeData: LiveData<SubmitResult<HomeWithDetails>> get() = _homeData

    private val _homeDetails = MutableLiveData<HomeWithDetails?>()
    val homeDetails: LiveData<HomeWithDetails?> get() = _homeDetails


    fun fetchHomeData() {
        viewModelScope.launch {

            val localHomeData = repositoryHome.getLocalAllHomeData()
            localHomeData?.let {
                _homeDetails.value = it
                _homeData.value = SubmitResult.Success(it)
            }

            val result = repositoryHome.getHomeDataFromServer()
            if (result.isSuccess) {
                val homeEntity = result.getOrNull()

                if (homeEntity == null) {
                    _homeData.value = SubmitResult.Empty
                } else {
                    _homeDetails.value = homeEntity
                    _homeData.value = SubmitResult.Success(homeEntity)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
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
                                        error.errorResponse.code ?: 0,
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