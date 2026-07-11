package com.mobility.enp.viewmodel.toll_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.R
import com.mobility.enp.data.model.new_toll_history.mapper.defaultFilterDates
import com.mobility.enp.data.model.new_toll_history.mapper.toFilterTagUiList
import com.mobility.enp.data.model.new_toll_history.mapper.toUi
import com.mobility.enp.data.model.new_toll_history.tags.remote.Tag
import com.mobility.enp.data.repository.NewTollHistoryRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.view.ui_models.toll_history.ExportType
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterDateUi
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterEvent
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterExportState
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterScreenUiState
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterUi
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterValidation
import com.mobility.enp.view.ui_models.toll_history.TollHistoryTagsLoadState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TollHistoryFilterViewModel(
    private val repo: NewTollHistoryRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(TollHistoryFilterUi())
    private val _tagsLoadState =
        MutableStateFlow<TollHistoryTagsLoadState>(TollHistoryTagsLoadState.Loading)
    private val _exportState =
        MutableStateFlow<TollHistoryFilterExportState>(TollHistoryFilterExportState.Idle)
    private val _events = Channel<TollHistoryFilterEvent>(Channel.BUFFERED)

    private var loadedTags: List<Tag> = emptyList()

    val exportState: StateFlow<TollHistoryFilterExportState> = _exportState.asStateFlow()
    val events = _events.receiveAsFlow()

    val screenState: StateFlow<TollHistoryFilterScreenUiState> = combine(
        _tagsLoadState,
        _filter,
        repo.observeAllowedCountries()
    ) { tagsState, filter, countries ->
        val countriesUi = countries
            .sortedBy { it.position }
            .map { entity -> entity.toUi(isSelected = entity.value == filter.countryCode) }

        TollHistoryFilterScreenUiState(
            tagsLoadState = tagsState,
            countries = countriesUi,
            filter = filter,
            isSearchEnabled = tagsState !is TollHistoryTagsLoadState.Empty,
            showAllTagsChecked = filter.allTagsSelected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TollHistoryFilterScreenUiState()
    )

    init {
        initializeDefaultDates()
        loadTags()
        observeDefaultCountry()
    }

    fun loadTags() {
        viewModelScope.launch {
            _tagsLoadState.value = TollHistoryTagsLoadState.Loading
            repo.fetchFilterTags()
                .onSuccess { tags ->
                    loadedTags = tags
                    rebuildTagsState()
                }
                .onFailure { error ->
                    handleTagsLoadFailure(error)
                }
        }
    }

    fun onTagToggled(serialNumber: String, isChecked: Boolean) {
        _filter.update { current ->
            val updatedSerials = if (isChecked) {
                current.selectedTagSerials + serialNumber
            } else {
                current.selectedTagSerials - serialNumber
            }
            current.copy(
                selectedTagSerials = updatedSerials,
                allTagsSelected = false
            )
        }
        rebuildTagsState()
    }

    fun onShowAllTagsChanged(isChecked: Boolean) {
        _filter.update { current ->
            if (isChecked) {
                current.copy(
                    allTagsSelected = true,
                    selectedTagSerials = loadedTags.map { it.serialNumber }.toSet()
                )
            } else {
                current.copy(
                    allTagsSelected = false,
                    selectedTagSerials = emptySet()
                )
            }
        }
        rebuildTagsState()
    }

    fun onCountrySelected(countryCode: String) {
        _filter.update { it.copy(countryCode = countryCode) }
    }

    fun onDateFromSelected(date: TollHistoryFilterDateUi) {
        _filter.update { it.copy(dateFrom = date) }
    }

    fun onDateToSelected(date: TollHistoryFilterDateUi) {
        _filter.update { it.copy(dateTo = date) }
    }

    fun onSearchClicked() {
        when (val validation = validateFilter()) {
            TollHistoryFilterValidation.Valid -> {
                _events.trySend(TollHistoryFilterEvent.NavigateToResults(_filter.value))
            }

            is TollHistoryFilterValidation.Invalid -> {
                _events.trySend(
                    TollHistoryFilterEvent.ShowToast(validationMessageRes(validation))
                )
            }
        }
    }

    fun onExportMenuClicked() {
        when (val validation = validateFilter()) {
            TollHistoryFilterValidation.Valid -> {
                _events.trySend(TollHistoryFilterEvent.ShowExportMenu)
            }

            is TollHistoryFilterValidation.Invalid -> {
                _events.trySend(
                    TollHistoryFilterEvent.ShowToast(validationMessageRes(validation))
                )
            }
        }
    }

    fun onExportTypeSelected(type: ExportType) {
        _events.trySend(TollHistoryFilterEvent.RequestNotificationPermission(type))
    }

    fun resetExportState() {
        _exportState.value = TollHistoryFilterExportState.Idle
    }

    fun validateFilter(): TollHistoryFilterValidation {
        if (_tagsLoadState.value is TollHistoryTagsLoadState.Empty) {
            return TollHistoryFilterValidation.Invalid.NoPassageData
        }

        val filter = _filter.value
        if (!filter.hasSelectedTags) {
            return TollHistoryFilterValidation.Invalid.NoTagsSelected
        }
        if (!filter.isCountrySelected) {
            return TollHistoryFilterValidation.Invalid.NoCountrySelected
        }
        if (!filter.isDateRangeComplete) {
            return TollHistoryFilterValidation.Invalid.NoDateRange
        }
        return TollHistoryFilterValidation.Valid
    }

    private fun initializeDefaultDates() {
        val (dateFrom, dateTo) = defaultFilterDates()
        _filter.update { it.copy(dateFrom = dateFrom, dateTo = dateTo) }
    }

    private fun observeDefaultCountry() {
        viewModelScope.launch {
            repo.observeAllowedCountries()
                .filter { it.isNotEmpty() }
                .first()
                .minByOrNull { it.position }
                ?.value
                ?.let { firstCode ->
                    _filter.update { current ->
                        if (current.countryCode.isNotEmpty()) current
                        else current.copy(countryCode = firstCode)
                    }
                }
        }
    }

    private fun rebuildTagsState() {
        _tagsLoadState.value = if (loadedTags.isEmpty()) {
            TollHistoryTagsLoadState.Empty
        } else {
            val filter = _filter.value
            TollHistoryTagsLoadState.Success(
                tags = loadedTags.toFilterTagUiList(
                    selectedSerials = filter.selectedTagSerials,
                    allSelected = filter.allTagsSelected
                )
            )
        }
    }

    private fun handleTagsLoadFailure(throwable: Throwable?) {
        when (throwable) {
            is NetworkError.NoConnection -> {
                _tagsLoadState.value = TollHistoryTagsLoadState.Error(
                    message = "",
                    isNoConnection = true
                )
                _events.trySend(
                    TollHistoryFilterEvent.ShowNoInternetDialog(
                        titleRes = R.string.no_connection_title,
                        subtitleRes = R.string.please_connect_to_the_internet
                    )
                )
            }

            is NetworkError.ApiError -> {
                if (throwable.errorResponse.code == 401 || throwable.errorResponse.code == 405) {
                    _events.trySend(TollHistoryFilterEvent.LogoutOnInvalidToken)
                } else {
                    _tagsLoadState.value = TollHistoryTagsLoadState.Error(
                        message = throwable.errorResponse.message.orEmpty()
                    )
                }
            }

            else -> {
                _tagsLoadState.value = TollHistoryTagsLoadState.Error(
                    message = throwable?.message.orEmpty()
                )
            }
        }
    }

    private fun validationMessageRes(validation: TollHistoryFilterValidation.Invalid): Int {
        return when (validation) {
            TollHistoryFilterValidation.Invalid.NoTagsSelected ->
                R.string.please_select_tag

            TollHistoryFilterValidation.Invalid.NoCountrySelected ->
                R.string.please_select_country

            TollHistoryFilterValidation.Invalid.NoDateRange ->
                R.string.please_select_dates

            TollHistoryFilterValidation.Invalid.NoPassageData ->
                R.string.no_passage_data
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                TollHistoryFilterViewModel(app.newTollHistoryRepository)
            }
        }
    }
}
