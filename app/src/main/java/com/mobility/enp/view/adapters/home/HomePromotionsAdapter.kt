package com.mobility.enp.view.adapters.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobility.enp.R
import com.mobility.enp.data.model.api_home_page.homedata.Promotion
import com.mobility.enp.databinding.CardFlagsPromotionHomeBinding

class HomePromotionsAdapter(
    val list: List<Promotion>,
    private val onItemClicked: (Promotion) -> Unit,
    private val upsertPromotion: (Promotion) -> Unit,
    private val isSerbiaAdded: () -> Boolean
) :
    RecyclerView.Adapter<HomePromotionsAdapter.HomeInvoicesAdapterViewHolder>() {

    inner class HomeInvoicesAdapterViewHolder(
        val binding: CardFlagsPromotionHomeBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(promotion: Promotion) {
            binding.data = promotion

            when (promotion.countryCode) {
                "RS" -> Glide.with(binding.root.context).load(R.drawable.serbian_flag_home)
                    .into(binding.backgroundImage)

                "MK" -> Glide.with(binding.root.context).load(R.drawable.flag_home_macedonian)
                    .into(binding.backgroundImage)

                "ME" -> Glide.with(binding.root.context).load(R.drawable.flag_home_crna_gora)
                    .into(binding.backgroundImage)
            }

            binding.btnObjection.setOnClickListener {
                onItemClicked(promotion)
            }

            binding.closeButton.setOnClickListener {
                promotion.deletedByUser = true
                promotion.time = System.currentTimeMillis()
                upsertPromotion(promotion)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeInvoicesAdapterViewHolder {
        return HomeInvoicesAdapterViewHolder(
            CardFlagsPromotionHomeBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: HomeInvoicesAdapterViewHolder, position: Int) {
        val current = list[holder.bindingAdapterPosition]
        holder.bind(current)
    }

}