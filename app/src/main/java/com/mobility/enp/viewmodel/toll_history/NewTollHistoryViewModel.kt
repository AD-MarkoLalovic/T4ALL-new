package com.mobility.enp.viewmodel.toll_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.new_toll_history.mapper.toUi
import com.mobility.enp.data.repository.NewTollHistoryRepository
import com.mobility.enp.view.ui_models.toll_history.AllowedCountryUi
import com.mobility.enp.view.ui_models.toll_history.SumTagUi
import com.mobility.enp.view.ui_models.toll_history.TollHistoryListItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class NewTollHistoryViewModel(private val repo: NewTollHistoryRepository) : ViewModel() {

    data class HistoryFilter(
        val country: String = "",
        val dateFrom: String = "",
        val dateTo: String = ""
    )

    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)

    private val _filter = MutableStateFlow(HistoryFilter())
    val currentFilter: StateFlow<HistoryFilter> = _filter

    private val _logoutEvent = Channel<Int>(Channel.BUFFERED)
    val logoutEvent: Flow<Int> = _logoutEvent.receiveAsFlow()

    val pagingFlow: Flow<PagingData<TollHistoryListItem>> = _filter
        .flatMapLatest { filter ->
            repo.getPagedHistory(
                filterCountry = filter.country,
                dateFrom = filter.dateFrom,
                dateTo = filter.dateTo,
                onUnauthorized = { httpCode ->
                    _logoutEvent.trySend(httpCode)
                }
            )
        }
        .map { pagingData ->
            pagingData
                .map { entity ->
                    TollHistoryListItem.PassageItem(entity.toUi()) as TollHistoryListItem
                }
                .insertSeparators { before, after ->
                    val afterPassage =
                        (after as? TollHistoryListItem.PassageItem)?.passage
                            ?: return@insertSeparators null
                    val beforePassage = (before as? TollHistoryListItem.PassageItem)?.passage

                    if (beforePassage == null || beforePassage.tagsSerialNumber != afterPassage.tagsSerialNumber) {
                        TollHistoryListItem.TagHeader(
                            tagSerialNumber = afterPassage.tagsSerialNumber,
                            total = afterPassage.tagTotal,
                            currency = afterPassage.tagCurrency
                        )
                    } else {
                        null
                    }
                }
                .insertSeparators { before, after ->
                    (before as? TollHistoryListItem.PassageItem)?.passage
                        ?: return@insertSeparators null

                    if (after == null || after is TollHistoryListItem.TagHeader) {
                        TollHistoryListItem.GroupEnd
                    } else {
                        null
                    }
                }
        }
        .cachedIn(viewModelScope)

    val sumTags: StateFlow<List<SumTagUi>> = repo.observeSumTags().map { entities ->
        entities.map { it.toUi() }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allowedCountriesUi: StateFlow<List<AllowedCountryUi>> = combine(
        repo.observeAllowedCountries(),
        _filter
    ) { countries, filter ->
        countries.sortedBy { it.position }
            .map { entity -> entity.toUi(isSelected = entity.value == filter.country) }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun initialize(country: String) {
        val now = LocalDate.now()
        val thirtyDaysAgo = now.minusDays(30)
        _filter.value = HistoryFilter(
            country = country,
            dateFrom = thirtyDaysAgo.format(dateFormatter),
            dateTo = now.format(dateFormatter)
        )
    }

    fun onCountrySelected(countryCode: String) {
        _filter.update { it.copy(country = countryCode) }
    }

    fun onDateRangeChanged(dateFrom: String, dateTo: String) {
        _filter.update { it.copy(dateFrom = dateFrom, dateTo = dateTo) }
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                NewTollHistoryViewModel(app.newTollHistoryRepository)
            }
        }
    }
}