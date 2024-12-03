package com.mobility.enp.util

import java.time.Duration
import java.time.Instant

object Util {

    fun hasTimePassed(fromTimeMillis: Long): Boolean {

        val currentTime = Instant.now()
        val pastTime = Instant.ofEpochMilli(fromTimeMillis)

        val duration = Duration.between(pastTime, currentTime)

        return duration.toDays() > 9   // changed to 9 days for production purposes
    }

    fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }
}