package com.mobility.enp.view.adapters.home

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.franchise.FranchiseModel
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.databinding.CardFlagsPromotionHomeBinding

class HomePromotionsAdapter(
    private val onItemClicked: (HomeCardsEntity) -> Unit,
    private val onDeleteClicked: (HomeCardsEntity) -> Unit,
    private val franchiseModel: FranchiseModel?
) : ListAdapter<HomeCardsEntity, HomePromotionsAdapter.HomeInvoicesAdapterViewHolder>(
    HomePromotionsDiffCallback()
) {

    inner class HomeInvoicesAdapterViewHolder(
        val binding: CardFlagsPromotionHomeBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: HomeCardsEntity) {
            binding.data = card


            franchiseModel?.franchisePrimaryColor?.let {
                binding.btnObjection.backgroundTintList =
                    ColorStateList.valueOf(it)
            }

            when (card.code) {
                "RS" -> binding.backgroundImage.setImageResource(R.drawable.serbian_flag_home)
                "MK" -> binding.backgroundImage.setImageResource(R.drawable.flag_home_macedonian)
                "ME" -> binding.backgroundImage.setImageResource(R.drawable.flag_home_crna_gora)
            }

            binding.btnObjection.setOnClickListener {
                onItemClicked(card)
            }

            binding.closeButton.setOnClickListener {
                card.deletedByUser = true
                card.time = System.currentTimeMillis()
                onDeleteClicked(card)

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

    override fun onBindViewHolder(holder: HomeInvoicesAdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HomePromotionsDiffCallback : DiffUtil.ItemCallback<HomeCardsEntity>() {
        override fun areItemsTheSame(oldItem: HomeCardsEntity, newItem: HomeCardsEntity): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(
            oldItem: HomeCardsEntity,
            newItem: HomeCardsEntity
        ): Boolean {
            return oldItem == newItem
        }

    }

}