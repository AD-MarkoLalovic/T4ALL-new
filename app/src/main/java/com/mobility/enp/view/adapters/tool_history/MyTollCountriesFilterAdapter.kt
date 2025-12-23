package com.mobility.enp.view.adapters.tool_history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.ItemTagStatusBinding

class MyTollCountriesFilterAdapter(
    private var onSelected: (String) -> Unit
) :
    ListAdapter<String, MyTollCountriesFilterAdapter.StatusTagViewHolder>(DIFF_CALLBACK) {
    private var selectedStatus = 0

    fun setStatus(position: Int) {
        clearStatus()
        selectedStatus = position
        notifyItemChanged(position)
    }

    fun getTabPosition(): Int {
        return selectedStatus
    }

    fun setTabPosition(position: Int) {
        this.selectedStatus = position
    }

    fun performClick(position: Int) {
        if (position in 0 until itemCount && position != selectedStatus) {
            val oldPosition = selectedStatus
            selectedStatus = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedStatus)

            onSelected(getItem(position))
        }
    }

    inner class StatusTagViewHolder(private val binding: ItemTagStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(status: String, isSelected: Boolean) {
            binding.tagStatus.text = status

            if (isSelected) {
                binding.tagStatus.setBackgroundResource(R.drawable.rounded_status_marked_border)
                binding.tagStatus.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.white
                    )
                )
            } else {
                binding.tagStatus.setBackgroundResource(R.drawable.rounded_status_unmarked_border_)
                binding.tagStatus.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.primary_light_dark
                    )
                )
            }

            binding.tagStatus.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION && bindingAdapterPosition != selectedStatus) {
                    val oldPosition = selectedStatus
                    selectedStatus = bindingAdapterPosition
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedStatus)

                    onSelected(status)
                }
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StatusTagViewHolder {
        return StatusTagViewHolder(
            ItemTagStatusBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: StatusTagViewHolder,
        position: Int
    ) {
        val status = getItem(position)
        holder.bind(status, position == selectedStatus)
    }

    fun clearStatus() {
        val oldPosition = selectedStatus
        selectedStatus = 5000
        notifyItemChanged(oldPosition)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }
    }

}


