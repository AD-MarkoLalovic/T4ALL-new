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
import com.mobility.enp.view.ui_models.my_tags.TagUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyTagsViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _myTags =
        MutableStateFlow<SubmitResultMyTags<List<TagUiModel>>>(SubmitResultMyTags.Idle)
    val myTags: StateFlow<SubmitResultMyTags<List<TagUiModel>>> get() = _myTags

    private val _reportLostTag = MutableStateFlow<SubmitResultFold<Unit>>(SubmitResultFold.Idle)
    val reportLostTag: StateFlow<SubmitResultFold<Unit>> get() = _reportLostTag

    var allTags: List<TagUiModel> = emptyList()
    private var selectedStatus: String = ""
    private var selectedCountry: String = "RS"
    private var allStatusLabel: String = ""

    fun setStatusFilter(statusKey: String) {
        selectedStatus = statusKey
        applyCombinedFilter()
    }

    fun setCountryFilter(countryCode: String) {
        when (countryCode) {
            "SRB" -> {
                selectedCountry = "RS"
                applyCombinedFilter()
            }

            "MKD" -> {
                selectedCountry = "MK"
                applyCombinedFilter()
            }

            "MNE" -> {
                selectedCountry = "ME"
                applyCombinedFilter()
            }

            "HRV" -> {
                selectedCountry = "HR"
                applyCombinedFilter()
            }
        }
    }

    fun setAllStatusLabel(label: String) {
        allStatusLabel = label
        selectedStatus = label
    }

    private fun applyCombinedFilter() {
        val filtered = allTags.filter { tag ->
            val statusForCountry = tag.statuses.firstOrNull {
                it.statusesCountry == selectedCountry
            }

            val matchesCountry = statusForCountry != null

            val matchesStatus =
                selectedStatus == allStatusLabel || statusForCountry?.statusText == selectedStatus

            matchesCountry && matchesStatus
        }
        _myTags.value = SubmitResultMyTags.Filtered(filtered)
    }

    fun fetchMyTags() {
        viewModelScope.launch {
            _myTags.value = SubmitResultMyTags.Loading

            val result = repository.getMyTags()
            result.fold(
                onSuccess = { tags ->
                    allTags = tags
                    _myTags.value = SubmitResultMyTags.Success(tags)
                },
                onFailure = { error ->
                    _myTags.value = SubmitResultMyTags.Failure(error)

                }
            )
        }
    }

    fun internetChecked(): Boolean {
        return repository.isNetworkAvail()
    }

    fun reportLostTag(serialNumber: String) {
        viewModelScope.launch {
            _reportLostTag.value = SubmitResultFold.Loading

            val result = repository.reportLostTag(serialNumber)
            result.fold(
                onSuccess = {
                    _reportLostTag.value = SubmitResultFold.Success(Unit)
                },
                onFailure = { error ->
                    _reportLostTag.value = SubmitResultFold.Failure(error)
                }

            )
        }
    }

    sealed class SubmitResultMyTags<out T> {
        data class Success<T>(val data: T) : SubmitResultMyTags<T>()
        data class Filtered<T>(val data: T) : SubmitResultMyTags<T>()
        object Loading : SubmitResultMyTags<Nothing>()
        data class Failure(val error: Throwable) : SubmitResultMyTags<Nothing>()
        object Idle : SubmitResultMyTags<Nothing>()
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepo = (this[APPLICATION_KEY] as MyApplication).profileRepository
                MyTagsViewModel(
                    repository = myRepo
                )
            }
        }
    }


}