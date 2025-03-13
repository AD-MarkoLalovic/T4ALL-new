package com.mobility.enp.view.adapters.home

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.franchise.FranchiseModel
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.databinding.CardFlagsPromotionHomeBinding

class HomePromotionsAdapter(
    private var list: List<HomeCardsEntity>,
    private val onItemClicked: () -> Unit,
    private val updateDeleteCard: (HomeCardsEntity) -> Unit
) :
    RecyclerView.Adapter<HomePromotionsAdapter.HomeInvoicesAdapterViewHolder>() {

    lateinit var franchiseModel: FranchiseModel

    inner class HomeInvoicesAdapterViewHolder(
        val binding: CardFlagsPromotionHomeBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: HomeCardsEntity) {
            binding.data = card


            if (::franchiseModel.isInitialized) {
                franchiseModel.franchisePrimaryColor.let {
                    binding.btnObjection.backgroundTintList = ColorStateList.valueOf(franchiseModel.franchisePrimaryColor)
                }
            }


            when (card.code) {
                "RS" -> binding.backgroundImage.setImageResource(R.drawable.serbian_flag_home)
                "MK" -> binding.backgroundImage.setImageResource(R.drawable.flag_home_macedonian)
                "ME" -> binding.backgroundImage.setImageResource(R.drawable.flag_home_crna_gora)
            }

            binding.btnObjection.setOnClickListener {
                onItemClicked()
            }

            binding.closeButton.setOnClickListener {
                card.deletedByUser = true
                card.time = System.currentTimeMillis()
                updateDeleteCard(card)

                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    list = list.toMutableList().apply { removeAt(position) }
                    notifyItemRemoved(position)
                }
            }

            binding.executePendingBindings()
        }
    }

    fun updateColor(franchiseModel: FranchiseModel) {
        this.franchiseModel = franchiseModel
        notifyDataSetChanged()
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