package com.mobility.enp.view.adapters.tool_history.result

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.data.model.api_tool_history.TagUtilCycler
import com.mobility.enp.data.model.api_tool_history.listing.ToolHistoryListing
import com.mobility.enp.data.model.api_tool_history.listing.TotalAmount
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.databinding.ToolHistoryIndexCardBinding
import com.mobility.enp.view.adapters.tool_history.main_screen.TotalCostPassageAdapter
import com.mobility.enp.viewmodel.PassageHistoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryResultAdapter(
    val tags: List<Tag>,
    val viewModel: PassageHistoryViewModel,
    private val complaintInterface: HistoryContentPagingAdapter.SendToFragment,
    val lifecycleOwner: LifecycleOwner, val countryCode: String
) :
    RecyclerView.Adapter<HistoryResultAdapter.TagsViewHolder>() {

    private lateinit var context: Context

    companion object {
        const val TAG = "PrimaryPassageAdapter"
    }

    inner class TagsViewHolder(
        val binding: ToolHistoryIndexCardBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            toolHistoryIndex: TagUtilCycler,
            position: Int,
            viewModel: PassageHistoryViewModel,
            context: Context,
            holder: TagsViewHolder,
            complaintInterface: HistoryContentPagingAdapter.SendToFragment
        ) {
            // perform initial data fill // for sub adapter

            holder.binding.progbar.visibility = View.VISIBLE
            binding.noPassage.visibility = View.GONE

            val initDataTransfer = object : PassageDataInterface {
                override fun onOk(toolHistoryListing: ToolHistoryListing) {
                    Log.d(TAG, "OnOk : $toolHistoryListing")

                    holder.binding.progbar.visibility = View.GONE


                    if (!toolHistoryListing.data.sum.isNullOrEmpty()) {
                        val total = toolHistoryListing.data.sum[0].total
                        binding.cyclerTotalPrice.adapter =
                            TotalCostPassageAdapter(total as ArrayList<TotalAmount>)
                        binding.cyclerTotalPrice.layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                        binding.data = toolHistoryIndex
                        binding.position = position

                        binding.cycler.adapter = HistoryContentPagingAdapter(
                            toolHistoryListing,
                            complaintInterface,
                            lifecycleOwner,
                            toolHistoryIndex.serialNumber,
                            countryCode
                        )
                        binding.cycler.layoutManager = LinearLayoutManager(context)

                        binding.executePendingBindings()
                    } else {
                        binding.noPassage.visibility = View.VISIBLE
                    }
                }

                override fun onFailed(boolean: Boolean, cause: String) {
                    holder.binding.progbar.visibility = View.GONE
                    Toast.makeText(
                        context,
                        "failed to set sub adapter with data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val dateFrom = viewModel.startDate.value?.formattedTime?.replace("/", ".")
            val dateTo = viewModel.endDate.value?.formattedTime?.replace("/", ".")

            if (dateFrom != null && dateTo != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getToolHistoryTransitResult(
                        initDataTransfer,
                        toolHistoryIndex.serialNumber,
                        1, dateFrom, dateTo
                    )   // fetch initial data here

                }
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsViewHolder {
        context = parent.context
        return TagsViewHolder(
            ToolHistoryIndexCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = tags.size

    override fun onBindViewHolder(holder: TagsViewHolder, position: Int) {
        val currentTag = tags[holder.bindingAdapterPosition]
        val tagUtilCycler = TagUtilCycler("", "")
        try {
            tagUtilCycler.serialNumber = currentTag.serialNumber ?: "no api data"
            tagUtilCycler.registrationPlate = currentTag.registrationPlate ?: "no api data"
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

        holder.bind(
            tagUtilCycler,
            holder.bindingAdapterPosition,
            viewModel,
            context,
            holder,
            complaintInterface
        )
    }

    interface PassageDataInterface {
        fun onOk(toolHistoryListing: ToolHistoryListing)
        fun onFailed(boolean: Boolean, cause: String)
    }
}