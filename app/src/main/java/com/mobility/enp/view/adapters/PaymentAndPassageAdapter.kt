package com.mobility.enp.view.adapters

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
import com.mobility.enp.data.model.api_my_profile.cards.Card
import com.mobility.enp.databinding.ItemPaymentAndPassagesBinding
import com.mobility.enp.viewmodel.PaymentAndPassageViewModel

class PaymentAndPassageAdapter(
    private val cards: ArrayList<Card> = arrayListOf(),
    private val viewModel: PaymentAndPassageViewModel,
    private val listener: PrimaryCardListener
) :
    RecyclerView.Adapter<PaymentAndPassageAdapter.PaymentAndPassageViewHolder>() {

    inner class PaymentAndPassageViewHolder(val binding: ItemPaymentAndPassagesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: Card, viewModel: PaymentAndPassageViewModel) {
            binding.paymentAndPassage = card
            binding.viewModel = viewModel

            if (card.active == 0) {
                binding.inactiveCard.visibility = View.VISIBLE
                binding.itemCard.backgroundTintList = AppCompatResources.getColorStateList(
                    binding.itemCard.context,
                    R.color.very_light_gray
                )
                binding.horizontalLinePayment.visibility = View.INVISIBLE
                binding.primaryCard.visibility = View.GONE
                binding.removeCard.visibility = View.GONE
                binding.cardFlag.visibility = View.GONE
            } else {
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

            binding.executePendingBindings()

            if (card.defaultCard == 1) {
                binding.primaryCard.text = binding.root.context.getString(R.string.primary_card)
                binding.primaryCard.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.primary_light_dark
                    )
                )
                binding.removeCard.visibility = View.GONE
                binding.primaryCard.isClickable = false

                binding.executePendingBindings()
            } else {
                val text = binding.root.context.getString(R.string.choose_primary_card)
                val spannableString = SpannableString(text)

                spannableString.setSpan(UnderlineSpan(), 0, text.length, 0)

                val setTextColor = ContextCompat.getColor(
                    binding.root.context,
                    R.color.figmaSplashScreenColor
                )
                spannableString.setSpan(ForegroundColorSpan(setTextColor), 0, text.length, 0)

                binding.primaryCard.text = spannableString

                if (card.active == 0) {
                    binding.removeCard.visibility = View.GONE
                } else {
                    binding.removeCard.visibility = View.VISIBLE
                }

                binding.removeCard.setOnClickListener {
                    card.id?.let { id -> listener.clickRemoveCard(id.toString()) }
                }

                binding.primaryCard.isClickable = true

                binding.primaryCard.setOnClickListener {
                    Log.d("PrimaryCard", "card: ${card.id}")
                    card.id?.let { id -> listener.setPrimaryCard(id) }
                }
                binding.executePendingBindings()
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
        holder.bind(current, viewModel)
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