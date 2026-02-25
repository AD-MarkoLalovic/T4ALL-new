package com.mobility.enp.view.adapters.tool_history.filter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.databinding.ToolHistoryTagsAdapterBinding
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel

class ToolHistoryFilterFragmentSerialAdapter(
    private val franchiseModel: FranchiseViewModel,
    val lifecycleOwner: LifecycleOwner,
    private val viewModel: UserPassViewModel
) :
    RecyclerView.Adapter<ToolHistoryFilterFragmentSerialAdapter.ToolHistoryTagsViewHolder>() {

    private var listOfTags: List<Tag> = emptyList()

    private var currentPage: Int = 0
    private var lastPage: Int = 0
    private var total: Int = 0

    fun setAdapterData(indexData: List<IndexData>) {
        if (indexData.isNotEmpty()) {
            currentPage = indexData[indexData.size - 1].currentPage ?: 0
            lastPage = indexData[indexData.size - 1].lastPage ?: 0
            total = indexData.size
        }

        listOfTags = indexData.flatMap { it.data?.tags.orEmpty() }

        for (i in listOfTags.indices) {
            notifyItemChanged(i)
        }

        if (total > 1) {
            viewModel.getSerialDeviceDataValidationSerialAdapter(total)
        }
    }

    inner class ToolHistoryTagsViewHolder(val binding: ToolHistoryTagsAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: Tag) {

            val selected = viewModel.isSelected(tag)
            binding.checkbox.isChecked = selected

            if (selected) {
                viewModel.selectedTags.add(tag)
                setCheckboxColors()
            }

            if (tag.registrationPlate.isNullOrEmpty()) { // ignore recommendation android studio is wrong here
                tag.registrationPlate = tag.serialNumber
                binding.serialNumber.visibility = View.INVISIBLE
            } else {
                binding.serialNumber.visibility = View.VISIBLE
            }

            binding.data = tag

            binding.checkbox.setOnClickListener {
                if (binding.checkbox.isChecked) {
                    viewModel.select(tag)
                    viewModel.selectedTags.add(tag)
                } else {
                    viewModel.unselect(tag)
                    viewModel.selectedTags.remove(tag)
                }
                setCheckboxColors()
            }

            if (binding.regPlate.text == binding.serialNumber.text) {
                binding.serialNumber.visibility = View.INVISIBLE
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
        return ToolHistoryTagsViewHolder(
            ToolHistoryTagsAdapterBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }


    override fun getItemCount(): Int {
        return listOfTags.size
    }

    override fun onBindViewHolder(holder: ToolHistoryTagsViewHolder, position: Int) {
        val currentTag = listOfTags[holder.bindingAdapterPosition]
        holder.bind(currentTag)
        runPaginationCheck(currentTag)
    }

    private fun runPaginationCheck(currentTag: Tag) {
        if (currentTag == listOfTags[listOfTags.size - 1]) {
            if (currentPage < lastPage) {
                // trigger background update with flow
                viewModel.getTagsUpdate(currentPage + 1)
            }
        }
    }
}