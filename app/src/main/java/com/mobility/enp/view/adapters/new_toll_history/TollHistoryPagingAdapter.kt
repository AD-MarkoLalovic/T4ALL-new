package com.mobility.enp.view.adapters.new_toll_history

import android.view.LayoutInflater
import android.view.ViewGroup
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
            VIEW_TYPE_PASSAGE -> {
                PassageViewHolder(ItemNewTollHistoryBinding.inflate(inflater, parent, false))
            }

            VIEW_TYPE_HEADER -> {
                TagHeaderViewHolder(ItemTagHeaderBinding.inflate(inflater, parent, false))
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
        when (holder) {
            is PassageViewHolder -> {
                (item as? TollHistoryListItem.PassageItem)?.let { holder.bind(it.passage) }
            }

            is TagHeaderViewHolder -> {
                (item as? TollHistoryListItem.TagHeader)?.let { holder.bind(it) }
            }

            is GroupEndViewHolder -> Unit
        }
    }

    class TagHeaderViewHolder(private val binding: ItemTagHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(header: TollHistoryListItem.TagHeader) {
            binding.tvSerialNumberValue.text = header.tagSerialNumber
            binding.tvTotalAmount.text = header.total
            binding.tvCurrency.text = header.currency
        }

    }

    inner class PassageViewHolder(private val binding: ItemNewTollHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TollHistoryItemUi) {
            binding.tvPassageInvoiceNumber.text = item.billFinal
            binding.tvPassageTotalAmount.text = item.amountDisplay
            binding.tvPassageCurrency.text = item.currencyDisplay
            binding.tvRoute.text = item.tollPlaza
            binding.tvCheckIn.text = item.checkInFormatted
            binding.tvCheckOut.text = item.checkOutFormatted

            val hasComplaint = item.complaintId != null

            binding.btnPassageComplaint.isVisible = !hasComplaint
            binding.layoutObjection.isVisible = hasComplaint

            if (hasComplaint) {
                binding.tvComplaintId.text = binding.root.context.getString(
                    R.string.complaint_number_label, item.complaintId
                )
                binding.tvObjectionCount.apply {
                    val count = item.objectionCount
                    isVisible = count > 0
                    if (count > 0) {
                        text = count.toString()
                    }
                }
                binding.btnPassageObjection.setOnClickListener {
                    onObjectionClick(
                        item.complaintId,
                        item.maxObjectionsReached
                    )
                }
            } else {
                binding.btnPassageComplaint.setOnClickListener { onComplaintClick(item.id) }
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
                                oldItem.tagSerialNumber == newItem.tagSerialNumber

                    is TollHistoryListItem.PassageItem -> {
                        newItem is TollHistoryListItem.PassageItem &&
                                oldItem.passage.id == newItem.passage.id
                    }

                    is TollHistoryListItem.GroupEnd ->
                        newItem is TollHistoryListItem.GroupEnd
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