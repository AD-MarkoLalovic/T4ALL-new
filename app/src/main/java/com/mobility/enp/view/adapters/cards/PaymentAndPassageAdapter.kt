package com.mobility.enp.view.adapters.cards

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.cards.response.Card
import com.mobility.enp.databinding.ItemPaymentAndPassagesBinding
import com.mobility.enp.viewmodel.FranchiseViewModel

class PaymentAndPassageAdapter(
    private val cards: ArrayList<Card> = arrayListOf(),
    private val listener: PrimaryCardListener,
    private val franchiseVm: FranchiseViewModel
) :
    RecyclerView.Adapter<PaymentAndPassageAdapter.PaymentAndPassageViewHolder>() {

    inner class PaymentAndPassageViewHolder(val binding: ItemPaymentAndPassagesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: Card) {
            binding.paymentAndPassage = card

            // Postavljanje za neaktivne i aktivne kartice
            if (card.active == 0) {
                setCardInactive()
            } else {
                setCardActive()
            }

            // Logika za primarnu karticu
            if (card.defaultCard == 1) {
                setPrimaryCardStyle()
            } else {
                setNonPrimaryCardStyle(card)
            }

            binding.executePendingBindings()
        }

        private fun setCardInactive() {
            binding.inactiveCard.visibility = View.VISIBLE
            binding.itemCard.backgroundTintList = AppCompatResources.getColorStateList(
                binding.itemCard.context,
                R.color.very_light_gray
            )
            binding.horizontalLinePayment.visibility = View.INVISIBLE
            binding.primaryCard.visibility = View.GONE
            binding.removeCard.visibility = View.GONE
            binding.cardFlag.visibility = View.GONE
        }

        private fun setCardActive() {
            binding.inactiveCard.visibility = View.INVISIBLE
            binding.itemCard.backgroundTintList = AppCompatResources.getColorStateList(
                binding.itemCard.context,
                R.color.primary_light_lightest
            )
            binding.horizontalLinePayment.visibility = View.VISIBLE
            binding.primaryCard.visibility = View.VISIBLE
            binding.removeCard.visibility = View.VISIBLE
            binding.cardFlag.visibility = View.VISIBLE
        }

        private fun setPrimaryCardStyle() {
            binding.primaryCard.text = binding.root.context.getString(R.string.primary_card)
            binding.primaryCard.setTextColor(
                ContextCompat.getColor(binding.root.context, R.color.primary_light_dark)
            )
            binding.removeCard.visibility = View.GONE
            binding.primaryCard.isClickable = false
        }

        private fun setNonPrimaryCardStyle(card: Card) {
            val context = binding.root.context
            val text = context.getString(R.string.choose_primary_card)

            var color =
                franchiseVm.franchiseModel.value?.franchisePrimaryColor
                    ?: context.resources.getColor(R.color.figmaSplashScreenColor, null)

            val spannableString = SpannableString(text).apply {
                setSpan(UnderlineSpan(), 0, text.length, 0)
                setSpan(
                    ForegroundColorSpan(
                        color
                    ), 0, text.length, 0
                )
            }

            binding.primaryCard.text = spannableString
            binding.primaryCard.isClickable = true
            binding.primaryCard.setOnClickListener {
                card.id?.let { id -> listener?.setPrimaryCard(id) }
            }

            if (card.active == 0) {
                binding.removeCard.visibility = View.GONE
            } else {
                binding.removeCard.visibility = View.VISIBLE
                binding.removeCard.setOnClickListener {
                    card.id?.let { id -> listener?.clickRemoveCard(id.toString()) }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentAndPassageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPaymentAndPassagesBinding.inflate(layoutInflater, parent, false)
        return PaymentAndPassageViewHolder(binding)
    }

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: PaymentAndPassageViewHolder, position: Int) {
        val current = cards[holder.bindingAdapterPosition]

        // Postavljanje slike kartice na temelju cardType
        when (current.cardType) {
            "VISA" -> holder.binding.cardType.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.root.context,
                    R.drawable.ic_visa
                )
            )

            "DINA", "NATIONAL" -> holder.binding.cardType.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.root.context,
                    R.drawable.ic_dina_card
                )
            )

            "MASTERCARD", "MC" -> holder.binding.cardType.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.root.context,
                    R.drawable.ic_mastercard
                )
            )

            else -> holder.binding.cardFlag.setImageDrawable(null)
        }

        // Postavljanje zastave na temelju countryCode
        when (current.country?.code) {
            "MK" -> holder.binding.cardFlag.setImageResource(R.drawable.macedonia_flag)
            "RS" -> holder.binding.cardFlag.setImageResource(R.drawable.serbia_flag)
            "ME" -> holder.binding.cardFlag.setImageResource(R.drawable.montenegro_flag)
            else -> holder.binding.cardFlag.setImageDrawable(null) //
        }
        holder.bind(current)
    }

    fun updateListCards(newCard: List<Card>) {
        cards.clear()
        cards.addAll(newCard)
        notifyDataSetChanged()
        Log.d("PaymentAndPassageAdapter", "List updated: $cards")
    }

    interface PrimaryCardListener {
        fun setPrimaryCard(cardId: Int)
        fun clickRemoveCard(cardId: String)
    }

}