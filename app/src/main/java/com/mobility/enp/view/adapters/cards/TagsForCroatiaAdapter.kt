package com.mobility.enp.view.adapters.cards

import android.content.res.ColorStateList
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
    private val onCheckChange: (SerialNumberRequest) -> Unit,
    private val franchisePrimaryColor: Int?
) : ListAdapter<TagsForCroatiaUI, TagsForCroatiaAdapter.TagsViewHolder>(TagsForCroatiaDiffCallback()) {

    private val serialNumbers = mutableListOf<String>()

    inner class TagsViewHolder(private val binding: ItemTagForCroatiaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tag: TagsForCroatiaUI) {
            binding.serialNumberTextView.text = tag.serialNumberUI
            binding.registrationPlateTextView.text = tag.registrationPlateUI

            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked = tag.selected

            updateColors(tag.selected)

            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                updateColors(isChecked)

                if (isChecked) {
                    serialNumbers.add(tag.serialNumberUI)
                } else {
                    serialNumbers.remove(tag.serialNumberUI)
                }

                onCheckChange(SerialNumberRequest(serialNumbers))
            }
        }

        private fun updateColors(isChecked: Boolean) {
            val colorStateList = franchisePrimaryColor?.let {
                if (isChecked) ColorStateList.valueOf(it)
                else ContextCompat.getColorStateList(
                    binding.root.context,
                    R.color.primary_light_dark
                )
            } ?: run {
                if (isChecked) {
                    ContextCompat.getColorStateList(
                        binding.root.context,
                        R.color.figmaSplashScreenColor
                    )
                } else {
                    ContextCompat.getColorStateList(
                        binding.root.context,
                        R.color.primary_light_dark
                    )
                }
            }

            binding.registrationPlateTextView.setTextColor(colorStateList)
            binding.serialNumberTextView.setTextColor(colorStateList)
            binding.checkBox.buttonTintList = colorStateList
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
        val binding =
            ItemTagForCroatiaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }


}