package com.mobility.enp.viewmodel

import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.home.relation.HomeWithDetails
import com.mobility.enp.data.repository.HomeRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.ui_models.home.HomeTollHistoryUI
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TAG
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TOKEN
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(private val repositoryHome: HomeRepository) : ViewModel() {

    private val _homeData = MutableStateFlow<SubmitResult<HomeWithDetails>>(SubmitResult.Loading)
    val homeData: StateFlow<SubmitResult<HomeWithDetails>> get() = _homeData

    private val _homeDetails = MutableStateFlow<HomeWithDetails?>(null)
    val homeDetails: StateFlow<HomeWithDetails?> get() = _homeDetails

    private val _homeTollHistory = MutableStateFlow<List<HomeTollHistoryUI>>(emptyList())
    val homeTollHistory: StateFlow<List<HomeTollHistoryUI>> get() = _homeTollHistory

    private val _profileImage = MutableStateFlow<ProfileImage?>(null)
    val profileImage: StateFlow<ProfileImage?> get() = _profileImage


    fun fetchHomeData() {
        viewModelScope.launch {

            val localHomeData = repositoryHome.getLocalAllHomeData()
            localHomeData?.let {
                _homeData.value = SubmitResult.Success(it)
                _homeDetails.value = it
                _homeTollHistory.value = it.toUITollHistoryList()

            }

            val result = repositoryHome.getHomeDataFromServer()
            if (result.isSuccess) {
                val homeEntity = result.getOrNull()

                if (homeEntity == null) {
                    _homeData.value = SubmitResult.Empty
                } else {
                    _homeData.value = SubmitResult.Success(homeEntity)
                    _homeDetails.value = homeEntity
                    _homeTollHistory.value = homeEntity.toUITollHistoryList()
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

    fun loadProfileImage(displayName: String) {
        viewModelScope.launch {
            val image = repositoryHome.getProfileImage(displayName)
            _profileImage.value = image
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