package com.mobility.enp.view.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_my_profile.cards.Country
import com.mobility.enp.databinding.ItemCardsCountryBinding

class CardsCountryAdapter(
    private var countries: List<Country> = arrayListOf(),
    private var listenerCountry: CountryListener
) : RecyclerView.Adapter<CardsCountryAdapter.CardsCountryViewHolder>() {

    private var selectedItemPos = RecyclerView.NO_POSITION

    inner class CardsCountryViewHolder(val binding: ItemCardsCountryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(country: Country) {
            binding.country = country

            binding.cardCountry.isEnabled = country.isClickable

            if (bindingAdapterPosition == selectedItemPos) {
                binding.cardCountry.background = ContextCompat.getDrawable(
                    binding.root.context,
                    R.drawable.rounded_status_marked_border
                )
                binding.txCountryFilter.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.white
                    )
                )
            } else {
                binding.cardCountry.background = ContextCompat.getDrawable(
                    binding.root.context,
                    R.drawable.rounded_status_unmarked_border_
                )
                binding.txCountryFilter.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.primary_light_dark
                    )
                )
            }

            // Onemogućavanje klika i stil za neklikabilne drzave
            if (!country.isClickable) {
                binding.txCountryFilter.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.country_filter
                    )
                )
                binding.cardCountry.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.country_filter
                    )
                )
            } else {
                binding.cardCountry.backgroundTintList = null
            }
            binding.cardCountry.setOnClickListener {
                if (country.isClickable) { // Ovaj kod se izvršava samo ako je zemlja klikabilna
                    setSingleSelected(bindingAdapterPosition)
                    listenerCountry.setCountryListener(country.code!!)
                }
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardsCountryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCardsCountryBinding.inflate(layoutInflater, parent, false)
        return CardsCountryViewHolder(binding)
    }

    override fun getItemCount(): Int = countries.size

    override fun onBindViewHolder(holder: CardsCountryViewHolder, position: Int) {
        holder.bind(countries[position])
    }

    private fun setSingleSelected(adapterPosition: Int) {
        if (adapterPosition == RecyclerView.NO_POSITION) return

        notifyItemChanged(selectedItemPos)
        selectedItemPos = adapterPosition
        notifyItemChanged(selectedItemPos)
    }

    fun updateCountries(newCountries: List<Country>) {
        countries = newCountries
        notifyDataSetChanged()
    }

    fun setSelectedCountry(countryCode: String) {
        val position = countries.indexOfFirst { it.code == countryCode }
        if (position != -1) {
            setSingleSelected(position)
        }
    }

    interface CountryListener {
        fun setCountryListener(country: String)
    }
}
