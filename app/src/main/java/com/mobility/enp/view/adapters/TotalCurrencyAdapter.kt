package com.mobility.enp.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.home.entity.InvoiceHomeTotalCurrencyEntity
import com.mobility.enp.databinding.CardHomePageCurrencyBinding

class TotalCurrencyAdapter(private var list: List<InvoiceHomeTotalCurrencyEntity>) :
    RecyclerView.Adapter<TotalCurrencyAdapter.TotalCurrencyViewHolder>() {

    class TotalCurrencyViewHolder(val binding: CardHomePageCurrencyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: InvoiceHomeTotalCurrencyEntity) {
            binding.txtData.text = data.totalAndCurrency

            data.isPaid.let {
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
        return list.size
    }

    override fun onBindViewHolder(holder: TotalCurrencyViewHolder, position: Int) {
        val currentItem = list[holder.bindingAdapterPosition]
        holder.bind(currentItem)
    }

    fun submitList(newList: List<InvoiceHomeTotalCurrencyEntity>) {
        list = newList
        notifyDataSetChanged()
    }

}