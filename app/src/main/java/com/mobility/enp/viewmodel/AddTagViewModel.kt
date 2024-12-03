package com.mobility.enp.viewmodel

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.databinding.DialogConfirmedAddTagBinding
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddTagViewModel(application: Application) : AndroidViewModel(application) {
    private var database: DRoom? = null
    private var application = application

    fun initDatabase() {
        database = DRoom.getRoomInstance(application)
    }

    fun addTagForUser(
        serial: String,
        verificationCode: String,
        data: MutableLiveData<LostTagResponse>,
        errorBody: MutableLiveData<ErrorBody>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            database?.loginDao()?.fetchAllowedUsers()?.accessToken?.let {
                Repository.postAddTag(it, serial, verificationCode, errorBody, data, application)
            }
        }
    }

    fun showDialogAddTagConfirmed(view: View) {
        val dialogBuilder = AlertDialog.Builder(view.context, R.style.CustomAlertDialog)
        val dialogView = DataBindingUtil.inflate<DialogConfirmedAddTagBinding>(
            LayoutInflater.from(view.context),
            R.layout.dialog_confirmed_add_tag,
            null,
            false
        )
        dialogBuilder.setView(dialogView.root)

        val alertDialog = dialogBuilder.create()

        dialogView.confirmAddTag.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}