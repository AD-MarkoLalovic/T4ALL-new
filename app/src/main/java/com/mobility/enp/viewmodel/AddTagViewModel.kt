package com.mobility.enp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.repository.ProfileRepository
import com.mobility.enp.util.SubmitResultFold
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddTagViewModel(private var repository: ProfileRepository) : ViewModel() {

    private val _addTag = MutableStateFlow<SubmitResultFold<Unit>>(SubmitResultFold.Idle)
    val addTag: StateFlow<SubmitResultFold<Unit>> get() = _addTag

    fun addNewTag(serialNumber: String, verificationCode: String, montenegrin: Boolean) {
        viewModelScope.launch {
            _addTag.value = SubmitResultFold.Loading

            val result = if (montenegrin) {
                repository.addTag(serialNumber = serialNumber, verificationOrSerNumber = verificationCode, true)
            } else {
                repository.addTag(serialNumber = serialNumber, verificationOrSerNumber = verificationCode, false)
            }

            result.fold(
                onSuccess = {
                    _addTag.value = SubmitResultFold.Success(Unit)
                },
                onFailure = { error ->
                    _addTag.value = SubmitResultFold.Failure(error)
                }

            )
        }
    }

    fun resetAddTagState() {
        _addTag.value = SubmitResultFold.Idle
    }

    fun getCountryCode(): String? {
        return repository.getCountryCode()
    }


    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepo = (this[APPLICATION_KEY] as MyApplication).profileRepository
                AddTagViewModel(repository = myRepo)
            }
        }
    }
}