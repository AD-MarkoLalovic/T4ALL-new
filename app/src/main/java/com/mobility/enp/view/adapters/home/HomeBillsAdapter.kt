package com.mobility.enp.view.adapters.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.data.model.api_home_page.homedata.Invoice
import com.mobility.enp.databinding.HomeInvoicesCardBinding
import com.mobility.enp.view.adapters.TotalCurrencyAdapter

class HomeBillsAdapter(
    private val list: List<Invoice>?,
    private var switchToPage: () -> Unit
) :
    RecyclerView.Adapter<HomeBillsAdapter.HomeInvoicesAdapterViewHolder>() {

    inner class HomeInvoicesAdapterViewHolder(
        val binding: HomeInvoicesCardBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(billModel: Invoice) {
            binding.data = billModel

           /* billModel.totalCurrency.let {
                    val totalCurrencyAdapter = TotalCurrencyAdapter(it)
                binding.cycler.adapter = totalCurrencyAdapter
            }*/

            binding.imageView.setOnClickListener {
                switchToPage
            }


            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeInvoicesAdapterViewHolder {
        return HomeInvoicesAdapterViewHolder(
            HomeInvoicesCardBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: HomeInvoicesAdapterViewHolder, position: Int) {
        val current = list?.get(holder.bindingAdapterPosition)
        if (current != null) {
            holder.bind(current)
        }

    }



    interface AdapterSwitchToPage {
        fun switchToBills()
        fun switchToInvoices()
    }
}