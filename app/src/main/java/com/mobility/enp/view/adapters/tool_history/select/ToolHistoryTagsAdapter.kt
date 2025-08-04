package com.mobility.enp.view.adapters.tool_history.select

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.databinding.ToolHistoryTagsAdapterBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class ToolHistoryTagsAdapter(
    tagInterface: TagSend,
    private val franchiseModel: FranchiseViewModel,
    private val paginationUpdate: PaginationUpdate,
    private val toolHistoryIndex: IndexData
) :
    RecyclerView.Adapter<ToolHistoryTagsAdapter.ToolHistoryTagsViewHolder>() {

    val tagSendInt = tagInterface
    private val listOfTags: ArrayList<Tag> = toolHistoryIndex.data?.tags as ArrayList<Tag>
    var currentPage: Int = toolHistoryIndex.data?.currentPage ?: 0
    val lastPage: Int = toolHistoryIndex.data?.lastPage ?: 0
    val perPage: Int = toolHistoryIndex.data?.perPage ?: 0
    val total: Int = toolHistoryIndex.data?.total ?: 0

    lateinit var context: Context

    inner class ToolHistoryTagsViewHolder(val binding: ToolHistoryTagsAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: Tag) {
            if (tag.registrationPlate.isNullOrEmpty()) { // ignore recommendation android studio is wrong here
                tag.registrationPlate = tag.serialNumber
                binding.serialNumber.visibility = View.INVISIBLE
            } else {
                binding.serialNumber.visibility = View.VISIBLE
            }

            binding.data = tag

            binding.checkbox.setOnClickListener {
                if (binding.checkbox.isChecked) {
                    setCheckboxColors()
                    tagSendInt.onSendTag(tag)
                } else {
                    setCheckboxColors()
                    tagSendInt.onTagRemove(tag)
                }
            }

            binding.executePendingBindings()
        }

        private fun setCheckboxColors() {
            if (binding.checkbox.isChecked) {
                val color = franchiseModel.franchiseModel.value?.franchisePrimaryColor
                color?.let {
                    binding.checkbox.buttonTintList = ColorStateList.valueOf(it)
                    binding.regPlate.setTextColor(it)
                    binding.serialNumber.setTextColor(it)
                } ?: run {
                    binding.checkbox.buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.figmaSplashScreenColor
                        )
                    )
                    binding.regPlate.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.figmaSplashScreenColor
                        )
                    )
                    binding.serialNumber.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.figmaSplashScreenColor
                        )
                    )
                }

            } else {
                binding.checkbox.buttonTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.primary_light_dark
                    )
                )
                binding.regPlate.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.primary_light_dark
                    )
                )
                binding.serialNumber.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.primary_light_dark
                    )
                )
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

    interface PaginationUpdate {
        fun sendDataFillFilterAdapter(
            nextPage: Int,
            perPage: Int,
            flow: MutableStateFlow<SubmitResult<IndexData>>,
        )
    }

    interface TagSend {
        fun onSendTag(tag: Tag)
        fun onTagRemove(tag: Tag)
    }

}