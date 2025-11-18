package com.mobility.enp.view.adapters.tool_history.main_and_filter_screen

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.v2base_model.Item
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.databinding.ItemRelationPassageRealCroatiaBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.Util
import com.mobility.enp.util.collectLatestFlow
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class ToolHistoryListingPassageAdapterCroatia(
    private val data: V2HistoryTagResponse,
    private val complaintInterface: SendToFragment,
    private val lifecycleOwner: LifecycleOwner,
    private val tagSerialNumber: String,
) :
    RecyclerView.Adapter<ToolHistoryListingPassageAdapterCroatia.RelationViewHolder>() {

    private lateinit var context: Context

    private var currentPage = data.data?.records?.pagination?.currentPage ?: 1
    private val lastPage = data.data?.records?.pagination?.lastPage ?: 1

    private var relation: ArrayList<Item> =
        data.data?.records?.items as ArrayList<Item>

    companion object {
        const val TAG = "PassageAdapter"
    }

    inner class RelationViewHolder(
        private val context: Context,
        private val binding: ItemRelationPassageRealCroatiaBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(relation: Item) {
            Log.d(TAG, "bind: $relation")

            with(binding) {
                carTagNumber.text = relation.amount.toString()
                carTagCurrency.text = "EUR"
                passageRelation.text = relation.tollPlaza
                passageEntryDate.text = root.context.getString(R.string.time_of_passage)
                passageExitDate.text = relation.checkDate

                btnComplaint.setOnClickListener {
                    complaintInterface.croatiaReclamationDialog()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationViewHolder {
        context = parent.context
        return RelationViewHolder(
            context,
            ItemRelationPassageRealCroatiaBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = relation.size

    override fun onBindViewHolder(holder: RelationViewHolder, position: Int) {
        val currentItem = relation[holder.bindingAdapterPosition]

        holder.bind(currentItem)

        if (Util.isNetworkAvailable(context)) {
            performDataFill(currentItem, holder.bindingAdapterPosition) // paggination
        }
    }

    private fun performDataFill(currentItem: Item, bindingAdapterPosition: Int) {
        if (relation[relation.size - 1] == currentItem && lastPage > currentPage) {
            val indexListing =
                MutableStateFlow<SubmitResult<V2HistoryTagResponse>>(SubmitResult.Loading)

            collectLatestFlow(lifecycleOwner, indexListing) { serverResponse ->
                complaintInterface.stopSpinner()
                when (serverResponse) {
                    is SubmitResult.Success -> {
                        serverResponse.data.let {
                            Log.d(
                                TAG,
                                "performDataFill: ${it.data?.records?.pagination?.currentPage} ${it.data?.records?.pagination?.lastPage}"
                            )
                            currentPage = it.data?.records?.pagination?.currentPage ?: 1

                            for (item: Item in it.data?.records?.items ?: emptyList()) {
                                relation.add(item)
                                notifyItemChanged(relation.size - 1)
                                Log.d(TAG, "dataInserted: $item")
                            }
                        }
                    }

                    else -> {}
                }
            }

            complaintInterface.sendDataFill(currentPage + 1, indexListing, tagSerialNumber)
        } else if (lastPage == currentPage && relation[relation.size - 1] == currentItem) {
            Log.d(TAG, "performDataFill: no more passage data for tag ${data.serial}")
        }
    }

    interface SendToFragment {
        fun sendDataFill(
            nextPage: Int,
            flow: MutableStateFlow<SubmitResult<V2HistoryTagResponse>>,
            tagSerialNumber: String
        )

        fun stopSpinner()

        fun croatiaReclamationDialog()
    }

}