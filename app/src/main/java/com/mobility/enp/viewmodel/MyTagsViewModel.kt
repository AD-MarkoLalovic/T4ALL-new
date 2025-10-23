package com.mobility.enp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.api_tags.ActivateDeactivateTagModel
import com.mobility.enp.data.repository.ProfileRepository
import com.mobility.enp.util.SubmitResultFold
import com.mobility.enp.view.ui_models.my_tags.TagUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ReportType {
    LOST, FOUND, DEACTIVATED, ACTIVATED
}

class MyTagsViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _initialData =
        MutableStateFlow<SubmitResultMyTags<List<TagUiModel>>>(SubmitResultMyTags.Idle)
    val initialData: StateFlow<SubmitResultMyTags<List<TagUiModel>>> get() = _initialData

    private val _myTags =
        MutableStateFlow<SubmitResultMyTags<List<TagUiModel>>>(SubmitResultMyTags.Idle)
    val myTags: StateFlow<SubmitResultMyTags<List<TagUiModel>>> get() = _myTags

    private val _deactivateActivateTag =
        MutableStateFlow<SubmitResultFold<Unit>>(SubmitResultFold.Idle)
    val deactivateActivateTag: StateFlow<SubmitResultFold<Unit>> get() = _deactivateActivateTag

    private val _reportTag = MutableStateFlow<SubmitResultFold<Unit>>(SubmitResultFold.Idle)
    val reportTag: StateFlow<SubmitResultFold<Unit>> get() = _reportTag

    var allTags: List<TagUiModel> = emptyList()
    private var selectedStatus: String = ""
    private var selectedCountry: String = "RS"
    private var allStatusLabel: String = ""
    private var currentCountryForApi = ""
    private val tagsPerApiRequest: Int = 20

    fun reset() {
        selectedCountry = "RS"
        allStatusLabel = ""
        selectedStatus = ""
        allTags = listOf()
    }

    fun setCurrentApiCountry(country: String) {
        this.currentCountryForApi = country
    }

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

    /**
     * fetches inital data such as available countries because those require no filter
     * and sets first available country as default for fetch data fill that sets adapters
     * for example RS if serbian user
     */
    fun fetchInitialData() {
        viewModelScope.launch {
            _initialData.value = SubmitResultMyTags.Loading

            val result = repository.getMyTags("")
            result.fold(
                onSuccess = { tags ->
                    allTags = tags
                    _initialData.value = SubmitResultMyTags.Success(tags)
                },
                onFailure = { error ->
                    _initialData.value = SubmitResultMyTags.Failure(error)
                }
            )
        }
    }

    fun internetChecked(): Boolean {
        return repository.isNetworkAvail()
    }

    fun fetchDataByCountry() {
        currentCountryForApi = selectedCountry

        viewModelScope.launch {
            _myTags.value = SubmitResultMyTags.Loading
            val result = repository.getAllMyTagsByCountry(currentCountryForApi, tagsPerApiRequest)

            result.fold(
                onSuccess = { allItems ->
                    allTags = allItems
                    _myTags.value = SubmitResultMyTags.Success(allItems)
                },
                onFailure = { error ->
                    _myTags.value = SubmitResultMyTags.Failure(error)
                }
            )
        }
    }

    fun deactivateTagByCountry(body: ActivateDeactivateTagModel) {
        viewModelScope.launch {
            _deactivateActivateTag.value = SubmitResultFold.Loading
            val result = repository.deactivateTag(body)
            result.fold(
                onSuccess = {
                    _deactivateActivateTag.value =
                        SubmitResultFold.Success(Unit, ReportType.DEACTIVATED)
                },
                onFailure = { error ->
                    _deactivateActivateTag.value = SubmitResultFold.Failure(error)
                }
            )
        }
    }

    fun activateTagByCountry(body: ActivateDeactivateTagModel) {
        viewModelScope.launch {
            _deactivateActivateTag.value = SubmitResultFold.Loading
            val result = repository.activateTag(body)
            result.fold(
                onSuccess = {
                    _deactivateActivateTag.value =
                        SubmitResultFold.Success(Unit, ReportType.ACTIVATED)
                },
                onFailure = { error ->
                    _deactivateActivateTag.value = SubmitResultFold.Failure(error)
                }
            )
        }
    }

    fun reportLostTag(serialNumber: String) {
        viewModelScope.launch {
            _reportTag.value = SubmitResultFold.Loading

            val result = repository.reportLostTag(serialNumber)
            result.fold(
                onSuccess = {
                    _reportTag.value = SubmitResultFold.Success(Unit, ReportType.LOST)
                },
                onFailure = { error ->
                    _reportTag.value = SubmitResultFold.Failure(error)
                }
            )
        }
    }

    fun reportFoundTag(serialNumber: String) {
        viewModelScope.launch {
            _reportTag.value = SubmitResultFold.Loading

            val result = repository.reportFoundTag(serialNumber)
            result.fold(
                onSuccess = {
                    _reportTag.value = SubmitResultFold.Success(Unit, ReportType.FOUND)
                },
                onFailure = { error ->
                    _reportTag.value = SubmitResultFold.Failure(error)
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