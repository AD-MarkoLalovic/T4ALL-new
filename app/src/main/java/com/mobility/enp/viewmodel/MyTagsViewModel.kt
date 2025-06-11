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
        MutableStateFlow<SubmitResultFold<List<TagUiModel>>>(SubmitResultFold.Idle)
    val myTags: StateFlow<SubmitResultFold<List<TagUiModel>>> get() = _myTags

    var allTags: List<TagUiModel> = emptyList()

    fun fetchMyTags() {
        viewModelScope.launch {
            _myTags.value = SubmitResultFold.Loading

            val result = repository.getMyTags()
            result.fold(
                onSuccess = { tags ->
                    allTags = tags
                    _myTags.value = SubmitResultFold.Success(tags)
                },
                onFailure = { error ->
                    _myTags.value = SubmitResultFold.Failure(error)

                }
            )
        }
    }

    fun filterTagsByStatus(statusKey: String) {
        val filtered = allTags.filter { tag ->
            tag.statuses.any { status ->
                status.statusText == statusKey
            }
        }

        _myTags.value = if (filtered.isEmpty()) {
            SubmitResultFold.Success(allTags)
        } else {
            SubmitResultFold.Success(filtered)
        }
    }

    fun internetChecked(): Boolean {
        return repository.isNetworkAvail()
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