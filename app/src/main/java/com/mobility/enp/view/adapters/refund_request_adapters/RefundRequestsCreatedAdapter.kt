package com.mobility.enp.view.adapters.refund_request_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.ItemRefundRequestCreatedBinding
import com.mobility.enp.view.ui_models.refund_request.RefundRequestUIModel

class RefundRequestsCreatedAdapter(private var refundRequestList: List<RefundRequestUIModel>) :
    RecyclerView.Adapter<RefundRequestsCreatedAdapter.RefundRequestsViewHolder>() {

    class RefundRequestsViewHolder(val binding: ItemRefundRequestCreatedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(refundRequest: RefundRequestUIModel) {
            binding.refundRequest = refundRequest
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefundRequestsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRefundRequestCreatedBinding.inflate(layoutInflater, parent, false)
        return RefundRequestsViewHolder(binding)
    }

    override fun getItemCount() = refundRequestList.size

    override fun onBindViewHolder(holder: RefundRequestsViewHolder, position: Int) {
        val current = refundRequestList[position]
        when (current.statusValue) {
            1 -> {
                holder.binding.firstContainerRefund.setBackgroundResource(R.color.primary_light_light)
                holder.binding.refundCardView.strokeColor =
                    ContextCompat.getColor(holder.binding.root.context, R.color.primary_light_light)
            }

            2 -> {
                holder.binding.firstContainerRefund.setBackgroundResource(R.color.primary_light_orange)
                holder.binding.refundCardView.strokeColor = ContextCompat.getColor(
                    holder.binding.root.context,
                    R.color.primary_light_orange
                )
            }

            3 -> {
                holder.binding.firstContainerRefund.setBackgroundResource(R.color.figmaToolHistoryPaidBackground)
                holder.binding.refundCardView.strokeColor = ContextCompat.getColor(
                    holder.binding.root.context,
                    R.color.figmaToolHistoryPaidBackground
                )
            }
        }
        holder.bind(current)
    }

}

