package com.mobility.enp.view.adapters.tool_history.main_screen

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
import com.mobility.enp.data.model.api_tool_history.ToolHistoryListing
import com.mobility.enp.data.model.api_tool_history.TotalAmount
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.databinding.ToolHistoryIndexCardBinding
import com.mobility.enp.viewmodel.PassageHistoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToolHistoryListingAdapter(
    private val toolHistoryIndex: IndexData,
    private val viewModel: PassageHistoryViewModel,
    private val complaintInterface: ToolHistoryListingPassageAdapter.SendToFragment,
    val lifecycleOwner: LifecycleOwner,
    val passageData: SavePassageData
) : RecyclerView.Adapter<ToolHistoryListingAdapter.TagsViewHolder>() {

    private lateinit var context: Context

    companion object {
        const val TAG = "PrimaryPassageAdapter"
    }

    inner class TagsViewHolder(
        val binding: ToolHistoryIndexCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            toolHistoryIndex: TagUtilCycler,
            position: Int,
            holder: TagsViewHolder,
        ) {
            // perform initial data fill // for sub adapter
            binding.data = toolHistoryIndex

            holder.binding.progbar.visibility = View.VISIBLE
            binding.noPassage.visibility = View.GONE

            val itemSerialNumber = toolHistoryIndex.serialNumber

            val initDataTransfer = object : PassageDataInterface {
                override fun onOk(toolHistoryListing: ToolHistoryListing) {
                    toolHistoryListing.serial = itemSerialNumber
                    passageData.psgData(toolHistoryListing)

                    holder.binding.progbar.visibility = View.GONE

                    if (toolHistoryListing.data.sum.isNotEmpty()) {
                        val total = toolHistoryListing.data.sum[0].total
                        binding.cyclerTotalPrice.adapter =
                            TotalCostPassageAdapter(total as ArrayList<TotalAmount>)
                        binding.cyclerTotalPrice.layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                        binding.position = position

                        binding.cycler.adapter = ToolHistoryListingPassageAdapter(
                            toolHistoryListing,
                            complaintInterface,
                            false,
                            lifecycleOwner,
                            itemSerialNumber
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
                        context, "failed to set sub adapter with data", Toast.LENGTH_SHORT
                    ).show()
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                viewModel.getToolHistoryTransit(
                    initDataTransfer, context, toolHistoryIndex.serialNumber, 1
                )   // fetch initial data here

            }

            binding.executePendingBindings()
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsViewHolder {
        context = parent.context
        return TagsViewHolder(
            ToolHistoryIndexCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = toolHistoryIndex.data?.tags?.size ?: 0

    override fun onBindViewHolder(holder: TagsViewHolder, position: Int) {
        val currentTag = toolHistoryIndex.data?.tags!![holder.bindingAdapterPosition]

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

        holder.bind(
            tagUtilCycler, holder.bindingAdapterPosition, holder
        )
    }

    interface PassageDataInterface {
        fun onOk(toolHistoryListing: ToolHistoryListing)
        fun onFailed(boolean: Boolean, cause: String)
    }

    interface SavePassageData {
        fun psgData(toolHistoryListing: ToolHistoryListing)
    }
}