package com.mobility.enp.view.adapters.tool_history.main_and_filter_screen

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class ToolHistoryListingAdapter(
    private val toolHistoryIndex: IndexData,
    private val viewModel: UserPassViewModel,
    private val complaintInterface: ToolHistoryListingPassageAdapter.SendToFragment,
    val lifecycleOwner: LifecycleOwner,
    val passageData: SavePassageData, val paginationUpdate: PaginationUpdate
) : RecyclerView.Adapter<ToolHistoryListingAdapter.TagsViewHolder>() {

    var currentPage: Int = toolHistoryIndex.data?.currentPage ?: 0
    val lastPage: Int = toolHistoryIndex.data?.lastPage ?: 0
    val perPage: Int = toolHistoryIndex.data?.perPage ?: 0
    val total: Int = toolHistoryIndex.data?.total ?: 0

    val listOfTags: ArrayList<Tag> = toolHistoryIndex.data?.tags as ArrayList<Tag>

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

            val itemSerialNumber = toolHistoryIndex.serialNumber

            val contentInterface = object : PassageDataInterface {

                override fun onOk(toolHistoryListing: V2HistoryTagResponse) {

                    binding.cycler.visibility = View.VISIBLE
                    binding.cyclerTotalPrice.visibility = View.VISIBLE
                    binding.noPassage.visibility = View.GONE

                    toolHistoryListing.serial = itemSerialNumber

                    passageData.psgData(toolHistoryListing)

                    holder.binding.progbar.visibility = View.GONE

                    if (!toolHistoryListing.data?.sumTags.isNullOrEmpty()) {


                        /**
                         * this adapter is used for presenting the total cost of tag
                         * @param takes in a List<SumTag> of costs
                         */
                        binding.cyclerTotalPrice.adapter =
                            TotalCostPassageAdapter(toolHistoryListing.data?.sumTags)
                        binding.cyclerTotalPrice.layoutManager =
                            LinearLayoutManager(
                                binding.root.context,
                                LinearLayoutManager.VERTICAL,
                                false
                            )

                        binding.position = position


                        /**
                         * this adapter is used for presenting individual passages for 1 tag serial
                         * @param takes in V2HistoryTagResponse model from 1 tag
                         */
                        binding.cycler.adapter = ToolHistoryListingPassageAdapter(
                            toolHistoryListing,
                            complaintInterface,
                            false,
                            lifecycleOwner,
                            itemSerialNumber, countryCode
                        )
                        binding.cycler.layoutManager = LinearLayoutManager(binding.root.context)

                        binding.executePendingBindings()


                    } else {
                        binding.noPassage.visibility = View.VISIBLE
                        binding.cycler.visibility = View.GONE
                        binding.cyclerTotalPrice.visibility = View.GONE
                    }
                }

                override fun onFailed(boolean: Boolean, cause: String) {
                    holder.binding.progbar.visibility = View.GONE
                    Toast.makeText(
                        binding.root.context,
                        "failed to set sub adapter with data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


            val indexListing =
                MutableStateFlow<SubmitResult<V2HistoryTagResponse>>(SubmitResult.Loading)

            collectLatestFlow(lifecycleOwner, indexListing) { serverResponse ->
                when (serverResponse) {
                    is SubmitResult.Success -> {
                        contentInterface.onOk(serverResponse.data)
                    }

                    is SubmitResult.FailureServerError -> {
                        logError(binding.root.context.resources.getString(R.string.server_error_msg))
                    }

                    is SubmitResult.FailureApiError -> {
                        logError(binding.root.context.resources.getString(R.string.api_call_error))
                    }

                    else -> {
                        SubmitResult.Empty
                    }
                }
            }


            if (viewModel.internetAvailable()) {
                viewModel.getToolHistoryTransit(indexListing, toolHistoryIndex.serialNumber, 1)
            } else {
                viewModel.fetchStoredData(contentInterface, toolHistoryIndex.serialNumber)
            }

            binding.executePendingBindings()
        }
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
            it[holder.bindingAdapterPosition].country?.text
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
                ToolHistoryListingPassageAdapter.Companion.TAG,
                "last item $currentItem total $total"
            )
        }
    }


    interface PassageDataInterface {
        fun onOk(toolHistoryListing: V2HistoryTagResponse)
        fun onFailed(boolean: Boolean, cause: String)
    }

    interface SavePassageData {
        fun psgData(toolHistoryListing: V2HistoryTagResponse)
    }

    interface PaginationUpdate {
        fun sendDataFillMainAdapter(
            nextPage: Int,
            perPage: Int,
            flow: MutableStateFlow<SubmitResult<IndexData>>,
        )
    }

}