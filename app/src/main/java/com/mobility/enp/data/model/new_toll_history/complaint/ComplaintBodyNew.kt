package com.mobility.enp.data.model.new_toll_history.complaint

data class ComplaintBodyNew(
    val itemId: Int,
    val complaintRegistration: String,
    val complaintText: String,
    val complaintBankName: Int? = null,
    val accountZr: String? = null,
    val accountZr2: String? = null,
    val accountZr3: String? = null,
)