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
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.databinding.ToolHistoryIndexCardBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestFlow
import com.mobility.enp.view.adapters.tool_history.combined.HistoryTotalCostAdapter
import com.mobility.enp.view.adapters.tool_history.result.HistorySerialAdapterResultScreen
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class HistorySerialAdapter(
    private var toolHistoryIndex: IndexData,
    private val viewModel: UserPassViewModel,
    private val complaintInterface: HistoryPassageAdapter.SendToFragment,
    private val complaintInterfaceCroatia: HistoryPassageAdapterCroatia.SendToFragment,
    val lifecycleOwner: LifecycleOwner,
    val paginationUpdate: PaginationUpdate
) : RecyclerView.Adapter<HistorySerialAdapter.TagsViewHolder>() {

    var currentPage: Int = toolHistoryIndex.data?.currentPage ?: 0
    var lastPage: Int = toolHistoryIndex.data?.lastPage ?: 0
    var perPage: Int = toolHistoryIndex.data?.perPage ?: 0
    var total: Int = toolHistoryIndex.data?.total ?: 0

    val listOfTags: ArrayList<Tag> = toolHistoryIndex.data?.tags as ArrayList<Tag>

    fun clearData() {
        toolHistoryIndex = IndexData(0, null, "", null)
        lastPage = 0
        perPage = 0
        total = 0
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

            val indexListing =
                MutableStateFlow<SubmitResult<V2HistoryTagResponse?>>(SubmitResult.Loading)

            collectLatestFlow(lifecycleOwner, indexListing) { serverResponse ->
                holder.binding.progbar.visibility = View.GONE

                when (serverResponse) {

                    is SubmitResult.Success -> {
                        serverResponse.data?.let { data ->
                            data.serial = itemSerialNumber
                            data.countryCode = viewModel.selectedCountry

                            roomPassageDataFirstScreen(
                                data,
                                viewModel.selectedCountry
                            ) // saves to room passages for this serial

                            binding.cyclerTotalPrice.visibility = View.INVISIBLE
                            binding.noPassage.visibility = View.GONE
                            holder.binding.progbar.visibility = View.GONE

                            if (viewModel.selectedCountry != binding.root.context.getString(R.string.croatia_hr)) {
                                if (!data.data?.sumTags.isNullOrEmpty()) {  // sum total of price for passages hr doesn't have this data
                                    /**
                                     * this adapter is used for presenting the total cost of tag
                                     * @param takes in a List<SumTag> of costs
                                     */
                                    binding.cyclerTotalPrice.adapter =
                                        HistoryTotalCostAdapter(data.data.sumTags)
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

                            if (!data.data?.records?.items.isNullOrEmpty()) {

                                binding.position = position

                                val heightInDp = when (data.data.records.items.size) {
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


                                // croatia passage adapter
                                if (viewModel.selectedCountry == binding.root.context.getString(R.string.croatia_hr)) {
                                    binding.cycler.adapter = HistoryPassageAdapterCroatia(
                                        data,
                                        complaintInterfaceCroatia,
                                        lifecycleOwner,
                                        itemSerialNumber
                                    )
                                } else {
                                    //record of passages for tag for normal countries
                                    //adapter that presents the passages
                                    binding.cycler.adapter = HistoryPassageAdapter(
                                        data,
                                        complaintInterface,
                                        false,
                                        lifecycleOwner,
                                        itemSerialNumber, countryCode, viewModel
                                    )
                                }

                                binding.cycler.layoutManager =
                                    LinearLayoutManager(binding.root.context)

                                binding.executePendingBindings()
                            }

                            if (data.data?.records?.items.isNullOrEmpty()) {
                                binding.noPassage.visibility = View.VISIBLE
                                binding.cycler.visibility = View.GONE
                            }
                        }

                    }

                    is SubmitResult.FailureServerError -> {
                        logError(binding.root.context.resources.getString(R.string.server_error_msg))
                    }

                    is SubmitResult.FailureApiError -> {
                        logError(binding.root.context.resources.getString(R.string.api_call_error))
                    }

                    else -> {}
                }
            }


            if (viewModel.internetAvailable()) {
                binding.progbar.visibility = View.VISIBLE

                viewModel.getToolHistoryTransit(indexListing, toolHistoryIndex.serialNumber, 1)
            } else {
//                viewModel.fetchStoredData(contentInterface, toolHistoryIndex.serialNumber) todo
            }

            binding.executePendingBindings()
        }
    }

    private fun roomPassageDataFirstScreen(data: V2HistoryTagResponse, selectedCountry: String) {
        Log.d(
            HistorySerialAdapterResultScreen.Companion.TAG, "data for room: /n " +
                    "$data"
        )
        viewModel.roomPassageDataFirstScreen(data, selectedCountry)
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

    override fun getItemCount(): Int = toolHistoryIndex.data?.tags?.size ?: 0

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

        val country = toolHistoryIndex.data?.tags?.let {
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
                                listOfTags.add(item)
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

            paginationUpdate.sendDataFillMainAdapter(currentPage + 1, perPage, indexListing)
        } else if (lastPage == currentPage && listOfTags[listOfTags.size - 1] == currentItem) {
            Log.d(
                HistoryPassageAdapter.Companion.TAG,
                "last item $currentItem total $total"
            )
        }
    }

    interface PaginationUpdate {
        fun sendDataFillMainAdapter(
            nextPage: Int,
            perPage: Int,
            flow: MutableStateFlow<SubmitResult<IndexData>>,
        )
    }

}