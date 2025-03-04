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

    fun fransizerID(id:String):String?{

        val map = mutableMapOf<String, String>()

        map["Tehno-coop portal"] = "96371708-44d7-4438-b4f1-79f42bbac918"
        map["Telekom portal"] = "ad7e2bb9-22a5-4184-9c9b-5c384a506cb3"
        map["MTEL TR"] = "60c2f558-6368-44c5-a520-fa2b56041869"
        map["MTEL BA"] = "2d9da5de-9113-41e3-a8b4-09c2ccfec285"
        map["MTEL ME"] = "84f46084-4038-4ff2-9a77-b756a454f49f"
        map["MTEL MK"] = "4dcf082c-7232-47f8-b64f-3c27791364d6"
        map["S-blue"] = "a2ac8612-4b25-43e3-8017-fcf8ad0da0c4"
        map["AUTO TAG RAFAELO DOO (T4A) portal"] = "ed232756-b001-42e7-a3aa-c6c43b9ce49f"
        map["ENPUT DOO (T4A) portal"] = "2263768e-e3a5-48f8-8e7a-545f6c141318"
        map["FREE TRANS portal"] = "183e7ccd-353d-4dd6-950c-8f033dd94620"
        map["MTEL AT portal"] = "a577ddf8-1c08-4aa6-9d95-8ab2fd5c8b6c"
        map["MTEL DE portal"] = "263a2e3d-b544-480d-a604-0dd036c8d4ed"
        map["MTEL CH portal"] = "19334ec8-b056-486e-8faa-e42fe895d930"
        map["AMSS portal"] = "9aa3e972-d84b-40df-b35d-d14a229c03e3"
        map["Tehnomanija portal"] = "d47b35d1-bb44-4618-9b31-cf7e961595ec"
        map["IMPEREX ROADS BALKANS DOO (T4A) portal"] = "0768bada-5f65-4521-8b81-bb0eda51b806"

        return map[id]
    }

}