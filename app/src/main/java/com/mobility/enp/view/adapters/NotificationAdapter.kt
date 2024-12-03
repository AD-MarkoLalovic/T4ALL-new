package com.mobility.enp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobility.enp.R
import com.mobility.enp.data.model.notification.NotificationModel
import com.mobility.enp.databinding.NotificationCyclerCardBinding

class NotificationAdapter :
    RecyclerView.Adapter<NotificationAdapter.NotificationAdapterViewHolder> {

    private var list: ArrayList<NotificationModel> = arrayListOf()
    private lateinit var context: Context

    constructor() : super()

    constructor(list: ArrayList<NotificationModel>, context: Context) : super() {
        this.list = list
        this.context = context
        notifyDataSetChanged()
    }

    class NotificationAdapterViewHolder(val binding: NotificationCyclerCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notificationModel: NotificationModel, context: Context, lastElement: Boolean) {
            binding.notification = notificationModel
            Glide.with(context).load(
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.notification_icon,
                    null
                )
            ).into(binding.imageView)
            if (lastElement) {
                binding.seperator.visibility = View.GONE
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapterViewHolder {
        return NotificationAdapterViewHolder(
            NotificationCyclerCardBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: NotificationAdapterViewHolder, position: Int) {
        val current: NotificationModel = list[holder.bindingAdapterPosition]
        val lastItem = (list[list.size - 1] == current)
        holder.bind(current, context, lastItem)
    }

}