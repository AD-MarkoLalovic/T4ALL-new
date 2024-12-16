package com.mobility.enp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.repository.PassageHistoryRepository

class UserPassViewModel(private val repository: PassageHistoryRepository) : ViewModel() {




    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).passageHistoryRepository
                UserPassViewModel(
                    repository = myRepository
                )
            }
        }
    }

}