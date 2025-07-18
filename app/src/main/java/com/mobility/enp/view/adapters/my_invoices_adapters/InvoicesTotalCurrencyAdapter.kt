package com.mobility.enp.view.adapters.my_invoices_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_my_invoices.refactor.TotalCurrency
import com.mobility.enp.databinding.ItemTotalCurrencyBinding

class InvoicesTotalCurrencyAdapter(val totalCurrency: List<TotalCurrency> = arrayListOf()) :
    RecyclerView.Adapter<InvoicesTotalCurrencyAdapter.TotalCurrencyViewModel>() {

    class TotalCurrencyViewModel(val binding: ItemTotalCurrencyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(totalCurrency: TotalCurrency) {
            binding.totalCurrency = totalCurrency

            totalCurrency.isPaid?.let {
                if (it) {
                    binding.containerTotalCurrency.setBackgroundResource(R.drawable.border_home_paid)
                } else {
                    binding.containerTotalCurrency.setBackgroundResource(R.drawable.border_home_unpaid)
                }
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TotalCurrencyViewModel {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemTotalCurrencyBinding.inflate(layoutInflater, parent, false)
        return TotalCurrencyViewModel(binding)
    }

    override fun getItemCount(): Int = totalCurrency.size

    override fun onBindViewHolder(holder: TotalCurrencyViewModel, position: Int) {
        val current = totalCurrency[holder.bindingAdapterPosition]
        holder.bind(current)
    }
}