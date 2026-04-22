package com.mobility.enp.view.adapters.new_toll_history

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.ItemGroupEndBinding
import com.mobility.enp.databinding.ItemNewTollHistoryBinding
import com.mobility.enp.databinding.ItemTagHeaderBinding
import com.mobility.enp.view.ui_models.toll_history.TollHistoryItemUi
import com.mobility.enp.view.ui_models.toll_history.TollHistoryListItem

class TollHistoryPagingAdapter(
    private val onComplaintClick: (itemId: Int) -> Unit,
    private val onObjectionClick: (complaintId: Int, maxReached: Boolean) -> Unit
) : PagingDataAdapter<TollHistoryListItem, RecyclerView.ViewHolder>(DiFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TollHistoryListItem.TagHeader -> VIEW_TYPE_HEADER
            is TollHistoryListItem.PassageItem -> VIEW_TYPE_PASSAGE
            is TollHistoryListItem.GroupEnd, null -> VIEW_TYPE_GROUP_END
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                TagHeaderViewHolder(ItemTagHeaderBinding.inflate(inflater, parent, false))
            }

            VIEW_TYPE_PASSAGE -> {
                PassageViewHolder(
                    ItemNewTollHistoryBinding.inflate(inflater, parent, false),
                    onComplaintClick = onComplaintClick,
                    onObjectionClick = onObjectionClick
                )
            }

            VIEW_TYPE_GROUP_END -> {
                GroupEndViewHolder(ItemGroupEndBinding.inflate(inflater, parent, false))
            }

            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        Log.d("MARKO 1", "bind position=$position item=$item")
        // Redosled onBind poziva ≠ redosled u listi: getItem(0) je uvek "glava" liste u adapteru.
        Log.d(
            "MARKO 0",
            "bind pos=$position listHead=${getItem(0)} holder=${holder.javaClass.simpleName}"
        )
        when (holder) {
            is TagHeaderViewHolder -> {
                (item as? TollHistoryListItem.TagHeader)?.let { holder.bind(it) }
            }

            is PassageViewHolder -> {
                (item as? TollHistoryListItem.PassageItem)?.let { holder.bind(it.passage) }
            }

            is GroupEndViewHolder -> Unit
        }
    }

    class TagHeaderViewHolder(private val binding: ItemTagHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(header: TollHistoryListItem.TagHeader) = with(binding) {
            tvSerialNumberValue.text = header.tagSerialNumber

            val hasTotal = header.total.isNotBlank()
            tvTotalLabel.isVisible = hasTotal
            tvTotalAmount.isVisible = hasTotal
            tvTotalAmount.text = header.total

            val hasCurrency = header.currency.isNotBlank()
            tvCurrency.isVisible = hasCurrency
            tvCurrency.text = header.currency
        }

    }

    class PassageViewHolder(
        private val binding: ItemNewTollHistoryBinding,
        private val onComplaintClick: (itemId: Int) -> Unit,
        private val onObjectionClick: (complaintId: Int, maxReached: Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TollHistoryItemUi) = with(binding) {
            val context = root.context

            val (colorRes, iconRes) = when {
                !item.ticketUid.isNullOrBlank() -> {
                    R.color.primary_light_light to R.drawable.status_icon_light
                }

                item.isPaid -> {
                    R.color.figmaToolHistoryPaidBackground to R.drawable.status_icon_green
                }

                else -> {
                    R.color.figmaToolHistoryUnpaidBackground to R.drawable.status_icon_red
                }
            }

            val color = ContextCompat.getColor(context, colorRes)

            cardPassageContainer.strokeColor = color
            containerPassageAmount.setBackgroundColor(color)
            viewStatusIndicator.setImageResource(iconRes)

            val hasInvoice = item.billFinal.isNotBlank()
            tvPassageInvoiceNumber.isVisible = hasInvoice
            tvPassageInvoiceNumber.text = if (hasInvoice) item.billFinal else ""

            tvPassageTotalAmount.text = item.amountDisplay
            tvPassageCurrency.text =
                item.currencyDisplay.ifBlank { context.getString(R.string.euro_currency) }
            tvRoute.text = item.tollPlaza
            tvCheckIn.text = item.checkInFormatted.ifBlank { item.entryTime }
            tvCheckOut.text = item.checkOutFormatted.ifBlank { item.exitTime }

            val hasComplaint = item.complaintId != null

            btnPassageComplaint.isVisible = !hasComplaint
            layoutObjection.isVisible = hasComplaint

            btnPassageComplaint.setOnClickListener(null)
            btnPassageObjection.setOnClickListener(null)

            if (hasComplaint) {
                tvComplaintId.text = context.getString(
                    R.string.complaint_number_label,
                    item.complaintId
                )

                val count = item.objectionCount
                tvObjectionCount.isVisible = count > 0
                tvObjectionCount.text = if (count > 0) count.toString() else ""

                btnPassageObjection.setOnClickListener {
                    onObjectionClick(
                        item.complaintId,
                        item.maxObjectionsReached
                    )
                }
            } else {
                tvComplaintId.text = ""
                tvObjectionCount.isVisible = false
                tvObjectionCount.text = ""

                btnPassageComplaint.setOnClickListener {
                    onComplaintClick(item.id)
                }
            }
        }
    }

    class GroupEndViewHolder(binding: ItemGroupEndBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_PASSAGE = 1
        private const val VIEW_TYPE_GROUP_END = 2

        private val DiFF_CALLBACK = object : DiffUtil.ItemCallback<TollHistoryListItem>() {
            override fun areItemsTheSame(
                oldItem: TollHistoryListItem,
                newItem: TollHistoryListItem
            ): Boolean {
                return when (oldItem) {
                    is TollHistoryListItem.TagHeader ->
                        newItem is TollHistoryListItem.TagHeader &&
                                oldItem.uniqueKey == newItem.uniqueKey

                    is TollHistoryListItem.PassageItem -> {
                        newItem is TollHistoryListItem.PassageItem &&
                                oldItem.passage.id == newItem.passage.id
                    }

                    is TollHistoryListItem.GroupEnd ->
                        newItem is TollHistoryListItem.GroupEnd &&
                                oldItem.uniqueKey == newItem.uniqueKey
                }
            }

            override fun areContentsTheSame(
                oldItem: TollHistoryListItem,
                newItem: TollHistoryListItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}