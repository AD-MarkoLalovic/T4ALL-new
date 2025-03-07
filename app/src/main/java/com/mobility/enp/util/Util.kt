package com.mobility.enp.util

import android.content.Context
import androidx.core.content.ContextCompat
import com.mobility.enp.R
import com.mobility.enp.data.model.franchise.FranchiseModel
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


    /**
     * Retrieves the FranchiseModel associated with a given portal key.
     *
     * This function uses a predefined map to look up the corresponding FranchiseModel
     * based on the provided portalKey. Each portalKey represents a unique franchise,
     * such as "Telekom portal", "S-blue", "AMSS portal", or "Tehnomanija portal".
     *
     * @param portalKey The unique identifier (UUID) of the franchise portal.
     *                  Examples include:
     *                  - "ad7e2bb9-22a5-4184-9c9b-5c384a506cb3" (Telekom portal)
     *                  - "a2ac8612-4b25-43e3-8017-fcf8ad0da0c4" (S-blue)
     *                  - "9aa3e972-d84b-40df-b35d-d14a229c03e3" (AMSS portal)
     *                  - "d47b35d1-bb44-4618-9b31-cf7e961595ec" (Tehnomanija portal)
     * @param context The application context, used to access resources like colors and drawables.
     * @return The FranchiseModel associated with the given portalKey, or null if no
     *         matching key is found in the map.
     */
    fun fransizerID(portalKey: String, context: Context): FranchiseModel? {

        val map = mutableMapOf<String, FranchiseModel>()

        map["ad7e2bb9-22a5-4184-9c9b-5c384a506cb3"] = FranchiseModel(
            "ad7e2bb9-22a5-4184-9c9b-5c384a506cb3",
            "Telekom portal",
            context.resources.getColor(R.color.franchiser_telekom_srbija_mtel, null),
            ContextCompat.getDrawable(context, R.drawable.telekom_srbija),
            R.drawable.telekom_srbija_profile,
            ContextCompat.getDrawable(context, R.drawable.telekom_logo), false,
            ContextCompat.getColorStateList(context, R.color.bottom_nav_color_telekom),
            R.drawable.promotions_dot_telekom,
            context.resources.getColor(
                R.color.franchiser_telekom_srbija_mtel_half_visibility,
                null
            ),
            R.drawable.toolbar_shared_back_telekom,
            R.drawable.ic_arrow_down_telekom
        )

        map["a2ac8612-4b25-43e3-8017-fcf8ad0da0c4"] = FranchiseModel(
            "a2ac8612-4b25-43e3-8017-fcf8ad0da0c4",
            "S-blue",
            context.resources.getColor(R.color.franchiser_s_blue, null),
            ContextCompat.getDrawable(context, R.drawable.s_blue),
            R.drawable.s_blue_profile,
            ContextCompat.getDrawable(context, R.drawable.s_blue_logo), true,
            ContextCompat.getColorStateList(context, R.color.bottom_nav_color_s_blue),
            R.drawable.promotions_dot_s_blue,
            context.resources.getColor(R.color.franchiser_s_blue_half_visibility, null),
            R.drawable.toolbar_shared_back_s_blue,
            R.drawable.ic_arrow_down_s_blue
        )

        map["9aa3e972-d84b-40df-b35d-d14a229c03e3"] = FranchiseModel(
            "9aa3e972-d84b-40df-b35d-d14a229c03e3",
            "AMSS portal",
            context.resources.getColor(R.color.franchiser_amss, null),
            ContextCompat.getDrawable(context, R.drawable.novi_amss),
            R.drawable.novi_amss_profile,
            ContextCompat.getDrawable(context, R.drawable.amss_logo), true,
            ContextCompat.getColorStateList(context, R.color.bottom_nav_color_amss),
            R.drawable.promotions_dot_amcc,
            context.resources.getColor(R.color.franchiser_amss_half_visibility, null),
            R.drawable.toolbar_shared_back_amss,
            R.drawable.ic_arrow_down_amss
        )

        map["d47b35d1-bb44-4618-9b31-cf7e961595ec"] = FranchiseModel(
            "d47b35d1-bb44-4618-9b31-cf7e961595ec",
            "Tehnomanija portal",
            context.resources.getColor(R.color.franchiser_tehnomania, null),
            ContextCompat.getDrawable(context, R.drawable.tehnomanija),
            R.drawable.tehnomanija_profile,
            ContextCompat.getDrawable(context, R.drawable.tehnomanija_logo), true,
            ContextCompat.getColorStateList(context, R.color.bottom_nav_color_tehnomania),
            R.drawable.promotions_dot_tehnomania,
            context.resources.getColor(R.color.franchiser_tehnomania_half_visibility, null),
            R.drawable.toolbar_shared_back_tehnomania,
            R.drawable.ic_arrow_down_tehnomania
        )


        return map[portalKey]
    }

}