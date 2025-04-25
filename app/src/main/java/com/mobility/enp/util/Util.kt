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


    fun franchiseID(portalKey: String, context: Context): FranchiseModel? {
        //#franchise grey color
        val telecomSerbiaAndMTellLogo = when (portalKey) {
            "ad7e2bb9-22a5-4184-9c9b-5c384a506cb3" -> R.drawable.telekom_srbija_new
            "2d9da5de-9113-41e3-a8b4-09c2ccfec285", "84f46084-4038-4ff2-9a77-b756a454f49f" -> R.drawable.mtel_ba_me_logo_svg_new
            "4dcf082c-7232-47f8-b64f-3c27791364d6", "a577ddf8-1c08-4aa6-9d95-8ab2fd5c8b6c",
            "19334ec8-b056-486e-8faa-e42fe895d930", "263a2e3d-b544-480d-a604-0dd036c8d4ed",
            "60c2f558-6368-44c5-a520-fa2b56041869" -> R.drawable.mtel_mk_at_ch_de_tr_logo_svg_new

            "96371708-44d7-4438-b4f1-79f42bbac918" -> R.drawable.tehnocoop_logo_svg
            else -> R.drawable.ic_logo_home_screen_svg
        }

        val franchise = when (portalKey) {
            "ad7e2bb9-22a5-4184-9c9b-5c384a506cb3", "2d9da5de-9113-41e3-a8b4-09c2ccfec285",
            "84f46084-4038-4ff2-9a77-b756a454f49f", "4dcf082c-7232-47f8-b64f-3c27791364d6", "a577ddf8-1c08-4aa6-9d95-8ab2fd5c8b6c",
            "19334ec8-b056-486e-8faa-e42fe895d930", "263a2e3d-b544-480d-a604-0dd036c8d4ed",
            "60c2f558-6368-44c5-a520-fa2b56041869", "96371708-44d7-4438-b4f1-79f42bbac918" -> FranchiseModel(
                portalKey,
                "Telekom portal",
                context.resources.getColor(R.color.franchiser_telekom_srbija_mtel, null),
                ContextCompat.getDrawable(context, R.drawable.telekom_srbija),
                R.drawable.telekom_srbija_profile,
                ContextCompat.getDrawable(context, telecomSerbiaAndMTellLogo),
                when (portalKey) {  // tehnocoop has background color on toolbar
                    "96371708-44d7-4438-b4f1-79f42bbac918" -> true
                    else -> false
                },
                ContextCompat.getColorStateList(context, R.color.bottom_nav_color_telekom),
                R.drawable.promotions_dot_telekom,
                context.resources.getColor(
                    R.color.franchiser_telekom_srbija_mtel_half_visibility,
                    null
                ),
                R.drawable.toolbar_shared_back_telekom,
                R.drawable.ic_arrow_down_telekom,
                R.drawable.ic_arrow_up_telekom,
                R.drawable.arrow_right_telekom,
                R.drawable.loop_telekom,
                R.drawable.ic_camera_telekom,
                R.drawable.ic_language_home_screen_telekom,
                R.drawable.calendar_today_telekom,
                R.drawable.plus_ic_telekom,
                R.drawable.ic_close_telekom,
                R.drawable.default_user_picture_mtel,
                ContextCompat.getColorStateList(context, R.color.home_page_text_default),
                when (portalKey) {  // tehnocoop has background color on toolbar
                    "96371708-44d7-4438-b4f1-79f42bbac918" ->
                        ContextCompat.getColorStateList(context, R.color.flavor_text_sbb_e_box_tehnocoop_color)

                    else ->
                        ContextCompat.getColorStateList(context, R.color.flavor_text_default)
                }
            )

            "a2ac8612-4b25-43e3-8017-fcf8ad0da0c4" -> FranchiseModel(
                "a2ac8612-4b25-43e3-8017-fcf8ad0da0c4",
                "S-blue",
                context.resources.getColor(R.color.franchiser_s_blue, null),
                ContextCompat.getDrawable(context, R.drawable.s_blue),
                R.drawable.s_blue_profile,
                ContextCompat.getDrawable(context, R.drawable.s_blue_logo_svg), true,
                ContextCompat.getColorStateList(context, R.color.bottom_nav_color_s_blue),
                R.drawable.promotions_dot_s_blue,
                context.resources.getColor(R.color.franchiser_s_blue_half_visibility, null),
                R.drawable.toolbar_shared_back_s_blue,
                R.drawable.ic_arrow_down_s_blue,
                R.drawable.ic_arrow_up_s_blue,
                R.drawable.arrow_right_s_blue,
                R.drawable.loop_s_blue,
                R.drawable.ic_camera_s_blue,
                R.drawable.ic_language_home_screen_s_blue,
                R.drawable.calendar_today_s_blue,
                R.drawable.plus_ic_s_blue,
                R.drawable.ic_close_s_blue,
                R.drawable.default_user_picture_s_blue,
                ContextCompat.getColorStateList(context, R.color.home_page_text_default),
                ContextCompat.getColorStateList(context, R.color.flavor_text_default)
            )

            "9aa3e972-d84b-40df-b35d-d14a229c03e3" -> FranchiseModel(
                "9aa3e972-d84b-40df-b35d-d14a229c03e3",
                "AMSS portal",
                context.resources.getColor(R.color.franchiser_amss, null),
                ContextCompat.getDrawable(context, R.drawable.novi_amss),
                R.drawable.novi_amss_profile,
                ContextCompat.getDrawable(context, R.drawable.amss_logo_svg), true,
                ContextCompat.getColorStateList(context, R.color.bottom_nav_color_amss),
                R.drawable.promotions_dot_amcc,
                context.resources.getColor(R.color.franchiser_amss_half_visibility, null),
                R.drawable.toolbar_shared_back_amss,
                R.drawable.ic_arrow_down_amss,
                R.drawable.ic_arrow_up_amss,
                R.drawable.arrow_right_amss,
                R.drawable.loop_amss,
                R.drawable.ic_camera_amss,
                R.drawable.ic_language_home_screen_amss,
                R.drawable.calendar_today_amss,
                R.drawable.plus_ic_amss,
                R.drawable.ic_close_amss,
                R.drawable.default_user_picture_amss,
                ContextCompat.getColorStateList(
                    context,
                    R.color.amss_tehnomania_home_welcome_text_color
                ),
                ContextCompat.getColorStateList(context, R.color.flavor_text_amss_tehnomania)

            )

            "d47b35d1-bb44-4618-9b31-cf7e961595ec" -> FranchiseModel(
                "d47b35d1-bb44-4618-9b31-cf7e961595ec",
                "Tehnomanija portal",
                context.resources.getColor(R.color.franchiser_tehnomania, null),
                ContextCompat.getDrawable(context, R.drawable.tehnomanija),
                R.drawable.tehnomanija_profile,
                ContextCompat.getDrawable(context, R.drawable.tehnomania_logo_svg), true,
                ContextCompat.getColorStateList(context, R.color.bottom_nav_color_tehnomania),
                R.drawable.promotions_dot_tehnomania,
                context.resources.getColor(R.color.franchiser_tehnomania_half_visibility, null),
                R.drawable.toolbar_shared_back_tehnomania,
                R.drawable.ic_arrow_down_tehnomania,
                R.drawable.ic_arrow_up_tehnomania,
                R.drawable.arrow_right_tehnomania,
                R.drawable.loop_tehnomania,
                R.drawable.ic_camera_tehnomania,
                R.drawable.ic_language_home_screen_tehnomania,
                R.drawable.calendar_today_tehnomania,
                R.drawable.plus_ic_tehnomania,
                R.drawable.ic_close_tehnomania,
                R.drawable.default_user_picture_tehnomania,
                ContextCompat.getColorStateList(
                    context,
                    R.color.amss_tehnomania_home_welcome_text_color
                ),
                ContextCompat.getColorStateList(context, R.color.flavor_text_amss_tehnomania)
            )

            "2263768e-e3a5-48f8-8e7a-545f6c141318" -> FranchiseModel(
                "2263768e-e3a5-48f8-8e7a-545f6c141318",
                "Enput",
                context.resources.getColor(R.color.franchiser_enput, null),
                ContextCompat.getDrawable(context, R.drawable.enput_home),
                R.drawable.enput_profile,
                ContextCompat.getDrawable(context, R.drawable.enput_logo_svg), true,
                ContextCompat.getColorStateList(context, R.color.bottom_nav_color_enput),
                R.drawable.enput_promotion_dot,
                context.resources.getColor(R.color.franchiser_enput_half_color, null),
                R.drawable.toolbar_shared_back_enput,
                R.drawable.ic_arrow_down_enput,
                R.drawable.ic_arrow_up_enput,
                R.drawable.arrow_right_enput,
                R.drawable.loop_enput,
                R.drawable.ic_camera_enput,
                R.drawable.ic_language_home_screen_enput,
                R.drawable.calendar_today_enput,
                R.drawable.plus_ic_enput,
                R.drawable.ic_close_enput,
                R.drawable.default_user_picture_enput,
                ContextCompat.getColorStateList(context, R.color.home_page_text_default),
                ContextCompat.getColorStateList(context, R.color.flavor_text_enput)
            )

            "ed232756-b001-42e7-a3aa-c6c43b9ce49f" -> FranchiseModel(
                "ed232756-b001-42e7-a3aa-c6c43b9ce49f",
                "AUTO TAG RAFAELO DOO",
                context.resources.getColor(R.color.franchiser_tag_rafaelo, null),
                ContextCompat.getDrawable(context, R.drawable.auto_tag_rafaelo_home),
                R.drawable.auto_tag_rafaelo_profile,
                ContextCompat.getDrawable(context, R.drawable.rafaelo_logo_svg), true,
                ContextCompat.getColorStateList(context, R.color.bottom_nav_color_auto_tag_rafaelo),
                R.drawable.auto_tag_rafaelo_promotion_dot,
                context.resources.getColor(R.color.franchiser_tag_rafaelo_half_visibility, null),
                R.drawable.toolbar_shared_back_tag_rafaelo,
                R.drawable.ic_arrow_down_tag_rafaelo,
                R.drawable.ic_arrow_up_tag_rafaelo,
                R.drawable.arrow_right_tag_rafaelo,
                R.drawable.loop_tag_rafaelo,
                R.drawable.ic_camera_tag_rafaelo,
                R.drawable.ic_language_home_screen_tag_rafaelo,
                R.drawable.calendar_today_tag_rafaelo,
                R.drawable.plus_ic_tag_rafaelo,
                R.drawable.ic_close_tag_rafaelo,
                R.drawable.default_user_picture_rafaelo,
                ContextCompat.getColorStateList(context, R.color.home_page_text_default),
                ContextCompat.getColorStateList(context, R.color.flavor_text_euro_petrol_rafaelo)
            )

            "183e7ccd-353d-4dd6-950c-8f033dd94620" -> FranchiseModel(
                "183e7ccd-353d-4dd6-950c-8f033dd94620",
                "FREE TRANS 010",
                context.resources.getColor(R.color.franchiser_free_trans, null),
                ContextCompat.getDrawable(context, R.drawable.free_trans_home),
                R.drawable.free_trans_profile,
                ContextCompat.getDrawable(context, R.drawable.free_trans_logo_svg), true,
                ContextCompat.getColorStateList(context, R.color.bottom_nav_color_free_trans),
                R.drawable.free_trans_promotion_dot,
                context.resources.getColor(R.color.franchiser_free_trans_half_visibility, null),
                R.drawable.toolbar_shared_back_free_trans,
                R.drawable.ic_arrow_down_free_trans,
                R.drawable.ic_arrow_up_free_trans,
                R.drawable.arrow_right_free_trans,
                R.drawable.loop_free_trans,
                R.drawable.ic_camera_free_trans,
                R.drawable.ic_language_home_screen_free_trans,
                R.drawable.calendar_today_free_trans,
                R.drawable.plus_ic_free_trans,
                R.drawable.ic_close_free_trans,
                R.drawable.default_user_picture_free_trans,
                ContextCompat.getColorStateList(context, R.color.home_page_text_default),
                ContextCompat.getColorStateList(context, R.color.flavor_text_default)
            )

            "0768bada-5f65-4521-8b81-bb0eda51b806" -> FranchiseModel(
                "0768bada-5f65-4521-8b81-bb0eda51b806",
                "E-Box",
                context.resources.getColor(R.color.franchiser_e_box, null),
                ContextCompat.getDrawable(context, R.drawable.e_box_home),
                R.drawable.e_box_profile,
                ContextCompat.getDrawable(context, R.drawable.e_box_logo_svg), true,
                ContextCompat.getColorStateList(context, R.color.bottom_nav_color_e_box),
                R.drawable.e_box_promotion_dot,
                context.resources.getColor(R.color.franchiser_e_box_half_visibility, null),
                R.drawable.toolbar_shared_back_e_box,
                R.drawable.ic_arrow_down_e_box,
                R.drawable.ic_arrow_up_e_box,
                R.drawable.arrow_right_e_box,
                R.drawable.loop_e_box,
                R.drawable.ic_camera_e_box,
                R.drawable.ic_language_home_screen_e_box,
                R.drawable.calendar_today_e_box,
                R.drawable.plus_ic_e_box,
                R.drawable.ic_close_e_box,
                R.drawable.default_user_picture_e_box,
                ContextCompat.getColorStateList(context, R.color.home_page_text_default),
                ContextCompat.getColorStateList(
                    context,
                    R.color.flavor_text_sbb_e_box_tehnocoop_color
                )
            )

            // TODO: Treba ubaciti odgovarajuci primaryKey i Id za Pay&Roll
            "Pay&Roll" -> FranchiseModel(
                "Pay&Roll",
                "Pay&Roll",
                context.resources.getColor(R.color.franchiser_pay_and_roll, null),
                ContextCompat.getDrawable(context, R.drawable.pay_and_roll_home),
                R.drawable.pay_and_roll_profile,
                ContextCompat.getDrawable(context, R.drawable.pay_n_roll_logo_svg_new), true,
                ContextCompat.getColorStateList(context, R.color.bottom_nav_color_pay_and_roll),
                R.drawable.pay_and_roll_promotion_dot,
                context.resources.getColor(R.color.franchiser_pay_and_roll_half_visibility, null),
                R.drawable.toolbar_shared_back_pay_and_roll,
                R.drawable.ic_arrow_down_pay_and_roll,
                R.drawable.ic_arrow_up_pay_and_roll,
                R.drawable.arrow_right_pay_and_roll,
                R.drawable.loop_pay_and_roll,
                R.drawable.ic_camera_pay_and_roll,
                R.drawable.ic_language_home_screen_pay_and_roll,
                R.drawable.calendar_today_pay_and_roll,
                R.drawable.plus_ic_pay_and_roll,
                R.drawable.ic_close_pay_and_roll,
                R.drawable.default_user_picture_pay_n_roll,
                ContextCompat.getColorStateList(context, R.color.home_page_text_default),
                ContextCompat.getColorStateList(context, R.color.flavor_text_default)
            )

            "5fcb381c-176c-4d94-93bc-feb476c9d62a" -> FranchiseModel(
                "5fcb381c-176c-4d94-93bc-feb476c9d62a",
                "SBB Portal",
                context.resources.getColor(R.color.franchiser_sbb, null),
                ContextCompat.getDrawable(context, R.drawable.sbb_home),
                R.drawable.sbb_profile,
                ContextCompat.getDrawable(context, R.drawable.sbb_logo_svg), true,
                ContextCompat.getColorStateList(context, R.color.bottom_nav_color_sbb),
                R.drawable.sbb_promotion_dot,
                context.resources.getColor(R.color.franchiser_sbb_half_visibility, null),
                R.drawable.toolbar_shared_back_sbb,
                R.drawable.ic_arrow_down_sbb,
                R.drawable.ic_arrow_up_sbb,
                R.drawable.arrow_right_sbb,
                R.drawable.loop_sbb,
                R.drawable.ic_camera_sbb,
                R.drawable.ic_language_home_screen_sbb,
                R.drawable.calendar_today_sbb,
                R.drawable.plus_ic_sbb,
                R.drawable.ic_close_sbb,
                R.drawable.default_user_picture_sbb,
                ContextCompat.getColorStateList(context, R.color.home_page_text_default),
                ContextCompat.getColorStateList(
                    context,
                    R.color.flavor_text_sbb_e_box_tehnocoop_color
                )
            )

            "86841eac-1d56-49fb-a1fb-219dae2b3681" -> FranchiseModel(
                "86841eac-1d56-49fb-a1fb-219dae2b3681",
                "POSTE SRBIJE",
                context.resources.getColor(R.color.franchiser_serbian_post_office, null),
                ContextCompat.getDrawable(context, R.drawable.serbian_post_office_home),
                R.drawable.serbian_post_office_profile,
                ContextCompat.getDrawable(context, R.drawable.posta_srbije_logo_svg),
                false,  // has no toolbar background
                ContextCompat.getColorStateList(
                    context,
                    R.color.bottom_nav_color_serbian_post_office
                ),
                R.drawable.serbian_post_office_promotion_dot,
                context.resources.getColor(
                    R.color.franchiser_serbian_post_office_half_visibility,
                    null
                ),
                R.drawable.toolbar_shared_back_serbian_post_office,
                R.drawable.ic_arrow_down_serbian_post_office,
                R.drawable.ic_arrow_up_serbian_post_office,
                R.drawable.arrow_right_serbian_post_office,
                R.drawable.loop_serbian_post_office,
                R.drawable.ic_camera_serbian_post_office,
                R.drawable.ic_language_home_screen_serbian_post_office,
                R.drawable.calendar_today_serbian_post_office,
                R.drawable.plus_ic_serbian_post_office,
                R.drawable.ic_close_serbian_post_office,
                R.drawable.default_user_picture_posta_srbije,
                ContextCompat.getColorStateList(context, R.color.home_page_text_default),
                ContextCompat.getColorStateList(context, R.color.flavor_text_default)
            )

            "0e16dfd9-b94f-4e1a-aaca-47733b312e79" -> FranchiseModel(
                "0e16dfd9-b94f-4e1a-aaca-47733b312e79",
                "Euro Petrol",
                context.resources.getColor(R.color.franchiser_euro_petrol, null),
                ContextCompat.getDrawable(context, R.drawable.euro_petrol_home),
                R.drawable.euro_petrol_profile,
                ContextCompat.getDrawable(context, R.drawable.euro_petrol_logo_svg_new), true,
                ContextCompat.getColorStateList(context, R.color.bottom_nav_color_euro_petrol),
                R.drawable.euro_petrol_promotion_dot,
                context.resources.getColor(R.color.franchiser_euro_petrol_half_visibility, null),
                R.drawable.toolbar_shared_back_euro_petrol,
                R.drawable.ic_arrow_down_euro_petrol,
                R.drawable.ic_arrow_up_euro_petrol,
                R.drawable.arrow_right_euro_petrol,
                R.drawable.loop_euro_petrol,
                R.drawable.ic_camera_euro_petrol,
                R.drawable.ic_language_home_screen_euro_petrol,
                R.drawable.calendar_today_euro_petrol,
                R.drawable.plus_ic_euro_petrol,
                R.drawable.ic_close_euro_petrol,
                R.drawable.default_user_picture_euro_petrol,
                ContextCompat.getColorStateList(context, R.color.home_page_text_default),
                ContextCompat.getColorStateList(context, R.color.flavor_text_euro_petrol_rafaelo)
            )

            else -> null
        }

        return franchise
    }

}