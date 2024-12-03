package com.mobility.enp.view.adapters.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.data.model.api_home_page.homedata.Invoice
import com.mobility.enp.databinding.HomeInvoicesCardBinding
import com.mobility.enp.view.adapters.TotalCurrencyAdapter

class HomeBillsAdapter(
    list: List<Invoice>?,
    adapterSwitchToPage: AdapterSwitchToPage,
    context: Context
) :
    RecyclerView.Adapter<HomeBillsAdapter.HomeInvoicesAdapterViewHolder>() {

    private var arrayList: List<Invoice>?
    private var adapterSwitchToPage: AdapterSwitchToPage
    private var context: Context

    init {
        this.arrayList = list
        this.adapterSwitchToPage = adapterSwitchToPage
        this.context = context
    }

    class HomeInvoicesAdapterViewHolder(
        val binding: HomeInvoicesCardBinding,
        adapterSwitchToPage: AdapterSwitchToPage
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.imageView.setOnClickListener {
                adapterSwitchToPage.switchToBills()
            }
        }

        fun bind(billModel: Invoice, context: Context) {
            binding.data = billModel
            binding.executePendingBindings()
            billModel.totalCurrency.let {
                val totalCurrencyAdapter = TotalCurrencyAdapter(it)
                binding.cycler.adapter = totalCurrencyAdapter
                binding.cycler.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
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
            ), adapterSwitchToPage
        )
    }

    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }

    override fun onBindViewHolder(holder: HomeInvoicesAdapterViewHolder, position: Int) {
        val current = arrayList?.get(holder.bindingAdapterPosition)
        if (current != null) {
            holder.bind(current, context)
        }
    }

    interface AdapterSwitchToPage {
        fun switchToBills()
        fun switchToInvoices()
    }
}