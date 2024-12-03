package com.mobility.enp.interf

import com.mobility.enp.data.model.api_home_page.homedata.Promotion

interface PromotionInterface {
    fun onCountrySelected(promotion: Promotion)
}