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
import com.mobility.enp.data.repository.FranchiserRepository
import com.mobility.enp.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FranchiseViewModel(private val repository: FranchiserRepository) : ViewModel() {

    private val _franchiseModel: MutableLiveData<FranchiseModel?> = MutableLiveData()
    val franchiseModel: LiveData<FranchiseModel?> get() = _franchiseModel

    private val _openSuccessDialog: MutableLiveData<Boolean?> = MutableLiveData()
    val openSuccessDialog: LiveData<Boolean?> get() = _openSuccessDialog


    fun postOpenDialog(bool: Boolean?) {
        _openSuccessDialog.value = bool
    }

    private var isDialogShown = false

    fun getDialogStatus(): Boolean {
        return isDialogShown
    }

    fun setDialogStatus(status: Boolean) {
        isDialogShown = status
    }

    fun deleteData() {
        _franchiseModel.value = null
    }


    fun getFranchiseModel(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPortalKey()?.let {
                val franchiseModel = Util.franchiseID(it, context)
                withContext(Dispatchers.Main) {
                    _franchiseModel.value = franchiseModel
                }
            }
        }
    }

    fun getFranchiseModelFromLogin(portalKey: String, context: Context) {
        val franchiseModel = Util.franchiseID(portalKey, context)
        _franchiseModel.value = franchiseModel
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