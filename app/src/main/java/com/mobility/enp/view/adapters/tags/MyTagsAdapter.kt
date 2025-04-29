package com.mobility.enp.view.adapters.tags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tags.Tag
import com.mobility.enp.data.model.api_tags.TagsResponse
import com.mobility.enp.databinding.ItemMyTagsBinding

class MyTagsAdapter(
    private var tagResponse: TagsResponse
) :
    RecyclerView.Adapter<MyTagsAdapter.MyTagsViewHolder>() {

    private lateinit var tagInterface: OnClickContent


    fun setInterface(tagInterface: OnClickContent) {
        this.tagInterface = tagInterface
    }

    companion object {
        const val TAG = "tagsContent"
    }

    inner class MyTagsViewHolder(val binding: ItemMyTagsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: Tag, tagInterface: OnClickContent) {
            binding.data = tag

            if (!tag.showButtonLostTag) {
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

            binding.buttonLostTag.setOnClickListener {
                tagInterface.reportLostTag(tag.serialNumber)
            }
            binding.buttonFoundTag.setOnClickListener {
                tagInterface.reportFoundTag(tag.serialNumber)
            }

            val adapterCountryStatus =
                AdapterCountryStatuses(tag.statuses) // set country Status adapter
            binding.cyclerTagStatuses.adapter = adapterCountryStatus

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTagsViewHolder {
        return MyTagsViewHolder(
            ItemMyTagsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = tagResponse.data.tags.size

    override fun onBindViewHolder(holder: MyTagsViewHolder, position: Int) {
        holder.bind(tagResponse.data.tags[holder.bindingAdapterPosition], tagInterface)
    }

    fun updateListTags(tagResponse: TagsResponse) {
        this.tagResponse = tagResponse
        notifyDataSetChanged()
    }

    interface OnClickContent {
        fun reportLostTag(tagSerial: String?)
        fun reportFoundTag(tagSerial: String?)
    }
}