package com.mobility.enp.view.adapters.tool_history.first_screen

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
import com.mobility.enp.databinding.ToolHistoryIndexCardBinding
import com.mobility.enp.view.adapters.tool_history.combined.HistoryTotalCostAdapter
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistorySerialAdapter(
    private val viewModel: UserPassViewModel,
    private val complaintInterface: HistoryPassageAdapter.SendToFragment,
    private val complaintInterfaceCroatia: HistoryPassageAdapterCroatia.SendToFragment,
    val lifecycleOwner: LifecycleOwner,
    private val stopSpinner: (Unit) -> Unit,
    private val hasPassages: (ArrayList<Boolean>) -> Unit
) : RecyclerView.Adapter<HistorySerialAdapter.TagsViewHolder>() {

    private var listOfTags: List<Tag> = emptyList()
    private var currentPage: Int = 0
    private var lastPage: Int = 0
    private val listOfPassages: ArrayList<Boolean> = arrayListOf()
    private var paginationJob: Job? = null

    override fun onViewRecycled(holder: TagsViewHolder) {
        super.onViewRecycled(holder)
        holder.job?.cancel()
        (holder.binding.cycler.adapter as? HistoryPassageAdapter)?.cancelJob()
        (holder.binding.cycler.adapter as? HistoryPassageAdapterCroatia)?.cancelJob()
    }

    fun clearAdapter(){
        paginationJob?.cancel()
        listOfTags = emptyList()
        currentPage = 0
        lastPage = 0
        listOfPassages.clear()
        notifyDataSetChanged()
    }

    fun setAdapterData(indexData: List<IndexData>) {
        listOfPassages.clear()
        if (indexData.isNotEmpty()) {
            currentPage = indexData[indexData.size - 1].currentPage ?: 0
            lastPage = indexData[indexData.size - 1].lastPage ?: 0
        }

        listOfTags = indexData.flatMap { it.data?.tags.orEmpty() }

        if (currentPage < lastPage) {
            viewModel.getSerialDeviceDataValidationSerialAdapter(lastPage)
        }

        for (tag in listOfTags.indices){
            notifyItemChanged(tag)
        }
    }

    companion object {
        const val TAG = "PrimaryPassageAdapter"
    }

    inner class TagsViewHolder(
        val binding: ToolHistoryIndexCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        var job: Job? = null

        fun bind(
            toolHistoryIndex: TagUtilCycler, position: Int, holder: TagsViewHolder, currentTag: Tag
        ) {
            job?.cancel()
            (binding.cycler.adapter as? HistoryPassageAdapter)?.cancelJob()
            (binding.cycler.adapter as? HistoryPassageAdapterCroatia)?.cancelJob()

            hideViews(binding)

            setViewHeight(binding, 0, position)
            // perform initial data fill // for sub adapter
            binding.data = toolHistoryIndex

            val itemSerialNumber = toolHistoryIndex.serialNumber

            //region inner passage adapters
            if (viewModel.selectedCountry == binding.root.context.getString(R.string.croatia_hr)) {

                job = lifecycleOwner.lifecycleScope.launch() {
                    val initLoad = withContext(Dispatchers.IO) {
                        viewModel.getCPassagesBySerialCountry(
                            itemSerialNumber, binding.root.context.getString(R.string.croatia_hr)
                        )
                    }

                    val listOfPassages = initLoad.flatMap { it?.data?.records?.items.orEmpty() }

                    if (listOfPassages.isEmpty()) {
                        binding.progbar.visibility = View.VISIBLE
                    }

                    setViewHeight(binding, listOfPassages.size, position)

                    val adapter = HistoryPassageAdapterCroatia(
                        complaintInterfaceCroatia,
                        lifecycleOwner,
                        itemSerialNumber,
                        viewModel,
                        { size ->
                            binding.progbar.visibility = View.GONE
                            binding.cyclerTotalPrice.adapter = HistoryTotalCostAdapter(emptyList())
                            binding.cyclerTotalPrice.layoutManager = LinearLayoutManager(
                                binding.root.context, LinearLayoutManager.VERTICAL, false
                            )
                            binding.cyclerTotalPrice.visibility = View.GONE
                            setViewHeight(binding, size, position)
                            setNoPassage(binding, size)

                            binding.executePendingBindings()
                        })

                    binding.cycler.adapter = adapter

                    adapter.submitList(listOfPassages)

                    binding.cyclerTotalPrice.visibility = View.GONE

                    binding.executePendingBindings()
                }
            } else {
                //record of passages for tag for normal countries
                //adapter that presents the passages

                job = lifecycleOwner.lifecycleScope.launch {
                    val initLoad = withContext(Dispatchers.IO) {
                        viewModel.getPassageBySerialCodeCountry(
                            itemSerialNumber, viewModel.selectedCountry
                        )
                    }
                    val listOfPassages = initLoad.flatMap { it?.data?.records?.items.orEmpty() }

                    if (listOfPassages.isEmpty()) {
                        binding.progbar.visibility = View.VISIBLE
                    }

                    val adapter = HistoryPassageAdapter(
                        complaintInterface,
                        false,
                        lifecycleOwner,
                        itemSerialNumber,
                        viewModel.selectedCountry,
                        viewModel,
                        { size ->
                            binding.progbar.visibility = View.GONE
                            setViewHeight(binding, size, position)
                            setNoPassage(binding, size)

                            binding.executePendingBindings()
                        },
                        { sumTags ->
                            if (sumTags.isNotEmpty()) {
                                binding.cyclerTotalPrice.adapter = HistoryTotalCostAdapter(sumTags)
                                binding.cyclerTotalPrice.layoutManager =
                                    LinearLayoutManager(
                                        binding.root.context,
                                        LinearLayoutManager.VERTICAL,
                                        false
                                    )

                                binding.cyclerTotalPrice.visibility = View.VISIBLE
                            } else {
                                binding.cyclerTotalPrice.visibility = View.GONE
                            }
                        }
                    )


                    binding.cycler.adapter = adapter

                    adapter.submitList(emptyList())
                    adapter.submitList(listOfPassages)

                    binding.executePendingBindings()
                }
            }
            //endregion
        }
    }

    private fun setViewHeight(binding: ToolHistoryIndexCardBinding, size: Int, position: Int) {
        binding.position = position

        val heightInDp = when (size) {

            0 -> {
                binding.nsScroll.layoutParams.height = 0
                binding.nsScroll.visibility = View.GONE
                binding.cycler.visibility = View.GONE
                return
            }

            1 -> binding.root.context.resources.getDimensionPixelSize(
                R.dimen.recycler_view_two_items
            )

            2 -> binding.root.context.resources.getDimensionPixelSize(
                R.dimen.recycler_view_two_items_modified
            )

            3 -> binding.root.context.resources.getDimensionPixelSize(
                R.dimen.recycler_view_three_items
            )

            else -> binding.root.context.resources.getDimensionPixelSize(
                R.dimen.recycler_view_more_items
            )
        }

        binding.nsScroll.layoutParams.height = heightInDp
        binding.cycler.isNestedScrollingEnabled = true
        binding.nsScroll.requestLayout()

        binding.nsScroll.visibility = View.VISIBLE
        binding.cycler.visibility = View.VISIBLE

        binding.cycler.layoutManager = LinearLayoutManager(binding.root.context)

        binding.executePendingBindings()
    }

    private fun setNoPassage(binding: ToolHistoryIndexCardBinding, size: Int) {
        when (size) {
            0 -> {
                listOfPassages.add(false)

                hideViews(binding)
            }

            else -> {
                listOfPassages.add(true)
                hasPassages(listOfPassages)

                binding.noPassage.visibility = View.GONE
                binding.relativeTop.visibility = View.VISIBLE
                binding.txtSerial.visibility = View.VISIBLE
                binding.txtTotal.visibility = View.VISIBLE
                binding.cyclerTotalPrice.visibility = View.VISIBLE
                binding.center.visibility = View.VISIBLE
                binding.tagSerialNumber.visibility = View.VISIBLE
                binding.txtSerial.visibility = View.VISIBLE
                binding.txtTotal.visibility = View.VISIBLE

                stopSpinner(Unit)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsViewHolder {
        return TagsViewHolder(
            ToolHistoryIndexCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    private fun hideViews(binding: ToolHistoryIndexCardBinding) {
        binding.cycler.adapter = null
        binding.cyclerTotalPrice.adapter = null
        binding.noPassage.visibility = View.GONE
        binding.relativeTop.visibility = View.GONE
        binding.txtSerial.visibility = View.GONE
        binding.txtTotal.visibility = View.GONE
        binding.progbar.visibility = View.GONE
        binding.cyclerTotalPrice.visibility = View.GONE
        binding.center.visibility = View.GONE
        binding.noPassage.visibility = View.GONE
        binding.txtSerial.visibility = View.GONE
        binding.txtTotal.visibility = View.GONE
        binding.tagSerialNumber.visibility = View.GONE
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
            tagUtilCycler, holder.bindingAdapterPosition, holder, currentTag
        )

        runPaginationCheck(currentTag)
    }

    private fun runPaginationCheck(currentTag: Tag) {
        if (currentTag == listOfTags[listOfTags.size - 1]) {
            paginationJob?.cancel()
            paginationJob = lifecycleOwner.lifecycleScope.launch {
                delay(2000)
                stopSpinner(Unit)
                hasPassages(listOfPassages)
                if (currentPage < lastPage) {
                    // trigger background update with flow
                    viewModel.getTagsUpdate(currentPage + 1)
                }
            }
        }
    }

}