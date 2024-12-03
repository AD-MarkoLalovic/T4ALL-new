package com.mobility.enp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.registration.CountryModel
import com.mobility.enp.databinding.CardCountriesRegistrationBinding

class SelectCountryAdapter(
    private val list: ArrayList<CountryModel>,
) :
    RecyclerView.Adapter<SelectCountryAdapter.SelectCountryViewHolder>() {
    private var selectedPosition = 0
    private lateinit var selectedCountry: SelectedCountry

    fun setInter(selectedCountry: SelectedCountry) {
        this.selectedCountry = selectedCountry
    }

    class SelectCountryViewHolder(val binding: CardCountriesRegistrationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(county: CountryModel) {
            binding.countryData = county
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectCountryViewHolder {
        val binding = CardCountriesRegistrationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SelectCountryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SelectCountryViewHolder, position: Int) {
        val adapterPosition = holder.bindingAdapterPosition
        val current = list[adapterPosition]
        holder.bind(current)

        holder.binding.radioButton.isChecked = adapterPosition == selectedPosition

        // Odredjujem koja boja treba da bude primenjena na osnovu selektovane drzave
        val colorStateList = if (adapterPosition == selectedPosition) {
            ContextCompat.getColorStateList(holder.itemView.context, R.color.figmaSplashScreenColor)
        } else {
            ContextCompat.getColorStateList(holder.itemView.context, R.color.primary_light_dark)
        }

        holder.binding.radioButton.buttonTintList = colorStateList
        holder.binding.countryTextField.setTextColor(colorStateList)

        holder.binding.radioButton.setOnClickListener {

            if (selectedPosition != adapterPosition) {
                // Unselect the previously selected item
                list.getOrNull(selectedPosition)?.isChecked = false
                notifyItemChanged(selectedPosition)

                // Select the current item
                current.isChecked = true
                selectedPosition = adapterPosition
                notifyItemChanged(adapterPosition)

                selectedCountry.pickedCountry(list[selectedPosition])
            }
        }
        if (!current.isIconVisible) {
            holder.binding.icon.visibility = View.GONE
        }
    }

    interface SelectedCountry {
        fun pickedCountry(county: CountryModel)
    }
}