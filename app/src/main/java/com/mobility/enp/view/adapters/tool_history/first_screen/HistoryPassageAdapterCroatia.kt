package com.mobility.enp.view.adapters.tool_history.first_screen

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
) : RecyclerView.Adapter<HistoryPassageAdapterCroatia.RelationViewHolder>() {

    private lateinit var context: Context

    private var relation: List<Item> = emptyList()


    init {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.getCroatiaPassagesBySerialPage(tagSerialNumber, viewmodel.selectedCountry)
                    .collect { data ->
                        if (data.isNotEmpty()) {
                            onInitDataSize(data[0]?.data?.records?.items?.size ?: 0)
                        }
                        val listOfPassages: ArrayList<Item> = arrayListOf()
                        for (passages in data) {
                            passages?.data?.records?.items?.let { setOfPassages ->
                                listOfPassages.addAll(setOfPassages)
                            }
                        }

                        if (listOfPassages.toList() != relation) {
                            relation = listOfPassages.toList()
                            for (i in relation.indices) {
                                notifyItemChanged(i)
                            }
                        }
                    }
            }
        }

        viewmodel.getToolHistoryTransitCroatia(tagSerialNumber, 1)
    }

    companion object {
        const val TAG = "PassageAdapter"
    }

    inner class RelationViewHolder(
        private val context: Context, private val binding: ItemRelationPassageRealCroatiaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(relation: Item) {
            Log.d(TAG, "bind: $relation")

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

    override fun getItemCount() = relation.size

    override fun onBindViewHolder(holder: RelationViewHolder, position: Int) {
        val currentItem = relation[holder.bindingAdapterPosition]

        holder.bind(currentItem)
    }

    interface SendToFragment {
        fun stopSpinner()

        fun croatiaReclamationDialog()
    }

}