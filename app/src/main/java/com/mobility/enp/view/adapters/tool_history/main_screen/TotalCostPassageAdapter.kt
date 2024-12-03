package com.mobility.enp.view.adapters.tool_history.main_screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.data.model.api_tool_history.TotalAmount
import com.mobility.enp.databinding.PassageHistoryTotalPriceBinding

class TotalCostPassageAdapter(private val countries: ArrayList<TotalAmount>) :
    RecyclerView.Adapter<TotalCostPassageAdapter.CountryAdapterViewHolder>() {


    class CountryAdapterViewHolder(val binding: PassageHistoryTotalPriceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: TotalAmount) {

            binding.data = data
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryAdapterViewHolder {
        return CountryAdapterViewHolder(
            PassageHistoryTotalPriceBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return countries.size
    }

    override fun onBindViewHolder(holder: CountryAdapterViewHolder, position: Int) {
        val current = countries[position]

        holder.bind(current)
    }

}