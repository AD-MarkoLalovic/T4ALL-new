package com.mobility.enp.view.adapters.tool_history.first_screen

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.v2base_model.Item
import com.mobility.enp.databinding.ItemRelationPassageRealCroatiaBinding
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.launch

class HistoryPassageAdapterCroatia(
    private val complaintInterface: SendToFragment,
    private val lifecycleOwner: LifecycleOwner,
    private val tagSerialNumber: String,
    private val viewmodel: UserPassViewModel,
    private var onInitDataSize: (Int) -> Unit
) : ListAdapter<Item, HistoryPassageAdapterCroatia.RelationViewHolder>(DIFF_CALLBACK) {

    private lateinit var context: Context
    private var totalPages: Int = 0
    private var currentPage: Int = 0
    private var lastPage: Int = 0

    init {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.getCroatiaPassagesBySerialPage(tagSerialNumber, viewmodel.selectedCountry)
                    .collect { data ->
                        if (data.isNotEmpty()) {
                            totalPages = data.size

                            currentPage = data[data.size - 1]?.currentPage ?: 0
                            lastPage = data[data.size - 1]?.lastPage ?: 0

                            if (data.isNotEmpty()) { // sum of tags
                                onInitDataSize(data[0]?.data?.records?.items?.size ?: 0)
                            }

                            val newList = buildList {
                                data.forEach { passage ->
                                    passage?.data?.records?.items?.let {
                                        addAll(it)
                                    }
                                }
                            }

                            submitList(newList)
                        }
                    }
            }
        }

        viewmodel.getToolHistoryTransitCroatia(tagSerialNumber, 1)
        if (totalPages > 1) {
            viewmodel.getSerialPassageTagDataValidationCroatia(
                totalPages,
                tagSerialNumber,
                context.getString(R.string.croatia_hr)
            )
        }
    }

    companion object {
        const val TAG = "PassageAdapter"

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {

            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class RelationViewHolder(
        private val context: Context, private val binding: ItemRelationPassageRealCroatiaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(relation: Item) {
            with(binding) {
                carTagNumber.text = relation.amount.toString()
                carTagCurrency.text = "EUR"
                passageRelation.text = relation.tollPlaza
                passageEntryDate.text = root.context.getString(R.string.time_of_passage)
                passageExitDate.text = relation.checkDate

                binding.toolHistoryStatus.setBackgroundResource(R.drawable.status_icon_light)
                topContainer.setBackgroundResource(R.drawable.tool_history_top_light)
                bottomContainer.setBackgroundResource(R.drawable.tool_history_bottom_light)

                btnComplaint.setOnClickListener {
                    complaintInterface.croatiaReclamationDialog()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationViewHolder {
        context = parent.context
        return RelationViewHolder(
            context, ItemRelationPassageRealCroatiaBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }


    override fun onBindViewHolder(holder: RelationViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
        runPaginationCheck(currentItem)
    }

    private fun runPaginationCheck(currentItem: Item) {
        if (currentItem == getItem(itemCount - 1)) {
            if (currentPage < lastPage) {
                // trigger background update with flow
                viewmodel.getToolHistoryTransitCroatia(tagSerialNumber, currentPage + 1)
            }
        }
    }

    interface SendToFragment {
        fun stopSpinner()

        fun croatiaReclamationDialog()
    }

}