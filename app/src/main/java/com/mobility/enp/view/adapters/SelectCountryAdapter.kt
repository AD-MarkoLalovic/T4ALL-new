package com.mobility.enp.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.registration.RegistrationCountry
import com.mobility.enp.databinding.CardCountriesRegistrationBinding

class SelectCountryAdapter(
    private val list: List<RegistrationCountry>,
    private val selectedCountry: (String) -> Unit
) :
    RecyclerView.Adapter<SelectCountryAdapter.SelectCountryViewHolder>() {
    private var selectedPosition = 0

    class SelectCountryViewHolder(val binding: CardCountriesRegistrationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(county: RegistrationCountry, isSelected: Boolean) {
            binding.radioButton.isChecked = isSelected
            binding.countryTextField.text = county.name
            binding.icon.setImageResource(county.flagResId)

            val colorStateList = if (isSelected) {
                ContextCompat.getColorStateList(binding.root.context, R.color.figmaSplashScreenColor)
            } else {
                ContextCompat.getColorStateList(binding.root.context, R.color.primary_light_dark)
            }

            binding.radioButton.buttonTintList = colorStateList
            binding.countryTextField.setTextColor(colorStateList)
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
        val current = list[position]
        val isSelected = position == selectedPosition
        holder.bind(current, isSelected)

        holder.binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && selectedPosition != position) {
                val previousPosition = selectedPosition
                selectedPosition = holder.bindingAdapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                selectedCountry(current.code)
            }
        }
    }

}