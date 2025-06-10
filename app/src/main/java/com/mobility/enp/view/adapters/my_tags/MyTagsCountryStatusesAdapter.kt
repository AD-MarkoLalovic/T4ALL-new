package com.mobility.enp.view.adapters.my_tags

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.CardCyclerTagStatusesBinding
import com.mobility.enp.view.ui_models.my_tags.TagStatusUiModel

class MyTagsCountryStatusesAdapter :
    ListAdapter<TagStatusUiModel, MyTagsCountryStatusesAdapter.CountryStatusesViewHolder>(
        DIFF_CALLBACK
    ) {

    inner class CountryStatusesViewHolder(val binding: CardCyclerTagStatusesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(status: TagStatusUiModel) {
            val flagResId = when (status.statusesCountry) {
                "MK" -> R.drawable.macedonia_flag
                "RS" -> R.drawable.serbia_flag
                "ME" -> R.drawable.montenegro_flag
                "HR" -> R.drawable.croatia_flag
                else -> null
            }

            binding.flagIcon.setImageResource(flagResId ?: 0)

            // Ako je zemlja HR, primeni poseban stil i preskoči ostale statuse
            if (status.statusesCountry == "HR") {
                val customTextColor =
                    ContextCompat.getColor(binding.root.context, R.color.tag_active)
                val customBackground = ContextCompat.getColor(
                    binding.root.context,
                    R.color.figmaToolHistoryPaidBackground
                )

                binding.countryStatus.text =
                    binding.root.context.getString(R.string.check_status_on_hac_portal)
                binding.countryStatus.setTextColor(customTextColor)
                binding.root.backgroundTintList = ColorStateList.valueOf(customBackground)

                return
            }

            binding.countryStatus.text = status.statusText

            // Postavljanje boje i pozadine
            status.statusValue?.let { value ->
                setStatusAppearance(value)
            }

        }

        private fun setStatusAppearance(status: Int) {
            val (textColor, backgroundTint) = when (status) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryStatusesViewHolder {
        return CountryStatusesViewHolder(
            CardCyclerTagStatusesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CountryStatusesViewHolder, position: Int) {
        val currentCountryStatus = getItem(position)
        holder.bind(currentCountryStatus)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TagStatusUiModel>() {
            override fun areItemsTheSame(
                oldItem: TagStatusUiModel,
                newItem: TagStatusUiModel
            ): Boolean {
                return oldItem.statusesCountry == newItem.statusesCountry
            }

            override fun areContentsTheSame(
                oldItem: TagStatusUiModel,
                newItem: TagStatusUiModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}