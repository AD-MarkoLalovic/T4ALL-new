package com.mobility.enp.view.adapters.refund_request_adapters.diff_util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.ItemRefundRequestCreatedBinding
import com.mobility.enp.view.ui_models.refund_request.RefundRequestUIModel

class RefundRequestsCreatedAdapter :
    ListAdapter<RefundRequestUIModel, RefundRequestsCreatedAdapter.RefundRequestsViewHolder>(
        DiffCallback()
    ) {

    class RefundRequestsViewHolder(
        val binding: ItemRefundRequestCreatedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(refundRequest: RefundRequestUIModel) {
            binding.refundRequest = refundRequest

            val context = binding.root.context
            val colorRes = when (refundRequest.statusValue) {
                1 -> R.color.primary_light_light
                2 -> R.color.primary_light_orange
                3 -> R.color.figmaToolHistoryPaidBackground
                else -> R.color.transparent
            }

            binding.firstContainerRefund.setBackgroundResource(colorRes)
            binding.refundCardView.strokeColor = ContextCompat.getColor(context, colorRes)

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefundRequestsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRefundRequestCreatedBinding.inflate(inflater, parent, false)
        return RefundRequestsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RefundRequestsViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class DiffCallback : DiffUtil.ItemCallback<RefundRequestUIModel>() {

        override fun areItemsTheSame(
            oldItem: RefundRequestUIModel,
            newItem: RefundRequestUIModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: RefundRequestUIModel,
            newItem: RefundRequestUIModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}