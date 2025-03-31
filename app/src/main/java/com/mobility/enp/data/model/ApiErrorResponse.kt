package com.mobility.enp.data.model

data class ApiErrorResponse(
    val message: String?, // Opšta poruka greške koja je obuhvatila sve greške
    val errors: List<String>?, // Lista konkretnih grešaka
    val code: Int? = null, // HTTP status code from the server
)

