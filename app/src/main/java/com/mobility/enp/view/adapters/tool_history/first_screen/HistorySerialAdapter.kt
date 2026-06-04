package com.mobility.enp.view.adapters.tool_history.first_screen

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
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
) : ListAdapter<Tag, HistorySerialAdapter.TagsViewHolder>(TagDiffCallback) {

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

    fun setAdapterData(indexData: List<IndexData>) {
        listOfPassages.clear()
        if (indexData.isNotEmpty()) {
            currentPage = indexData[indexData.size - 1].currentPage ?: 0
            lastPage = indexData[indexData.size - 1].lastPage ?: 0
        }

        val tags = indexData.flatMap { it.data?.tags.orEmpty() }

        if (currentPage < lastPage) {
            viewModel.getSerialDeviceDataValidationSerialAdapter(lastPage)
        }

        submitList(tags)
    }

    fun clearList(onCommit: () -> Unit) {
        submitList(null) {
            onCommit()
        }
    }

    companion object {
        const val TAG = "PrimaryPassageAdapter"

        private object TagDiffCallback : DiffUtil.ItemCallback<Tag>() {
            override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class TagsViewHolder(
        val binding: ToolHistoryIndexCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        var job: Job? = null

        fun bind(
            toolHistoryIndex: TagUtilCycler, position: Int
        ) {

            binding.cycler.layoutManager = LinearLayoutManager(binding.root.context)
            binding.cycler.isNestedScrollingEnabled = true

            job?.cancel()
            (binding.cycler.adapter as? HistoryPassageAdapter)?.cancelJob()
            (binding.cycler.adapter as? HistoryPassageAdapterCroatia)?.cancelJob()

            hideViews(binding)

            // perform initial data fill // for sub adapter
            binding.data = toolHistoryIndex

            val itemSerialNumber = toolHistoryIndex.serialNumber

            //region inner passage adapters
            if (viewModel.selectedCountry == binding.root.context.getString(R.string.croatia_hr)) {

                job = lifecycleOwner.lifecycleScope.launch {
                    val initLoad = withContext(Dispatchers.IO) {
                        viewModel.getCPassagesBySerialCountry(
                            itemSerialNumber, binding.root.context.getString(R.string.croatia_hr)
                        )
                    }

                    val listOfPassages = initLoad.flatMap { it?.data?.records?.items.orEmpty() }

                    if (listOfPassages.isEmpty()) {
                        binding.progbar.visibility = View.VISIBLE
                    }

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
                            setNoPassage(binding, size)
                            updateCyclerHeight(binding,size)

                            binding.executePendingBindings()
                        })

                    binding.cycler.adapter = adapter

                    adapter.submitList(listOfPassages)

                    binding.cyclerTotalPrice.visibility = View.GONE

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
                            setNoPassage(binding, size)
                            updateCyclerHeight(binding,size)
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

                }
            }
            //endregion

            binding.executePendingBindings()
        }
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

    private fun updateCyclerHeight(binding: ToolHistoryIndexCardBinding, size: Int) {
        val maxItems = 3
        val params = binding.nsScroll.layoutParams

        binding.cycler.isNestedScrollingEnabled = true
        binding.nsScroll.isNestedScrollingEnabled = true

        val density = binding.root.context.resources.displayMetrics.density
        val paddingVertical = (5 * density).toInt()
        val paddingHorizontal = (10 * density).toInt()
        binding.cycler.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        binding.cycler.clipToPadding = false

        if (size == 0) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.nsScroll.layoutParams = params
            return
        }

        binding.cycler.post {
            val child = binding.cycler.getChildAt(0)

            val itemHeight = if (child != null) {
                val lp = child.layoutParams as? ViewGroup.MarginLayoutParams
                child.height + (lp?.topMargin ?: 0) + (lp?.bottomMargin ?: 0)
            } else {
                (140 * density).toInt()
            }

            if (size > maxItems) {
                params.height = (itemHeight * maxItems) + (paddingVertical * 2)
            } else {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

            binding.nsScroll.layoutParams = params
            binding.nsScroll.requestLayout()
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

    override fun onBindViewHolder(holder: TagsViewHolder, position: Int) {
        holder.binding.noPassage.visibility = View.GONE
        val currentTag = getItem(position)

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
            tagUtilCycler, position
        )

        runPaginationCheck(currentTag)
    }

    private fun runPaginationCheck(currentTag: Tag) {
        if (currentTag == getItem(itemCount - 1)) {
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