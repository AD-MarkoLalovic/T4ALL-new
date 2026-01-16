package com.mobility.enp.view.adapters.tool_history.filter

import android.content.res.ColorStateList
import android.util.Log
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
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestFlow
import com.mobility.enp.view.adapters.tool_history.first_screen.HistoryPassageAdapter
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class HistoryTagsAdapter(
    tagInterface: TagSend,
    private val franchiseModel: FranchiseViewModel,
    private val paginationUpdate: PaginationUpdate,
    toolHistoryIndex: IndexData,
    private val complaintInterface: SendToFragment,
    val lifecycleOwner: LifecycleOwner,
    private val userPassViewModel: UserPassViewModel
) :
    RecyclerView.Adapter<HistoryTagsAdapter.ToolHistoryTagsViewHolder>() {

    val tagSendInt = tagInterface
    private val listOfTags: ArrayList<Tag> = toolHistoryIndex.data?.tags as ArrayList<Tag>
    var currentPage: Int = toolHistoryIndex.data?.currentPage ?: 0
    val lastPage: Int = toolHistoryIndex.data?.lastPage ?: 0
    val perPage: Int = toolHistoryIndex.data?.perPage ?: 0
    val total: Int = toolHistoryIndex.data?.total ?: 0

    inner class ToolHistoryTagsViewHolder(val binding: ToolHistoryTagsAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: Tag) {

            val selected = userPassViewModel.isSelected(tag)
            binding.checkbox.isChecked = selected

            if (selected) {
                userPassViewModel.selectedTags.add(tag)
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
                    userPassViewModel.select(tag)
                    userPassViewModel.selectedTags.add(tag)
                } else {
                    userPassViewModel.unselect(tag)
                    userPassViewModel.selectedTags.remove(tag)
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


    private fun performDataFill(currentItem: Tag) {
        if (listOfTags[listOfTags.size - 1] == currentItem && lastPage > currentPage) {
            val indexListing =
                MutableStateFlow<SubmitResult<IndexData>>(SubmitResult.Loading)

            collectLatestFlow(lifecycleOwner, indexListing) { serverResponse ->
                complaintInterface.stopSpinner()
                when (serverResponse) {
                    is SubmitResult.Success -> {
                        serverResponse.data.let {
                            Log.d(
                                "MainAdapter",
                                "performDataFill: ${it.data?.currentPage} ${it.data?.lastPage}"
                            )
                            currentPage = it.data?.currentPage ?: 0

                            for (item: Tag in it.data?.tags ?: emptyList()) {
                                listOfTags.add(item)
                                notifyItemChanged(listOfTags.size - 1)
                                Log.d(
                                    "FilterAdapter $currentItem $total",
                                    "dataInserted: $item"
                                )
                            }
                        }
                    }

                    else -> {
                        SubmitResult.Empty
                    }
                }
            }
            complaintInterface.startSpinner()
            paginationUpdate.sendDataFillFilterAdapter(currentPage + 1, perPage, indexListing)
        } else if (lastPage == currentPage && listOfTags[listOfTags.size - 1] == currentItem) {
            Log.d(
                HistoryPassageAdapter.Companion.TAG,
                "last item $currentItem total $total"
            )
        }
    }

    override fun getItemCount(): Int {
        return listOfTags.size
    }

    override fun onBindViewHolder(holder: ToolHistoryTagsViewHolder, position: Int) {
        val tag = listOfTags[holder.bindingAdapterPosition]

        holder.bind(tag)
        performDataFill(tag)
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

    interface SendToFragment {
        fun startSpinner()
        fun stopSpinner()
    }

}