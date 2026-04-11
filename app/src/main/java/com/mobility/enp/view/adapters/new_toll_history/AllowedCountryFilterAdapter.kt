package com.mobility.enp.view.adapters.new_toll_history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.databinding.ItemCountryFilterBinding
import com.mobility.enp.view.ui_models.toll_history.AllowedCountryUi

class AllowedCountryFilterAdapter(
    private val onCountrySelected: (country: String) -> Unit
) : ListAdapter<AllowedCountryUi, AllowedCountryFilterAdapter.CountryViewHolder>(DIFF_CALLBACK) {

    inner class CountryViewHolder(private val binding: ItemCountryFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AllowedCountryUi) {
            binding.selectedCountryFilter.text = item.name
            binding.selectedCountryFilter.isSelected = item.isSelected

            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onCountrySelected(item.value)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CountryViewHolder {
        val binding =
            ItemCountryFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CountryViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AllowedCountryUi>() {
            override fun areItemsTheSame(
                oldItem: AllowedCountryUi,
                newItem: AllowedCountryUi
            ): Boolean {
                return oldItem.value == newItem.value
            }

            override fun areContentsTheSame(
                oldItem: AllowedCountryUi,
                newItem: AllowedCountryUi
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}