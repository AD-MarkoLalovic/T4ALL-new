package com.mobility.enp.view.adapters.tool_history.result

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
) : ListAdapter<Tag, HistorySerialAdapterResult.TagsViewHolder>(TagDiffCallback) {

    private var currentPage: Int = 0
    private var lastPage: Int = 0

    fun setAdapterData(indexData: List<IndexData>) {
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

    companion object {
        const val TAG = "PrimaryPassageAdapter"
        private var cachedItemHeight: Int = 0

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
        val binding: ToolHistoryIndexCardResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            toolHistoryIndex: TagUtilCycler,
            position: Int
        ) {
            // perform initial data fill // for sub adapter
            binding.data = toolHistoryIndex

            binding.progbar.visibility = View.INVISIBLE
            binding.noPassage.visibility = View.GONE
            binding.nsScroll.visibility = View.INVISIBLE
            binding.cycler.visibility = View.INVISIBLE
            binding.cyclerTotalPrice.visibility = View.INVISIBLE

            binding.nsScroll.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

            val itemSerialNumber = toolHistoryIndex.serialNumber

            //region inner passage adapters
            if (viewModel.selectedCountry == binding.root.context.getString(R.string.croatia_hr)) {

                lifecycleOwner.lifecycleScope.launch {

                    val initLoad = withContext(Dispatchers.IO) {
                        viewModel.getCPassagesResultBySerialCode(
                            itemSerialNumber,
                            binding.root.context.getString(R.string.croatia_hr)
                        )
                    }

                    val listOfPassages = initLoad.flatMap { it?.data?.records?.items.orEmpty() }

                    if (listOfPassages.isEmpty()) {
                        binding.progbar.visibility = View.VISIBLE
                    }

                    setViewHeight(binding, listOfPassages.size, position)

                    val adapter = HistoryPassageAdapterCroatiaResult(
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

                    binding.cycler.adapter = adapter
                    adapter.submitList(listOfPassages)

                    binding.cyclerTotalPrice.visibility = View.GONE

                    binding.executePendingBindings()
                }
            } else {
                //record of passages for tag for normal countries
                //adapter that presents the passages
                lifecycleOwner.lifecycleScope.launch {
                    val initLoad = withContext(Dispatchers.IO) {
                        viewModel.getPassageBySerialNumberCode(
                            itemSerialNumber, viewModel.selectedCountry
                        )
                    }

                    val listOfPassages = initLoad.flatMap { it?.data?.records?.items.orEmpty() }

                    if (listOfPassages.isEmpty()) {
                        binding.progbar.visibility = View.VISIBLE
                    }

                    setViewHeight(binding, listOfPassages.size, position)

                    val adapter = HistoryPassageAdapterResult(
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

                    binding.cycler.adapter = adapter

                    adapter.submitList(listOfPassages)

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
        val maxItems = 3
        val params = binding.nsScroll.layoutParams

        binding.cycler.isNestedScrollingEnabled = true
        binding.nsScroll.isNestedScrollingEnabled = true

        val density = binding.root.context.resources.displayMetrics.density
        val paddingVertical = (5 * density).toInt()
        val paddingHorizontal = (10 * density).toInt()
        binding.cycler.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        binding.cycler.clipToPadding = false

        binding.nsScroll.visibility = View.VISIBLE
        binding.cycler.visibility = View.VISIBLE

        binding.cycler.layoutManager = LinearLayoutManager(binding.root.context)

        if (size == 0) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.nsScroll.layoutParams = params
            binding.nsScroll.requestLayout()
            binding.executePendingBindings()
            return
        }

        val applyHeight = {
            val child = binding.cycler.getChildAt(0)
            val itemHeight = if (child != null) {
                val lp = child.layoutParams as? ViewGroup.MarginLayoutParams
                val height = child.height + (lp?.topMargin ?: 0) + (lp?.bottomMargin ?: 0)
                if (height > 0) {
                    cachedItemHeight = height
                }
                height
            } else if (cachedItemHeight > 0) {
                cachedItemHeight
            } else {
                (140 * density).toInt()
            }

            val itemsToShow = if (size > maxItems) maxItems else size
            params.height = (itemHeight * itemsToShow) + (paddingVertical * 2)

            binding.nsScroll.layoutParams = params
            binding.nsScroll.requestLayout()
            binding.root.requestLayout()
            binding.executePendingBindings()
        }

        val layoutListener = object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                val child = binding.cycler.getChildAt(0)
                if (child != null && child.height > 0) {
                    binding.cycler.removeOnLayoutChangeListener(this)
                    applyHeight()
                }
            }
        }
        binding.cycler.addOnLayoutChangeListener(layoutListener)

        applyHeight()
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
            tagUtilCycler,
            position
        )

        runPaginationCheck(currentTag)
    }

    private fun runPaginationCheck(currentTag: Tag) {
        if (currentTag == getItem(itemCount - 1)) {
            if (currentPage < lastPage) {
                // trigger background update with flow
                viewModel.getTagsUpdate(currentPage + 1)
            }
        }
    }

}