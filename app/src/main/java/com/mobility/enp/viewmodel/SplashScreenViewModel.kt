package com.mobility.enp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mobility.enp.data.room.database.DRoom

class SplashScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val database: DRoom = DRoom.getRoomInstance(application)

    /*private val _introPageShown = MutableStateFlow(false)
    val introPageShow: StateFlow<Boolean> = _introPageShown.asStateFlow()

    init {
        // Osveži introPageShow koristeći Flow iz baze podataka
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                database.introStateDao().getIntroPageShown().collectLatest { introShown ->
                    _introPageShown.value = introShown
                }
            }

        }
    }*/


    /*fun fetchData(): Flow<Boolean> = flow {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.introStateDao().getIntroPageShown()
            }
            result.collect{
                _introPageShown.value = it
            }
        }

    }*/

}