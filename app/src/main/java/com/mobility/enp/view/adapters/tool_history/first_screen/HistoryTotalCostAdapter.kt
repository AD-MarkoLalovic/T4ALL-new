package com.mobility.enp.view.adapters.tool_history.first_screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.data.model.api_tool_history.v2base_model.SumTag
import com.mobility.enp.databinding.PassageHistoryTotalPriceBinding

class HistoryTotalCostAdapter(private val countries: List<SumTag>?) :
    RecyclerView.Adapter<HistoryTotalCostAdapter.CountryAdapterViewHolder>() {


    class CountryAdapterViewHolder(val binding: PassageHistoryTotalPriceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SumTag?) {

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
        return countries?.size ?: 0
    }

    override fun onBindViewHolder(holder: CountryAdapterViewHolder, position: Int) {
        val current = countries?.get(position)

        holder.bind(current)
    }

}