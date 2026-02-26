package com.mobility.enp.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.registration.RegistrationCountry
import com.mobility.enp.databinding.CardCountriesRegistrationBinding

class SelectCountryAdapter(
    private val items: List<RegistrationCountry>,
    selectedCode: String,
    private val onClick: (String) -> Unit
) :
    RecyclerView.Adapter<SelectCountryAdapter.CountryVH>() {

    private var selectedCodeInternal: String = selectedCode

    fun setSelectedCode(newCode: String) {
        if (newCode == selectedCodeInternal) return

        val oldIndex = items.indexOfFirst { it.code == selectedCodeInternal }
        val newIndex = items.indexOfFirst { it.code == newCode }

        selectedCodeInternal = newCode

        if (oldIndex != -1) notifyItemChanged(oldIndex)
        if (newIndex != -1) notifyItemChanged(newIndex)
    }

    class CountryVH(val binding: CardCountriesRegistrationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(county: RegistrationCountry, isSelected: Boolean, onClick: (String) -> Unit) {
            val context = binding.root.context

            binding.radioButton.isChecked = isSelected
            binding.countryTextField.text = context.getString(county.name)
            binding.icon.setImageResource(county.flagResId)

            val colorStateList = if (isSelected) {
                ContextCompat.getColorStateList(
                    binding.root.context,
                    R.color.figmaSplashScreenColor
                )
            } else {
                ContextCompat.getColorStateList(binding.root.context, R.color.primary_light_dark)
            }

            binding.radioButton.buttonTintList = colorStateList
            binding.countryTextField.setTextColor(colorStateList)

            binding.root.setOnClickListener {
                onClick(county.code)
            }

            binding.radioButton.setOnClickListener {
                onClick(county.code)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryVH {
        val binding = CardCountriesRegistrationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CountryVH(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: CountryVH, position: Int) {
        val item = items[position]
        val isSelected = item.code == selectedCodeInternal

        holder.bind(item, isSelected, onClick)
    }

}