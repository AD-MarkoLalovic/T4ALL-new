package com.mobility.enp.view.adapters.tool_history.result

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.TagUtilCycler
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.databinding.ToolHistoryIndexCardResultBinding
import com.mobility.enp.view.adapters.tool_history.combined.HistoryTotalCostAdapter
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistorySerialAdapterResult(
    private val viewModel: UserPassViewModel,
    private val complaintInterface: HistoryPassageAdapterResult.SendToFragment,
    private val complaintInterfaceCroatia: HistoryPassageAdapterCroatiaResult.SendToFragment,
    val lifecycleOwner: LifecycleOwner,
) : RecyclerView.Adapter<HistorySerialAdapterResult.TagsViewHolder>() {

    private var listOfTags: List<Tag> = emptyList()

    private var currentPage: Int = 0
    private var lastPage: Int = 0

    fun setAdapterData(indexData: List<IndexData>) {
        if (indexData.isNotEmpty()) {
            currentPage = indexData[indexData.size - 1].currentPage ?: 0
            lastPage = indexData[indexData.size - 1].lastPage ?: 0
        }

        listOfTags = indexData.flatMap { it.data?.tags.orEmpty() }

        for (i in listOfTags.indices) {
            notifyItemChanged(i)
        }

        if (currentPage < lastPage) {
            viewModel.getSerialDeviceDataValidationSerialAdapter(lastPage)
        }
    }

    fun clearData() {
        listOfTags = emptyList()
        currentPage = 0
        lastPage = 0
        notifyDataSetChanged()
    }

    companion object {
        const val TAG = "PrimaryPassageAdapter"
    }

    inner class TagsViewHolder(
        val binding: ToolHistoryIndexCardResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            toolHistoryIndex: TagUtilCycler,
            position: Int,
            holder: TagsViewHolder
        ) {
            // perform initial data fill // for sub adapter
            binding.data = toolHistoryIndex

            holder.binding.progbar.visibility = View.INVISIBLE
            binding.noPassage.visibility = View.GONE
            binding.nsScroll.visibility = View.INVISIBLE
            binding.cycler.visibility = View.INVISIBLE
            binding.cyclerTotalPrice.visibility = View.INVISIBLE

            val itemSerialNumber = toolHistoryIndex.serialNumber

            //region inner passage adapters
            if (viewModel.selectedCountry == binding.root.context.getString(R.string.croatia_hr)) {

                lifecycleOwner.lifecycleScope.launch() {

                    val initLoad = withContext(Dispatchers.IO) {
                        viewModel.getCroatiaPassagesBySerialPageLoadResult(
                            itemSerialNumber,
                            binding.root.context.getString(R.string.croatia_hr)
                        )
                    }

                    val listOfPassages = initLoad.flatMap { it?.data?.records?.items.orEmpty() }

                    if (listOfPassages.isEmpty()) {
                        binding.progbar.visibility = View.VISIBLE
                    }

                    setViewHeight(binding, listOfPassages.size, position)

                    binding.cycler.adapter = HistoryPassageAdapterCroatiaResult(
                        listOfPassages,
                        complaintInterfaceCroatia,
                        lifecycleOwner,
                        itemSerialNumber, viewModel, { size ->
                            binding.progbar.visibility = View.GONE

                            binding.cyclerTotalPrice.adapter =
                                HistoryTotalCostAdapter(emptyList())

                            binding.cyclerTotalPrice.layoutManager =
                                LinearLayoutManager(
                                    binding.root.context,
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )
                            binding.cyclerTotalPrice.visibility = View.INVISIBLE

                            setViewHeight(binding, size, position)
                            setNoPassage(binding, size)
                        }
                    )
                    binding.cyclerTotalPrice.visibility = View.GONE

                    binding.executePendingBindings()
                }
            } else {
                //record of passages for tag for normal countries
                //adapter that presents the passages
                lifecycleOwner.lifecycleScope.launch {
                    val initLoad = withContext(Dispatchers.IO) {
                        viewModel.getV2PassagesBySerialAndCountryCodeLoadResult(
                            itemSerialNumber, viewModel.selectedCountry
                        )
                    }

                    val listOfPassages = initLoad.flatMap { it?.data?.records?.items.orEmpty() }

                    if (listOfPassages.isEmpty()) {
                        binding.progbar.visibility = View.VISIBLE
                    }

                    setViewHeight(binding, listOfPassages.size, position)

                    binding.cycler.adapter = HistoryPassageAdapterResult(
                        listOfPassages,
                        complaintInterface,
                        false,
                        lifecycleOwner,
                        itemSerialNumber, viewModel.selectedCountry, viewModel,
                        { size ->
                            binding.progbar.visibility = View.GONE
                            setViewHeight(binding, size, position)
                            setNoPassage(binding, size)

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

                    binding.executePendingBindings()
                }
            }
            //endregion
        }
    }

    private fun setViewHeight(
        binding: ToolHistoryIndexCardResultBinding,
        size: Int,
        position: Int
    ) {
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

    private fun setNoPassage(binding: ToolHistoryIndexCardResultBinding, size: Int) {
        when (size) {
            0 -> {
                binding.noPassage.visibility = View.VISIBLE
            }

            else -> {
                binding.noPassage.visibility = View.GONE
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsViewHolder {
        return TagsViewHolder(
            ToolHistoryIndexCardResultBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = listOfTags.size ?: 0

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


        holder.bind(
            tagUtilCycler,
            holder.bindingAdapterPosition,
            holder
        )

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