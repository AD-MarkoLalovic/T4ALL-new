package com.mobility.enp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.franchise.FranchiseModel
import com.mobility.enp.data.model.home.entity.HomeEntity
import com.mobility.enp.data.repository.FranchiserRepository
import com.mobility.enp.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FranchiseViewModel(private val repository: FranchiserRepository) : ViewModel() {

    private val _franchiseModel: MutableLiveData<FranchiseModel?> = MutableLiveData()
    val franchiseModel: LiveData<FranchiseModel?> get() = _franchiseModel

    suspend fun deleteData(){
        _franchiseModel.value = null
    }

    suspend fun getHomeData(): HomeEntity? {
        return repository.getHomeEntity()
    }

    fun getFranchiseModel(context: Context) {
        viewModelScope.launch {
            val portalKey = withContext(Dispatchers.IO) {
                repository.getPortalKey()
            }

            portalKey?.let {
                val franchiseModel = Util.franchiseID(it, context)
                _franchiseModel.value = franchiseModel
            }
        }
    }

    fun upsertHomeData(portalKey: String, context: Context) { // for testing
        viewModelScope.launch(Dispatchers.IO) {
            val homeEntity = repository.getHomeEntity()
            homeEntity?.portalKey = portalKey
            homeEntity?.let {
                repository.upsertHomeEntity(it)
            }

            getFranchiseModel(context)
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