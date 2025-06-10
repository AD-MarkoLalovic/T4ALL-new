package com.mobility.enp.view.adapters.my_tags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.ItemMyTagsBinding
import com.mobility.enp.view.ui_models.my_tags.TagUiModel

class MyTagsListAdapter :
    ListAdapter<TagUiModel, MyTagsListAdapter.MyTagViewHolder>(DIFF_CALLBACK) {

    inner class MyTagViewHolder(private val binding: ItemMyTagsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val adapterCountryStatus = MyTagsCountryStatusesAdapter()

        init {
            binding.cyclerTagStatuses.adapter = adapterCountryStatus
        }

        fun bind(tag: TagUiModel) {
            binding.data = tag

            if (tag.showButtonLostTag == true) {
                binding.buttonLostTag.visibility = View.GONE
                binding.buttonFoundTag.visibility = View.VISIBLE
            } else {
                binding.buttonLostTag.visibility = View.VISIBLE
                binding.buttonFoundTag.visibility = View.GONE
            }

            if (tag.registrationPlate.isNullOrEmpty()) {
                binding.txTable.text = binding.root.context.getString(R.string.serial_number)
                binding.txTable.setTextAppearance(R.style.CaptionRegular)
            } else {
                binding.txTable.text = tag.registrationPlate
                binding.txTable.setTextAppearance(R.style.SubtitlesRegular)
                binding.txTable.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.primary_light_darkest
                    )
                )
            }

            val reversedList = tag.statuses.reversed()
            adapterCountryStatus.submitList(reversedList)

            binding.executePendingBindings()

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTagViewHolder {
        return MyTagViewHolder(
            ItemMyTagsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyTagViewHolder, position: Int) {
        val currentTag = getItem(position)
        holder.bind(currentTag)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TagUiModel>() {
            override fun areItemsTheSame(oldItem: TagUiModel, newItem: TagUiModel): Boolean {
                return oldItem.serialNumber == newItem.serialNumber
            }

            override fun areContentsTheSame(oldItem: TagUiModel, newItem: TagUiModel): Boolean {
                return oldItem == newItem
            }

        }
    }
}