package com.mobility.enp.data.model.api_my_profile.refund_request.response

import com.mobility.enp.data.model.api_my_profile.refund_request.entity.DataRefundRequestEntity


data class RefundRequestsResponse(
    val data: List<DataRefundRequestDTO>
) {
    // Metoda koja konvertuje listu DTO objekata u listu entiteta
    fun toEntityList(): List<DataRefundRequestEntity> {
        return data.map { it.toEntity() } // Poziva toEntity() za svaki element u listi
    }
}