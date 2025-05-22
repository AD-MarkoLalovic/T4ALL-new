package com.mobility.enp.view.adapters.cards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.cards.registration_croatia.SerialNumberRequest
import com.mobility.enp.databinding.ItemTagForCroatiaBinding
import com.mobility.enp.view.ui_models.TagsForCroatiaUI

class TagsForCroatiaAdapter(
    private val onCheckChange: (SerialNumberRequest) -> Unit
) : ListAdapter<TagsForCroatiaUI, TagsForCroatiaAdapter.TagsViewHolder>(TagsForCroatiaDiffCallback()) {

    private val serialNumbers = mutableListOf<String>()

    inner class TagsViewHolder(private val binding: ItemTagForCroatiaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tag: TagsForCroatiaUI) {
            binding.serialNumberTextView.text = tag.serialNumberUI
            binding.registrationPlateTextView.text = tag.registrationPlateUI

            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked = tag.selected

            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val color = ContextCompat.getColor(binding.root.context, R.color.figmaSplashScreenColor)
                    binding.serialNumberTextView.setTextColor(color)
                    binding.registrationPlateTextView.setTextColor(color)

                    serialNumbers.add(tag.serialNumberUI)
                } else {
                    val color = ContextCompat.getColor(binding.root.context, R.color.primary_light_dark)
                    binding.serialNumberTextView.setTextColor(color)
                    binding.registrationPlateTextView.setTextColor(color)

                    serialNumbers.remove(tag.serialNumberUI)
                }

                onCheckChange(SerialNumberRequest(serialNumbers))
            }
        }
    }

    class TagsForCroatiaDiffCallback : DiffUtil.ItemCallback<TagsForCroatiaUI>() {
        override fun areItemsTheSame(
            oldItem: TagsForCroatiaUI,
            newItem: TagsForCroatiaUI
        ): Boolean {
            return oldItem.serialNumberUI == newItem.serialNumberUI
        }

        override fun areContentsTheSame(
            oldItem: TagsForCroatiaUI,
            newItem: TagsForCroatiaUI
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsViewHolder {
        val binding = ItemTagForCroatiaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

}