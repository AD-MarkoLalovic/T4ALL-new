package com.mobility.enp.network.error

import java.io.IOException

class ApiMessageException(
    val code: Int,
    val userMessage: String
) : IOException(userMessage)
