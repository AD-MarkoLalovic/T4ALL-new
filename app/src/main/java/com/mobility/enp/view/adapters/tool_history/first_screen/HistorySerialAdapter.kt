package com.mobility.enp.view.adapters.tool_history.first_screen

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.TagUtilCycler
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.databinding.ToolHistoryIndexCardBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestFlow
import com.mobility.enp.view.adapters.tool_history.combined.HistoryTotalCostAdapter
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class HistorySerialAdapter(
    private val viewModel: UserPassViewModel,
    private val complaintInterface: HistoryPassageAdapter.SendToFragment,
    private val complaintInterfaceCroatia: HistoryPassageAdapterCroatia.SendToFragment,
    val lifecycleOwner: LifecycleOwner,
) : RecyclerView.Adapter<HistorySerialAdapter.TagsViewHolder>() {

    private var toolHistoryIndex: IndexData? = null

    private var currentPage: Int = 0
    private var lastPage: Int = 0
    private var perPage: Int = 0
    private var total: Int = 0
    private var listOfTags: List<Tag> = emptyList()

    fun setAdapterData(indexData: IndexData) {
        toolHistoryIndex = indexData
        currentPage = toolHistoryIndex?.data?.currentPage ?: 0
        lastPage = toolHistoryIndex?.data?.lastPage ?: 0
        perPage = toolHistoryIndex?.data?.perPage ?: 0
        total = toolHistoryIndex?.data?.total ?: 0
        listOfTags = toolHistoryIndex?.data?.tags ?: emptyList()

        notifyDataSetChanged()
    }

    fun clearData() {
        toolHistoryIndex = null
        currentPage = 0
        lastPage = 0
        total = 0
        listOfTags = emptyList()
        notifyDataSetChanged()
    }

    companion object {
        const val TAG = "PrimaryPassageAdapter"
    }

    inner class TagsViewHolder(
        val binding: ToolHistoryIndexCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            toolHistoryIndex: TagUtilCycler,
            position: Int,
            holder: TagsViewHolder, countryCode: String
        ) {
            // perform initial data fill // for sub adapter

            binding.data = toolHistoryIndex

            holder.binding.progbar.visibility = View.VISIBLE
            binding.noPassage.visibility = View.GONE
            binding.nsScroll.visibility = View.INVISIBLE
            binding.cycler.visibility = View.INVISIBLE
            binding.nsScroll.layoutParams.height = 250
            binding.nsScroll.requestLayout()

            val itemSerialNumber = toolHistoryIndex.serialNumber

            //region inner passage adapters
            if (viewModel.selectedCountry == binding.root.context.getString(R.string.croatia_hr)) {
                binding.cycler.adapter = HistoryPassageAdapterCroatia(
                    complaintInterfaceCroatia,
                    lifecycleOwner,
                    itemSerialNumber, { size ->
                        setViewHeight(binding, size, position)
                    }
                )
                binding.cyclerTotalPrice.visibility = View.GONE
            } else {
                //record of passages for tag for normal countries
                //adapter that presents the passages
                binding.cycler.adapter = HistoryPassageAdapter(
                    complaintInterface,
                    false,
                    lifecycleOwner,
                    itemSerialNumber, viewModel.selectedCountry, viewModel,
                    { size ->
                        binding.progbar.visibility = View.GONE

                        when (size) {
                            0 -> {
                                binding.noPassage.visibility = View.VISIBLE
                            }

                            else -> {
                                binding.noPassage.visibility = View.GONE
                            }
                        }

                        setViewHeight(binding, size, position)
                        Log.d(TAG, "bind: $size")

                    }, { sumTags ->
                        if (sumTags.isNotEmpty()) {  // sum total of price for passages hr doesn't have this data
                            binding.cyclerTotalPrice.adapter =
                                HistoryTotalCostAdapter(sumTags)
                            binding.cyclerTotalPrice.layoutManager =
                                LinearLayoutManager(
                                    binding.root.context,
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )

                            binding.cyclerTotalPrice.visibility = View.VISIBLE
                        } else {
                            binding.cyclerTotalPrice.visibility = View.INVISIBLE
                        }
                    }
                )
            }
            //endregion

            binding.executePendingBindings()
        }
    }

    private fun setViewHeight(binding: ToolHistoryIndexCardBinding, size: Int, position: Int) {
        binding.position = position

        val heightInDp = when (size) {

            0 -> binding.root.context.resources.getDimensionPixelSize(
                R.dimen.recycler_view_one_zero_items
            )

            1 -> binding.root.context.resources.getDimensionPixelSize(
                R.dimen.recycler_view_one_item
            )

            2 -> binding.root.context.resources.getDimensionPixelSize(
                R.dimen.recycler_view_two_items
            )

            3 -> binding.root.context.resources.getDimensionPixelSize(
                R.dimen.recycler_view_three_items
            )

            else -> binding.root.context.resources.getDimensionPixelSize(
                R.dimen.recycler_view_more_items
            )
        }

        binding.nsScroll.layoutParams.height = heightInDp
        binding.nsScroll.requestLayout()

        binding.nsScroll.visibility = View.VISIBLE
        binding.cycler.visibility = View.VISIBLE

        binding.cycler.layoutManager =
            LinearLayoutManager(binding.root.context)

        binding.executePendingBindings()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsViewHolder {
        return TagsViewHolder(
            ToolHistoryIndexCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    private fun logError(string: String) {
        Log.d(TAG, "showError: $string")
    }

    override fun getItemCount(): Int = if (toolHistoryIndex != null) {
        toolHistoryIndex?.data?.tags?.size ?: 0
    } else {
        0
    }

    override fun onBindViewHolder(holder: TagsViewHolder, position: Int) {
        holder.binding.noPassage.visibility = View.GONE
        val currentTag = listOfTags[holder.bindingAdapterPosition]

        val tagUtilCycler = TagUtilCycler("", "")

        try {
            tagUtilCycler.serialNumber = currentTag?.serialNumber ?: "no api data"
            tagUtilCycler.registrationPlate = currentTag?.registrationPlate ?: "no api data"
        } catch (e: NullPointerException) {
            Log.d(TAG, "issueDetected: $tagUtilCycler")
        }

        holder.binding.cycler.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                if (holder.binding.cycler.hasNestedScrollingParent()) {
                    holder.binding.cycler.parent?.requestDisallowInterceptTouchEvent(true)
                }
            } else if (event.action == MotionEvent.ACTION_UP) {
                holder.binding.cycler.performClick()
            }
            false
        }

        val country = toolHistoryIndex?.data?.tags?.let {
            it[holder.bindingAdapterPosition].country?.value
        }

        holder.bind(
            tagUtilCycler,
            holder.bindingAdapterPosition,
            holder,
            country ?: "no data"
        )

        performDataFill(currentTag)
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
//                                listOfTags.add(item) todo update for room
                                notifyItemChanged(listOfTags.size - 1)
                                Log.d(
                                    "MainAdapter",
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

//            paginationUpdate.sendDataFillMainAdapter(currentPage + 1, perPage, indexListing)
        } else if (lastPage == currentPage && listOfTags[listOfTags.size - 1] == currentItem) {
            Log.d(
                HistoryPassageAdapter.Companion.TAG,
                "last item $currentItem total $total"
            )
        }
    }

}