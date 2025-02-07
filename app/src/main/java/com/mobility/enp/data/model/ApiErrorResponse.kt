package com.mobility.enp.data.model

data class ApiErrorResponse(
    val message: String?, // Opšta poruka greške koja je obuhvatila sve greške
    val errors: Map<String, List<String>>?, // Mapu koja sadrži konkretne greške po poljima
    val code: Int? = null,            // HTTP status code from the server
)
