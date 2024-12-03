package com.mobility.enp.view.adapters.tags

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tags.Status
import com.mobility.enp.databinding.CardCyclerTagStatusesBinding

class AdapterCountryStatuses(val status: List<Status>) :
    RecyclerView.Adapter<AdapterCountryStatuses.AdapterCountryStatusesViewHolder>() {

    inner class AdapterCountryStatusesViewHolder(val binding: CardCyclerTagStatusesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(status: Status) {
            binding.data = status

            // Postavljanje zastave
            val flagResId = when (status.country.value) {
                "MK" -> R.drawable.macedonia_flag
                "RS" -> R.drawable.serbia_flag
                "ME" -> R.drawable.montenegro_flag
                else -> null
            }

            binding.flagIcon.setImageResource(flagResId ?: 0)

            // Postavljanje boje i pozadine
            setStatusAppearance(status)

            binding.executePendingBindings()
        }

        private fun setStatusAppearance(status: Status) {
            val (textColor, backgroundTint) = when (status.status.value) {
                5, 6, 8, 10, 12 -> Pair(
                    R.color.figmaColorObjection,
                    R.color.figmaToolHistoryUnpaidBackground
                )

                4 -> Pair(R.color.tag_active, R.color.figmaToolHistoryPaidBackground)
                9 -> Pair(R.color.dark_orange, R.color.soft_peach)
                1, 11 -> Pair(R.color.primary_light_dark, R.color.primary_light_light)
                13 -> {
                    binding.root.background = ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.border_tag_state_deactivated
                    )
                    return // Prekidamo ovde, jer ne koristimo tint za status 13
                }

                else -> Pair(android.R.color.transparent, android.R.color.transparent)
            }

            binding.countryStatus.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    textColor
                )
            )
            binding.root.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, backgroundTint))
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AdapterCountryStatusesViewHolder {
        return AdapterCountryStatusesViewHolder(
            CardCyclerTagStatusesBinding.inflate(
                LayoutInflater.from(
                    p0.context
                ), p0, false
            )
        )
    }

    override fun getItemCount(): Int = status.size

    override fun onBindViewHolder(p0: AdapterCountryStatusesViewHolder, p1: Int) {
        p0.bind(status[p0.bindingAdapterPosition])
    }

}