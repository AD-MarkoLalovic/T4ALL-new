package com.mobility.enp.view.adapters.tool_history.select

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.databinding.ToolHistoryTagsAdapterBinding

class ToolHistoryTagsAdapter(private val listOfTags: ArrayList<Tag>, tagInterface: TagSend) :
    RecyclerView.Adapter<ToolHistoryTagsAdapter.ToolHistoryTagsViewHolder>() {

    val tagSendInt = tagInterface
    lateinit var context: Context

    inner class ToolHistoryTagsViewHolder(val binding: ToolHistoryTagsAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: Tag) {
            if (tag.registrationPlate.isNullOrEmpty()) { // ignore recommendation android studio is wrong here
                tag.registrationPlate = "No Api Data"
            }
            binding.data = tag

            binding.checkbox.setOnClickListener {
                if (binding.checkbox.isChecked) {
                    setCheckboxColors(tag)
                    tagSendInt.onSendTag(tag)
                } else {
                    setCheckboxColors(tag)
                    tagSendInt.onTagRemove(tag)
                }
            }

            binding.executePendingBindings()
        }

        private fun setCheckboxColors(tag: Tag) {
            if (binding.checkbox.isChecked) {
                binding.checkbox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.figmaSplashScreenColor))
                binding.regPlate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.figmaSplashScreenColor))
                binding.serialNumber.setTextColor(ContextCompat.getColor(binding.root.context, R.color.figmaSplashScreenColor))
            } else {
                binding.checkbox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.primary_light_dark))
                binding.regPlate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.primary_light_dark))
                binding.serialNumber.setTextColor(ContextCompat.getColor(binding.root.context, R.color.primary_light_dark))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolHistoryTagsViewHolder {
        context = parent.context
        return ToolHistoryTagsViewHolder(
            ToolHistoryTagsAdapterBinding.inflate(
                LayoutInflater.from(
                    context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return listOfTags.size
    }

    override fun onBindViewHolder(holder: ToolHistoryTagsViewHolder, position: Int) {
        val tag = listOfTags[holder.bindingAdapterPosition]
        holder.bind(tag)
    }

    interface TagSend {
        fun onSendTag(tag: Tag)
        fun onTagRemove(tag: Tag)
    }

}