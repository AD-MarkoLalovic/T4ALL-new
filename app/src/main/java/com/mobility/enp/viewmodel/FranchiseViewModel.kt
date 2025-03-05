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
import com.mobility.enp.data.repository.FranchiserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FranchiseViewModel(private val repository: FranchiserRepository) : ViewModel() {

    private val _portalKey: MutableLiveData<String?> = MutableLiveData()
    val portalKey: LiveData<String?> get() = _portalKey

    suspend fun getHomeData(): HomeEntity? {
        return repository.getHomeEntity()
    }

    fun getPortalKey() {
        viewModelScope.launch {
            val portalKey = withContext(Dispatchers.IO) {
                repository.getPortalKey()
            }
            _portalKey.value = portalKey
        }
    }

    fun upsertHomeData(portalKey: String) { // for testing
        viewModelScope.launch(Dispatchers.IO) {
            val homeEntity = repository.getHomeEntity()
            homeEntity?.portalKey = portalKey
            homeEntity?.let {
                repository.upsertHomeEntity(it)
            }

            getPortalKey()
        }
    }


    companion object {

        const val TAG = "FranchiseVM"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).franchiseRepository
                FranchiseViewModel(
                    repository = myRepository
                )
            }
        }
    }

}