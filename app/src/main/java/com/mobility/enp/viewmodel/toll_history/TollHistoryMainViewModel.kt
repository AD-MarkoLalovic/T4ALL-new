package com.mobility.enp.viewmodel.toll_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.new_toll_history.local.entity.TollHistoryItemEntity
import com.mobility.enp.data.model.new_toll_history.mapper.toUi
import com.mobility.enp.data.repository.NewTollHistoryRepository
import com.mobility.enp.view.ui_models.toll_history.AllowedCountryUi
import com.mobility.enp.view.ui_models.toll_history.TollHistoryListItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class TollHistoryMainViewModel(private val repo: NewTollHistoryRepository) : ViewModel() {

    data class HistoryFilter(
        val country: String = "",
        val dateFrom: String = "",
        val dateTo: String = ""
    )

    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
    private val _filter = MutableStateFlow(HistoryFilter())

    private val _logoutEvent = Channel<Int>(Channel.BUFFERED)
    val logoutEvent: Flow<Int> = _logoutEvent.receiveAsFlow()

    val pagingFlow: Flow<PagingData<TollHistoryListItem>> = _filter
        .filter { it.country.isNotEmpty() }
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
                    when (entity.rowType) {
                        TollHistoryItemEntity.ROW_TYPE_HEADER -> TollHistoryListItem.TagHeader(
                            tagSerialNumber = entity.tagsSerialNumber,
                            total = entity.tagTotal,
                            currency = entity.tagCurrency
                        )

                        TollHistoryItemEntity.ROW_TYPE_GROUP_END -> TollHistoryListItem.GroupEnd(
                            currency = entity.tagCurrency,
                            afterTagSerialNumber = entity.tagsSerialNumber
                        )

                        else -> TollHistoryListItem.PassageItem(entity.toUi())
                    }
                }
        }
        .cachedIn(viewModelScope)

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

    val customerCountry: StateFlow<String?> = repo.customerCountry

    init {
        initializeFilter()
        observeDefaultCountry()
    }

    private fun initializeFilter() {
        val now = LocalDate.now()
        val twoHundredDaysAgo = now.minusDays(30)

        _filter.value = HistoryFilter(
            dateFrom = twoHundredDaysAgo.format(dateFormatter),
            dateTo = now.format(dateFormatter)
        )
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
                        if (current.country.isNotEmpty()) current
                        else current.copy(country = firstCode)
                    }
                }
        }
    }

    fun onCountrySelected(countryCode: String) {
        _filter.update { it.copy(country = countryCode) }
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                TollHistoryMainViewModel(app.newTollHistoryRepository)
            }
        }
    }
}