package com.mobility.enp.view.adapters.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobility.enp.R
import com.mobility.enp.data.model.api_home_page.homedata.Promotion
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.databinding.CardFlagsPromotionHomeBinding

class HomePromotionsAdapter(
    private val list: List<HomeCardsEntity>,
    private val onItemClicked: (HomeCardsEntity) -> Unit,
    private val upsertPromotion: (HomeCardsEntity) -> Unit
) :
    RecyclerView.Adapter<HomePromotionsAdapter.HomeInvoicesAdapterViewHolder>() {

    inner class HomeInvoicesAdapterViewHolder(
        val binding: CardFlagsPromotionHomeBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: HomeCardsEntity) {
            binding.data = card

            when (card.code) {
                "RS" -> Glide.with(binding.root.context).load(R.drawable.serbian_flag_home)
                    .into(binding.backgroundImage)

                "MK" -> Glide.with(binding.root.context).load(R.drawable.flag_home_macedonian)
                    .into(binding.backgroundImage)

                "ME" -> Glide.with(binding.root.context).load(R.drawable.flag_home_crna_gora)
                    .into(binding.backgroundImage)
            }

            binding.btnObjection.setOnClickListener {
                onItemClicked(card)
            }

            binding.closeButton.setOnClickListener {
                card.deletedByUser = true
                card.time = System.currentTimeMillis()
                upsertPromotion(card)
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