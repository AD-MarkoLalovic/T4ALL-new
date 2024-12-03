package com.mobility.enp.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_home_page.homedata.TotalCurrency
import com.mobility.enp.databinding.CardHomePageCurrencyBinding

class TotalCurrencyAdapter(list: List<TotalCurrency>) :
    RecyclerView.Adapter<TotalCurrencyAdapter.TotalCurrencyViewHolder>() {

    private val totalCurrencyList: List<TotalCurrency>

    init {
        this.totalCurrencyList = list
    }

    class TotalCurrencyViewHolder(val binding: CardHomePageCurrencyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: TotalCurrency) {
            binding.totalCurrencyItem = data

            data.isPaid?.let {
                if (it) {
                    binding.txtData.setBackgroundResource(R.drawable.border_home_paid)
                } else {
                    binding.txtData.setBackgroundResource(R.drawable.border_home_unpaid)
                }
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TotalCurrencyViewHolder {
        return TotalCurrencyViewHolder(
            CardHomePageCurrencyBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun getItemCount(): Int {
        return totalCurrencyList.size
    }

    override fun onBindViewHolder(holder: TotalCurrencyViewHolder, position: Int) {
        val currentItem = totalCurrencyList[holder.bindingAdapterPosition]
        holder.bind(currentItem)
    }

}