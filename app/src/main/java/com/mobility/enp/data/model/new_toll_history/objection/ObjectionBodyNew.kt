package com.mobility.enp.data.model.new_toll_history.objection

data class ObjectionBodyNew(
    val complaintRequestId: Int,
    val objectionItemDate: String,
    val objectionItemNumber: String,
    val objectionItemOptions: String = "1",
    val objectionItemReason: String
)